[CmdletBinding()]
param(
    [switch]$Check
)

$ErrorActionPreference = "Stop"

# Project paths

$ProjectRoot = Split-Path -Parent $PSScriptRoot

# Java
#
# Java 21 is the minimum required JDK.
# Any JDK >= 21 is accepted (21, 22, 23, 24, 25, ...).
# The Maven build should target Java 21 in the POM.

$JavaMinMajor = 21

# Spot

$SpotVersion = "2.16"
$SpotUrl = "https://www.lre.epita.fr/dload/spot/spot-2.16.tar.gz"

$SpotDir = Join-Path $ProjectRoot "tools\spot"
$SpotArchive = Join-Path $SpotDir "downloads\spot-$SpotVersion.tar.gz"
$SpotSource = Join-Path $SpotDir "src\spot-$SpotVersion"
$SpotHome = Join-Path $SpotDir "install"

# Limit parallelism so the Spot source build does not overwhelm
# smaller Windows machines. Override with $env:MAKE_JOBS if desired.
$MakeJobs = if ($env:MAKE_JOBS) { $env:MAKE_JOBS } else { "2" }

# Maven

$MavenVersion = "3.10.0"

$MavenDir = Join-Path $ProjectRoot "tools\maven"
$MavenArchive = Join-Path $MavenDir "downloads\apache-maven-$MavenVersion-bin.zip"
$MavenHome = Join-Path $MavenDir "install"
$MavenInstallDir = Join-Path $MavenHome "apache-maven-$MavenVersion"
$MavenBin = Join-Path $MavenInstallDir "bin"
$MavenExecutable = Join-Path $MavenBin "mvn.cmd"

$MavenUrl = "https://dlcdn.apache.org/maven/maven-3/$MavenVersion/binaries/apache-maven-$MavenVersion-bin.zip"

# MSYS2

$Msys2Home = if ($env:MSYS2_HOME) {
    $env:MSYS2_HOME
}
else {
    "C:\msys64"
}

$Msys2Bash = Join-Path $Msys2Home "usr\bin\bash.exe"
$Msys2UsrBin = Join-Path $Msys2Home "usr\bin"
$Msys2Ucrt = Join-Path $Msys2Home "ucrt64\bin"

# Utility functions

function Say([string]$Text) {
    Write-Host $Text
}

function Fail([string]$Text) {
    Write-Error $Text
    exit 1
}

function Has-Command([string]$Name) {
    return $null -ne (Get-Command $Name -ErrorAction SilentlyContinue)
}

function Refresh-Path {
    $machinePath = [Environment]::GetEnvironmentVariable("Path", "Machine")
    $userPath = [Environment]::GetEnvironmentVariable("Path", "User")

    $parts = @()

    if ($machinePath) {
        $parts += $machinePath
    }

    if ($userPath) {
        $parts += $userPath
    }

    $env:Path = $parts -join ";"
}

# Java detection

