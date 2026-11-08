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

Machine Decomposition CLI
=========================

A command-line interface for constructing, analyzing, visualizing,
and decomposing LTLf-based multi-agent systems.

USAGE
-----

.\scripts\run-cli.ps1 <option> [arguments]


1. RESIDUAL AUTOMATON (RA)
--------------------------

Construct and inspect the Residual Automaton of an LTLf formula.

  --ra <formula>
      Construct and display the Residual Automaton in the terminal.

  --ra-dot <formula>
      Generate and display the Residual Automaton in Graphviz DOT format.

  --ra-svg <output.svg> <formula>
      Generate and save the Residual Automaton as an SVG image.

  --ra-png <output.png> <formula>
      Generate and save the Residual Automaton as a PNG image.


2. DECOMPOSITION RESIDUES (DRes)
--------------------------------

Identify and visualize decomposition-related states in the
Residual Automaton.

  --dres <formula>
      Compute and display the DRes states in the terminal.

  --dres-img-h <formula>
      Generate a highlighted visualization of the Residual Automaton,
      showing the identified DRes states.


3. AGENT AUTOMATON
------------------

Load and visualize an individual agent's transition system.

  --agent-svg <output.svg> <agent-model-file>
      Generate and save the Agent Automaton as an SVG image.

  --agent-png <output.png> <agent-model-file>
      Generate and save the Agent Automaton as a PNG image.


4. PRODUCT AUTOMATON
--------------------

Construct the synchronized product of an Agent Automaton and
a Residual Automaton.

  --product-svg <output.svg> <agent-model-file> <formula>
      Generate and save the Product Automaton as an SVG image.

  --product-png <output.png> <agent-model-file> <formula>
      Generate and save the Product Automaton as a PNG image.


5. TEAM AUTOMATON
-----------------

Construct and visualize the Team Automaton for a specified
number of agents.

  --team-svg <output.svg> <agent-model-file> <formula> <number-of-agents>
      Generate and save the Team Automaton as an SVG image.

  --team-png <output.png> <agent-model-file> <formula> <number-of-agents>
      Generate and save the Team Automaton as a PNG image.


6. TASK DECOMPOSITION
---------------------

Perform task decomposition using an Agent Model and an LTLf formula.

  --decom <agent-model-file> <formula> <number-of-agents>
      Compute and display the decomposition results in the terminal.

  --decom-img <agent-model-file> <formula> <number-of-agents>
      Compute the decomposition and generate its visualization.

  --decom-img-h <agent-model-file> <formula> <number-of-agents>
      Compute the decomposition and generate a highlighted visualization.


EXAMPLES
--------

Residual Automaton:
  .\scripts\run-cli.ps1 --ra "F(p)"
  .\scripts\run-cli.ps1 --ra-dot "p | X(q & p)"
  .\scripts\run-cli.ps1 --ra-svg output\automaton\ra.svg "F(p)"
  .\scripts\run-cli.ps1 --ra-png output\automaton\ra.png "F(p)"

DRes:
  .\scripts\run-cli.ps1 --dres "F p"
  .\scripts\run-cli.ps1 --dres-img-h "((a & !b) & X(b & !a)) | ((!a & b) & X(a & !b))"

Agent Automaton:
  .\scripts\run-cli.ps1 --agent-svg output\automaton\agent.svg input\agent-model\agent1.txt
  .\scripts\run-cli.ps1 --agent-png output\automaton\agent.png input\agent-model\agent1.txt

Product Automaton:
  .\scripts\run-cli.ps1 --product-svg output\automaton\product.svg input\agent-model\agent1.txt "F(p)"
  .\scripts\run-cli.ps1 --product-png output\automaton\product.png input\agent-model\agent1.txt "F(p)"

Team Automaton:
  .\scripts\run-cli.ps1 --team-svg output\automaton\team.svg input\agent-model\agent1.txt "F a & F b" 3
  .\scripts\run-cli.ps1 --team-png output\automaton\team.png input\agent-model\agent1.txt "F a & F b" 3

Task Decomposition:
  .\scripts\run-cli.ps1 --decom input\agent-model\agent1.txt "F p" 2
  .\scripts\run-cli.ps1 --decom-img input\agent-model\agent1.txt "F p" 2
  .\scripts\run-cli.ps1 --decom-img-h input\agent-model\agent1.txt "F p" 2


NOTES
-----

- LTLf formulas should be enclosed in double quotes.
- Input model files and output image paths can be relative or absolute.
- Quote paths containing spaces.
- Run .\scripts\setup.ps1 to configure the project dependencies.

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