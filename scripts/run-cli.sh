#!/usr/bin/env sh

set -eu

PROJECT_ROOT="$(CDPATH= cd -- "$(dirname -- "$0")/.." && pwd)"

JAR="$PROJECT_ROOT/cli/target/cli.jar"
NATIVE="$PROJECT_ROOT/ltlf2ra-spot/target/native-build/native"

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

exec java \
    "-Djava.library.path=$NATIVE" \
    -jar "$JAR" \
    "$@"