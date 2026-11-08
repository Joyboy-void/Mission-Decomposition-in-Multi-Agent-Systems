#!/usr/bin/env sh

set -eu

# User-facing help

if [ "$#" -eq 0 ] || [ "$1" = "--help" ] || [ "$1" = "-h" ]; then
    cat <<'EOF'

Machine Decomposition CLI
=========================

A command-line interface for constructing, analyzing, visualizing,
and decomposing LTLf-based multi-agent systems.

USAGE
-----

./scripts/run-cli.sh <option> [arguments]


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
  ./scripts/run-cli.sh --ra "F(p)"
  ./scripts/run-cli.sh --ra-dot "p | X(q & p)"
  ./scripts/run-cli.sh --ra-svg output/automaton/ra.svg "F(p)"
  ./scripts/run-cli.sh --ra-png output/automaton/ra.png "F(p)"

DRes:
  ./scripts/run-cli.sh --dres "F p"
  ./scripts/run-cli.sh --dres-img-h "((a & !b) & X(b & !a)) | ((!a & b) & X(a & !b))"

Agent Automaton:
  ./scripts/run-cli.sh --agent-svg output/automaton/agent.svg input/agent-model/agent1.txt
  ./scripts/run-cli.sh --agent-png output/automaton/agent.png input/agent-model/agent1.txt

Product Automaton:
  ./scripts/run-cli.sh --product-svg output/automaton/product.svg input/agent-model/agent1.txt "F(p)"
  ./scripts/run-cli.sh --product-png output/automaton/product.png input/agent-model/agent1.txt "F(p)"

Team Automaton:
  ./scripts/run-cli.sh --team-svg output/automaton/team.svg input/agent-model/agent1.txt "F a & F b" 3
  ./scripts/run-cli.sh --team-png output/automaton/team.png input/agent-model/agent1.txt "F a & F b" 3

Task Decomposition:
  ./scripts/run-cli.sh --decom input/agent-model/agent1.txt "F p" 2
  ./scripts/run-cli.sh --decom-img input/agent-model/agent1.txt "F p" 2
  ./scripts/run-cli.sh --decom-img-h input/agent-model/agent1.txt "F p" 2


NOTES
-----

- LTLf formulas should be enclosed in double quotes.
- Input model files and output image paths can be relative or absolute.
- Quote paths containing spaces.
- Run ./scripts/setup.sh to configure the project dependencies.

EOF
    exit 0
fi

# Paths

PROJECT_ROOT="$(CDPATH= cd -- "$(dirname -- "$0")/.." && pwd)"

JAR="$PROJECT_ROOT/cli/target/cli.jar"

NATIVE="$PROJECT_ROOT/ltlf2ra-spot/target/native-build/native"

GRAPHVIZ_BIN="$PROJECT_ROOT/tools/graphviz/install/bin"


# Graphviz

if [ -d "$GRAPHVIZ_BIN" ]; then
    export PATH="$GRAPHVIZ_BIN:$PATH"
fi


# Checks

if [ -z "${SPOT_HOME:-}" ]; then
    echo "Error: SPOT_HOME is not set."
    echo "Run ./scripts/setup.sh first."
    exit 1
fi


if [ ! -f "$JAR" ]; then
    echo "Error: CLI JAR not found:"
    echo "  $JAR"
    echo "Run: mvn clean package"
    exit 1
fi


if [ ! -d "$NATIVE" ]; then
    echo "Error: native library directory not found:"
    echo "  $NATIVE"
    echo "Run: mvn clean package"
    exit 1
fi

# Platform-specific native library paths
case "$(uname -s)" in
    Linux*)
        export LD_LIBRARY_PATH="$NATIVE:$SPOT_HOME/lib:${LD_LIBRARY_PATH:-}"
        ;;

    Darwin*)
        export DYLD_LIBRARY_PATH="$NATIVE:$SPOT_HOME/lib:${DYLD_LIBRARY_PATH:-}"
        ;;

    *)
        echo "Error: unsupported Unix platform."
        exit 1
        ;;
esac

# Run CLI

exec java \
    "-Djava.library.path=$NATIVE" \
    -jar "$JAR" \
    "$@"