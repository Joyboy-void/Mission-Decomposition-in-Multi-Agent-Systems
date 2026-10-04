$ErrorActionPreference = "Stop"

$ProjectRoot = Split-Path -Parent $PSScriptRoot

$Jar = Join-Path `
    $ProjectRoot `
    "cli\target\cli.jar"

$Native = Join-Path `
    $ProjectRoot `
    "ltlf2ra-spot\target\native-build\native"

$SpotHome = $env:SPOT_HOME

$Msys2Home = if ($env:MSYS2_HOME) {
    $env:MSYS2_HOME
}
else {
    "C:\msys64"
}

$SpotBin = Join-Path $SpotHome "bin"
$UcrtBin = Join-Path $Msys2Home "ucrt64\bin"


# Checks

if ([string]::IsNullOrWhiteSpace($SpotHome)) {

    Write-Error `
        "SPOT_HOME is not set. Run .\scripts\setup.ps1 first."

    exit 1
}


if (-not (Test-Path $Jar)) {

    Write-Error `
        "CLI JAR not found: $Jar"

    Write-Error `
        "Run: mvn clean package"

    exit 1
}


if (-not (Test-Path $Native)) {

    Write-Error `
        "Native library directory not found: $Native"

    Write-Error `
        "Run: mvn clean package"

    exit 1
}


if (-not (Test-Path $SpotBin)) {

    Write-Error `
        "Spot bin directory not found: $SpotBin"

    exit 1
}


if (-not (Test-Path $UcrtBin)) {

    Write-Error `
        "MSYS2 UCRT64 directory not found: $UcrtBin"

    Write-Error `
        "Run .\scripts\setup.ps1 first."

    exit 1
}


# Temporary runtime environment

$env:Path =
"$Native;$SpotBin;$UcrtBin;$env:Path"


# Run CLI

java `
    "-Djava.library.path=$Native" `
    -jar `
    $Jar `
    @args

exit $LASTEXITCODE