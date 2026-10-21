$ErrorActionPreference = "Stop"

# Use UTF-8 for console output so Unicode LTL symbols
# such as ∧, ∨, ⊤, and ⊥ are displayed correctly.
chcp 65001 > $null


# User-facing help

if (
$args.Count -eq 0 -or
        $args[0] -eq "--help" -or
        $args[0] -eq "-h"
) {

    Write-Host @"
Usage:
  .\scripts\run-cli.ps1 --ra "<LTLf formula>"
  .\scripts\run-cli.ps1 --ra-dot "<LTLf formula>"
  .\scripts\run-cli.ps1 --ra-svg <output.svg> "<LTLf formula>"
  .\scripts\run-cli.ps1 --ra-png <output.png> "<LTLf formula>"
  .\scripts\run-cli.ps1 --agent-svg <output.svg> <agent-model-file>
  .\scripts\run-cli.ps1 --agent-png <output.png> <agent-model-file>
  .\scripts\run-cli.ps1 --product-svg <output.svg> <agent-model-file> "<LTLf formula>"
  .\scripts\run-cli.ps1 --product-png <output.png> <agent-model-file> "<LTLf formula>"

Examples:
   .\scripts\run-cli.ps1 --ra "F(p)"
   .\scripts\run-cli.ps1 --ra-dot "p | X(q & p)"
   .\scripts\run-cli.ps1 --ra-svg output/automaton/1.svg "p | X(q & p)"
   .\scripts\run-cli.ps1 --ra-png output/automaton/1.png "p | X(q & p)"
  .\scripts\run-cli.ps1 --agent-svg output/agent.svg examples/agent-models/example.txt
  .\scripts\run-cli.ps1 --agent-png output/agent.png examples/agent-models/example.txt
  .\scripts\run-cli.ps1 --product-svg output/product.svg examples/agent-models/example.txt "F(p)"
  .\scripts\run-cli.ps1 --product-png output/product.png examples/agent-models/example.txt "F(p)"

Paths to input model files and output images may be relative or absolute. Quote paths that contain spaces.
"@

    exit 0
}


# Paths

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

$GraphvizBin = Join-Path `
    $ProjectRoot `
    "tools\graphviz\install\bin"


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
"$Native;$SpotBin;$UcrtBin;$GraphvizBin;$env:Path"


# Run CLI

java `
    "-Djava.library.path=$Native" `
    -jar `
    $Jar `
    @args

exit $LASTEXITCODE