function Get-JavaMajorFromHome([string]$JavaHome) {
    if ([string]::IsNullOrWhiteSpace($JavaHome)) {
        return 0
    }

    $javaExe = Join-Path $JavaHome "bin\java.exe"
    $javacExe = Join-Path $JavaHome "bin\javac.exe"

    if (-not (Test-Path $javaExe)) {
        return 0
    }

    if (-not (Test-Path $javacExe)) {
        return 0
    }

    $javaOutput = (
    cmd.exe /c "`"$javaExe`" -version 2>&1"
    ) | Out-String

    $match = [regex]::Match(
            $javaOutput,
            'version\s+"([0-9]+)'
    )

    if (-not $match.Success) {
        return 0
    }

    return [int]$match.Groups[1].Value
}

function Get-JavaHomeFromCommand([string]$CommandName) {
    $command = Get-Command $CommandName -ErrorAction SilentlyContinue

    if ($null -eq $command) {
        return $null
    }

    $source = $command.Source

    if ([string]::IsNullOrWhiteSpace($source)) {
        return $null
    }

    $binDir = Split-Path -Parent $source
    $detectedJavaHome = Split-Path -Parent $binDir

    if ((Test-Path (Join-Path $detectedJavaHome "bin\java.exe")) -and
            (Test-Path (Join-Path $detectedJavaHome "bin\javac.exe"))) {
        return $detectedJavaHome
    }

    return $null
}

function Get-UsableJavaHome {
    $candidates = @()

    if ($env:JAVA_HOME) {
        $candidates += $env:JAVA_HOME
    }

    $javacHome = Get-JavaHomeFromCommand "javac"
    if ($javacHome) {
        $candidates += $javacHome
    }

    $javaHome = Get-JavaHomeFromCommand "java"
    if ($javaHome) {
        $candidates += $javaHome
    }

    foreach ($candidate in $candidates) {
        if ([string]::IsNullOrWhiteSpace($candidate)) {
            continue
        }

        $fullCandidate = [IO.Path]::GetFullPath($candidate)
        $major = Get-JavaMajorFromHome $fullCandidate

        if ($major -ge $JavaMinMajor) {
            return $fullCandidate
        }
    }

    return $null
}

function Test-Java21Plus {
    return $null -ne (Get-UsableJavaHome)
}

function Select-Java21Plus {
    $javaHome = Get-UsableJavaHome

    if ($null -eq $javaHome) {
        return $null
    }

    $env:JAVA_HOME = $javaHome

    $javaBin = Join-Path $javaHome "bin"

    if ($env:Path -notlike "*$javaBin*") {
        $env:Path = "$javaBin;$env:Path"
    }

    return $javaHome
}

# User PATH management

function Add-UserPath([string]$Path) {
    if (-not (Test-Path $Path)) {
        return
    }

    $currentUserPath = [Environment]::GetEnvironmentVariable("Path", "User")

    $parts = @(
    $currentUserPath -split ';' |
            Where-Object {
                $_ -and $_.Trim()
            }
    )

    $alreadyPresent = $false

    foreach ($part in $parts) {
        if ($part.TrimEnd('\') -ieq $Path.TrimEnd('\')) {
            $alreadyPresent = $true
            break
        }
    }

    if (-not $alreadyPresent) {
        $parts += $Path

        [Environment]::SetEnvironmentVariable(
                "Path",
                ($parts -join ";"),
                "User"
        )
    }

    $currentParts = @(
    $env:Path -split ';' |
            Where-Object {
                $_ -and $_.Trim()
            }
    )

    $currentPresent = $false

    foreach ($part in $currentParts) {
        if ($part.TrimEnd('\') -ieq $Path.TrimEnd('\')) {
            $currentPresent = $true
            break
        }
    }

    if (-not $currentPresent) {
        $env:Path = "$Path;$env:Path"
    }
}

# WinGet

function Install-Winget([string]$Id, [string]$Name) {
    if (-not (Has-Command "winget")) {
        Fail "winget was not found. Install the missing dependency manually."
    }

    Say ""
    Say "Installing $Name using WinGet..."
    Say "Package: $Id"

    winget source update

    winget install `
        --id $Id `
        --exact `
        --accept-source-agreements `
        --accept-package-agreements

    if ($LASTEXITCODE -ne 0) {
        Fail "WinGet failed while installing $Name ($Id)."
    }

    Refresh-Path
}

function Ask-Install-Winget([string]$Id, [string]$Name) {
    $answer = Read-Host "Missing dependency: $Name. Install it with WinGet? [Y/n]"

    if ([string]::IsNullOrWhiteSpace($answer)) {
        $answer = "Y"
    }

    if ($answer -notmatch '^[Yy]$') {
        Fail "Setup cancelled."
    }

    Install-Winget $Id $Name
}

# Maven

function Get-MavenExecutable {
    if (Test-Path $MavenExecutable) {
        return $MavenExecutable
    }

    $globalMaven = Get-Command "mvn.cmd" -ErrorAction SilentlyContinue

    if ($null -ne $globalMaven) {
        return $globalMaven.Source
    }

    return $null
}

function Maven-Installed {
    return $null -ne (Get-MavenExecutable)
}

function Install-Maven {
    New-Item `
        -ItemType Directory `
        -Force `
        -Path `
            (Join-Path $MavenDir "downloads"), `
            $MavenHome |
            Out-Null

    if (-not (Test-Path $MavenArchive)) {
        Say ""
        Say "Downloading Apache Maven $MavenVersion..."

        Invoke-WebRequest `
            -Uri $MavenUrl `
            -OutFile $MavenArchive
    }
    else {
        Say ""
        Say "Maven archive already exists."
        Say "Reusing: $MavenArchive"
    }

    if (-not (Test-Path $MavenExecutable)) {
        Say ""
        Say "Extracting Apache Maven $MavenVersion..."

        Expand-Archive `
            -Path $MavenArchive `
            -DestinationPath $MavenHome `
            -Force
    }

    if (-not (Test-Path $MavenExecutable)) {
        Fail "Maven installation failed. Expected executable: $MavenExecutable"
    }

    Add-UserPath $MavenBin

    $env:MAVEN_HOME = $MavenInstallDir

    Say ""
    Say "Apache Maven $MavenVersion installed."
    Say "Maven home: $MavenInstallDir"
}

function Install-Maven-Interactively {
    $answer = Read-Host `
        "Missing dependency: Maven. Download Apache Maven $MavenVersion into the project? [Y/n]"

    if ([string]::IsNullOrWhiteSpace($answer)) {
        $answer = "Y"
    }

    if ($answer -notmatch '^[Yy]$') {
        Fail "Setup cancelled."
    }

    Install-Maven
}

# MSYS2 tool checks

function Test-MsysTool([string]$RelativePath) {
    return Test-Path (Join-Path $Msys2Home $RelativePath)
}

# MSYS2 programs are not necessarily Windows .exe files.
# In particular, autoconf, automake, and libtool are commonly
# shell wrappers installed as files without a .exe extension.
# Therefore, check them from inside the MSYS2 environment using
# command -v instead of checking Windows file paths.
function Test-MsysCommand([string]$Name) {
    if (-not (Test-Path $Msys2Bash)) {
        return $false
    }

    $command = @"
export MSYSTEM=UCRT64
export CHERE_INVOKING=1
export PATH=/ucrt64/bin:/usr/bin:`$PATH
command -v '$Name' >/dev/null 2>&1
"@

    & $Msys2Bash -lc $command

    return $LASTEXITCODE -eq 0
}

function Test-MsysBuildTools {
    return (
    (Test-MsysCommand "g++") -and
            (Test-MsysCommand "pkg-config") -and
            (Test-MsysCommand "make") -and
            (Test-MsysCommand "autoconf") -and
            (Test-MsysCommand "automake") -and
            (Test-MsysCommand "libtool") -and
            (Test-MsysCommand "curl") -and
            (Test-MsysCommand "tar")
    )
}

function Install-MsysPackage([string]$Package) {
    $command = @"
export MSYSTEM=UCRT64
export CHERE_INVOKING=1
export PATH=/ucrt64/bin:/usr/bin:`$PATH
pacman -S --needed --noconfirm $Package
"@

    & $Msys2Bash -lc $command

    if ($LASTEXITCODE -ne 0) {
        Fail "MSYS2 failed while installing package: $Package"
    }
}

function Install-MsysMissingPackages {
    if (-not (Test-Path $Msys2Bash)) {
        Fail "MSYS2 bash was not found at $Msys2Bash"
    }

    Say ""
    Say "Checking required MSYS2 packages..."

    # Install only the specific packages that are actually missing.
    # Do not install the full UCRT64 toolchain group, because existing
    # *-git packages can conflict with that meta-package.

    if (-not (Test-MsysCommand "g++")) {
        Say "Missing: UCRT64 C++ compiler"
        Install-MsysPackage "mingw-w64-ucrt-x86_64-gcc"
    }

    if (-not (Test-MsysCommand "pkg-config")) {
        Say "Missing: UCRT64 pkg-config"
        Install-MsysPackage "mingw-w64-ucrt-x86_64-pkgconf"
    }

    if (-not (Test-MsysCommand "make")) {
        Say "Missing: MSYS make"
        Install-MsysPackage "make"
    }

    if (-not (Test-MsysCommand "autoconf")) {
        Say "Missing: autoconf"
        Install-MsysPackage "autoconf"
    }

    if (-not (Test-MsysCommand "automake")) {
        Say "Missing: automake"
        Install-MsysPackage "automake"
    }

    if (-not (Test-MsysCommand "libtool")) {
        Say "Missing: libtool"
        Install-MsysPackage "libtool"
    }

    if (-not (Test-MsysCommand "curl")) {
        Say "Missing: curl"
        Install-MsysPackage "curl"
    }

    if (-not (Test-MsysCommand "tar")) {
        Say "Missing: tar"
        Install-MsysPackage "tar"
    }

    if (-not (Test-MsysBuildTools)) {
        Fail "Required MSYS2 build tools are still missing."
    }
}

# General dependency check

function Check-Tools {
    $missing = @()

    if (-not (Test-Java21Plus)) {
        $missing += "Java 21+ JDK"
    }

    if (-not (Maven-Installed)) {
        $missing += "Maven"
    }

    if (-not (Has-Command "cmake")) {
        $missing += "CMake"
    }

    if (-not (Has-Command "ninja")) {
        $missing += "Ninja"
    }

    if (-not (Test-Path $Msys2Bash)) {
        $missing += "MSYS2"
    }
    elseif (-not (Test-MsysBuildTools)) {
        $missing += "MSYS2 build tools"
    }

    if ($missing.Count -eq 0) {
        return $true
    }

    Say ""
    Say "Missing dependencies:"

    foreach ($item in $missing) {
        Say "  - $item"
    }

    return $false
}

# Install dependencies

function Install-Tools {
    if (-not (Has-Command "winget")) {
        Fail "winget is required for automatic Windows dependency installation."
    }

    # Java
    # Microsoft documents Microsoft.OpenJDK.21 as a Windows WinGet package.
    # We only install it when no Java 21+ JDK is already available.
    if (-not (Test-Java21Plus)) {
        Ask-Install-Winget `
            "Microsoft.OpenJDK.21" `
            "Java 21 JDK"

        Refresh-Path

        if (-not (Test-Java21Plus)) {
            Fail "Java 21+ is still not available after installation."
        }
    }

    # Maven
    if (-not (Maven-Installed)) {
        Install-Maven-Interactively
    }

    # CMake
    if (-not (Has-Command "cmake")) {
        Ask-Install-Winget `
            "Kitware.CMake" `
            "CMake"
    }

    # Ninja
    if (-not (Has-Command "ninja")) {
        Ask-Install-Winget `
            "Ninja-build.Ninja" `
            "Ninja"
    }

    # MSYS2
    if (-not (Test-Path $Msys2Bash)) {
        Ask-Install-Winget `
            "MSYS2.MSYS2" `
            "MSYS2"
    }

    if (-not (Test-Path $Msys2Bash)) {
        Fail "MSYS2 bash was not found at $Msys2Bash"
    }

    Refresh-Path

    # Make MSYS2 tools available in this PowerShell process.
    if (Test-Path $Msys2Ucrt) {
        Add-UserPath $Msys2Ucrt
    }

    if (Test-Path $Msys2UsrBin) {
        Add-UserPath $Msys2UsrBin
    }

    $env:MSYS2_HOME = $Msys2Home

    Install-MsysMissingPackages
}

# Spot checks

function Spot-Installed {
    $header = Join-Path $SpotHome "include\spot\tl\parse.hh"
    $pcFile = Join-Path $SpotHome "lib\pkgconfig\libspot.pc"

    return (
    (Test-Path $header) -and
            (Test-Path $pcFile)
    )
}

# Build Spot

function Build-Spot {
    New-Item `
        -ItemType Directory `
        -Force `
        -Path `
            (Join-Path $SpotDir "downloads"), `
            (Join-Path $SpotDir "src") |
            Out-Null

    # Download

    if (-not (Test-Path $SpotArchive)) {
        Say ""
        Say "Downloading Spot $SpotVersion..."

        Invoke-WebRequest `
            -Uri $SpotUrl `
            -OutFile $SpotArchive
    }
    else {
        Say ""
        Say "Spot archive already exists."
        Say "Reusing: $SpotArchive"
    }

    # Extract

    if (-not (Test-Path $SpotSource)) {
        Say ""
        Say "Extracting Spot $SpotVersion..."

        tar -xzf `
            $SpotArchive `
            -C (Join-Path $SpotDir "src")

        if ($LASTEXITCODE -ne 0) {
            Fail "Failed to extract Spot $SpotVersion."
        }
    }
    else {
        Say ""
        Say "Spot source already exists."
        Say "Reusing: $SpotSource"
    }

    if (-not (Test-Path $SpotSource)) {
        Fail "Spot source directory was not found after extraction: $SpotSource"
    }

    # Remove incomplete installation

    if (Test-Path $SpotHome) {
        Say ""
        Say "Removing previous incomplete Spot installation..."

        Remove-Item -Recurse -Force $SpotHome
    }

    New-Item `
        -ItemType Directory `
        -Force `
        -Path $SpotHome |
            Out-Null

    # Convert Windows paths to MSYS2 paths

    $srcPosix = (
    & $Msys2Bash -lc `
            "cygpath -u '$($SpotSource -replace '\\','/')'"
    ).Trim()

    $dstPosix = (
    & $Msys2Bash -lc `
            "cygpath -u '$($SpotHome -replace '\\','/')'"
    ).Trim()

    if ([string]::IsNullOrWhiteSpace($srcPosix)) {
        Fail "Could not convert Spot source path to an MSYS2 path."
    }

    if ([string]::IsNullOrWhiteSpace($dstPosix)) {
        Fail "Could not convert Spot install path to an MSYS2 path."
    }

    Say ""
    Say "Building Spot $SpotVersion using MSYS2 UCRT64..."
    Say "Build parallelism: $MakeJobs"

    $buildCommand = @"
export MSYSTEM=UCRT64
export CHERE_INVOKING=1
export PATH=/ucrt64/bin:/usr/bin:`$PATH

cd '$srcPosix'

./configure \
    --prefix='$dstPosix' \
    --disable-python

make -j$MakeJobs
make install
"@

    & $Msys2Bash -lc $buildCommand

    if ($LASTEXITCODE -ne 0) {
        Fail "Spot build failed."
    }

    if (-not (Spot-Installed)) {
        Fail `
            "Spot build completed, but the expected installation files were not found under $SpotHome"
    }

    Say ""
    Say "Spot $SpotVersion installed successfully."
}

# Persist environment

function Persist-Environment {
    $selectedJavaHome = Select-Java21Plus

    if ($null -eq $selectedJavaHome) {
        Fail "Could not select a usable Java 21+ JDK."
    }

    [Environment]::SetEnvironmentVariable(
            "JAVA_HOME",
            $selectedJavaHome,
            "User"
    )

    [Environment]::SetEnvironmentVariable(
            "SPOT_HOME",
            $SpotHome,
            "User"
    )

    [Environment]::SetEnvironmentVariable(
            "MSYS2_HOME",
            $Msys2Home,
            "User"
    )

    if (Test-Path $MavenInstallDir) {
        [Environment]::SetEnvironmentVariable(
                "MAVEN_HOME",
                $MavenInstallDir,
                "User"
        )
    }

    Add-UserPath (Join-Path $selectedJavaHome "bin")
    Add-UserPath (Join-Path $SpotHome "bin")
    Add-UserPath $Msys2Ucrt

    if (Test-Path $Msys2UsrBin) {
        Add-UserPath $Msys2UsrBin
    }

    if (Test-Path $MavenBin) {
        Add-UserPath $MavenBin
    }

    $env:JAVA_HOME = $selectedJavaHome
    $env:SPOT_HOME = $SpotHome
    $env:MSYS2_HOME = $Msys2Home

    if (Test-Path $MavenInstallDir) {
        $env:MAVEN_HOME = $MavenInstallDir
    }
}

# Check-only mode

if ($Check) {
    Say "========================================"
    Say "Machine Decomposition Environment Check"
    Say "========================================"
    Say ""

    Refresh-Path

    $javaHome = Get-UsableJavaHome

    if ($null -ne $javaHome) {
        $javaMajor = Get-JavaMajorFromHome $javaHome
        Say "Java 21+    : OK (Java $javaMajor)"
        Say "JAVA_HOME  : $javaHome"
    }
    else {
        Say "Java 21+    : MISSING"
        Say "JAVA_HOME  : not detected"
    }

    if (Maven-Installed) {
        Say "Maven       : OK"
    }
    else {
        Say "Maven       : MISSING"
    }

    if (Has-Command "cmake") {
        Say "CMake       : OK"
    }
    else {
        Say "CMake       : MISSING"
    }

    if (Has-Command "ninja") {
        Say "Ninja       : OK"
    }
    else {
        Say "Ninja       : MISSING"
    }

    if (Test-Path $Msys2Bash) {
        Say "MSYS2       : OK"
    }
    else {
        Say "MSYS2       : MISSING"
    }

    if (Test-MsysCommand "g++") {
        Say "UCRT64 C++  : OK"
    }
    else {
        Say "UCRT64 C++  : MISSING"
    }

    if (Test-MsysCommand "make") {
        Say "MSYS make   : OK"
    }
    else {
        Say "MSYS make   : MISSING"
    }

    if (Test-MsysCommand "pkg-config") {
        Say "pkg-config  : OK"
    }
    else {
        Say "pkg-config  : MISSING"
    }

    if (Test-MsysCommand "autoconf") {
        Say "autoconf     : OK"
    }
    else {
        Say "autoconf     : MISSING"
    }

    if (Test-MsysCommand "automake") {
        Say "automake     : OK"
    }
    else {
        Say "automake     : MISSING"
    }

    if (Test-MsysCommand "libtool") {
        Say "libtool      : OK"
    }
    else {
        Say "libtool      : MISSING"
    }

    if (Test-MsysCommand "curl") {
        Say "curl         : OK"
    }
    else {
        Say "curl         : MISSING"
    }

    if (Test-MsysCommand "tar") {
        Say "tar          : OK"
    }
    else {
        Say "tar          : MISSING"
    }

    Say ""

    if (Spot-Installed) {
        Say "Spot $SpotVersion : OK"
    }
    else {
        Say "Spot $SpotVersion : MISSING"
    }

    Say ""
    Say "Expected SPOT_HOME:"
    Say "  $SpotHome"

    Say "Expected Maven (project-local):"
    Say "  $MavenInstallDir"

    exit 0
}

# Normal setup

Refresh-Path

if (-not (Check-Tools)) {
    Say ""

    $answer = Read-Host `
        "Install missing dependencies using WinGet/MSYS2? [Y/n]"

    if ([string]::IsNullOrWhiteSpace($answer)) {
        $answer = "Y"
    }

    if ($answer -notmatch '^[Yy]$') {
        Fail "Setup cancelled."
    }

    Install-Tools
}

Refresh-Path

if (-not (Check-Tools)) {
    Fail "Required tools are still missing."
}

# Select Java now so Maven/CMake invoked later use a JDK >= 21.
$selectedJavaHome = Select-Java21Plus

if ($null -eq $selectedJavaHome) {
    Fail "Could not select a usable Java 21+ JDK."
}

Say ""
Say "Using JDK: $selectedJavaHome"
Say "Java major: $(Get-JavaMajorFromHome $selectedJavaHome)"

if (-not (Spot-Installed)) {
    Build-Spot
}
else {
    Say "Spot $SpotVersion is already installed. Reusing it."
}

Persist-Environment

Say ""
Say "========================================"
Say "Setup completed successfully."
Say "========================================"
Say "JAVA_HOME : $env:JAVA_HOME"
Say "SPOT_HOME : $env:SPOT_HOME"
if ($env:MAVEN_HOME) {
    Say "MAVEN_HOME: $env:MAVEN_HOME"
}
Say ""
Say "Open a new PowerShell session before building the project."