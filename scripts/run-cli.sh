#!/usr/bin/env sh

set -eu

# User-facing help

if [ "$#" -eq 0 ] || [ "$1" = "--help" ] || [ "$1" = "-h" ]; then
    cat <<'EOF'
Usage:
  ./scripts/run-cli.sh --ra "<LTLf formula>"
  ./scripts/run-cli.sh --ra-dot "<LTLf formula>"
  ./scripts/run-cli.sh --ra-svg <output.svg> "<LTLf formula>"
  ./scripts/run-cli.sh --ra-png <output.png> "<LTLf formula>"
  ./scripts/run-cli.sh --dres "<LTLf formula>"
  ./scripts/run-cli.sh --agent-svg <output.svg> <agent-model-file>
  ./scripts/run-cli.sh --agent-png <output.png> <agent-model-file>
  ./scripts/run-cli.sh --product-svg <output.svg> <agent-model-file> "<LTLf formula>"
  ./scripts/run-cli.sh --product-png <output.png> <agent-model-file> "<LTLf formula>"

Examples:
  ./scripts/run-cli.sh --ra "F(p)"
  ./scripts/run-cli.sh --ra-dot "p | X(q & p)"
  ./scripts/run-cli.sh --ra-svg output/automaton/1.svg "p | X(q & p)"
  ./scripts/run-cli.sh --ra-png output/automaton/1.png "p | X(q & p)"
  ./scripts/run-cli.sh --dres "F p"
  ./scripts/run-cli.sh --agent-svg output/agent.svg examples/agent-models/example.txt
  ./scripts/run-cli.sh --agent-png output/agent.png examples/agent-models/example.txt
  ./scripts/run-cli.sh --product-svg output/product.svg examples/agent-models/example.txt "F(p)"
  ./scripts/run-cli.sh --product-png output/product.png examples/agent-models/example.txt "F(p)"

Paths to input model files and output images may be relative or absolute. Quote paths that contain spaces.
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