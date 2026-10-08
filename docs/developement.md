# Development

## Toolchain

The Maven project is compiled with:

```text
Java 21
```

The native Spot/Graphviz workflow additionally uses:

```text
Maven
CMake
Ninja
C++ compiler
pkg-config
```

The setup scripts manage the project-local Spot and Graphviz installations.

## Recommended Workflow

```bash
./scripts/setup.sh --check
./scripts/setup.sh

mvn clean test
mvn clean package

./scripts/run-cli.sh "F G p"
```

On Windows, use the corresponding `.ps1` scripts.

## Build Artifacts

Important generated paths:

```text
cli/target/cli.jar
ltlf2ra-spot/target/native-build/native/
tools/spot/
tools/graphviz/
```

`tools/` contains local dependency installations and should not be treated as source code.

## Graphviz Development

The Graphviz module has no Java Graphviz dependency. It invokes the external `dot` executable.

For deterministic project-local behavior:

```text
GRAPHVIZ_HOME=<project>/tools/graphviz/install
```

or:

```text
GRAPHVIZ_DOT=<path-to-dot>
```

The latter takes precedence.

## Native Development

The Spot module uses:

```text
Java → JNI → C++ → Spot
```

The CMake configuration obtains Spot from `SPOT_HOME`, avoiding hard-coded machine paths.

## Tests

Run all tests:

```bash
mvn clean test
```

Graphviz DOT generation is tested independently of the external Graphviz executable, which keeps the exporter test fast and deterministic.

## Scope of the Current Version

The implemented pipeline ends at:

```text
LTLf
  ↓
Residual Automaton
  ↓
DOT / SVG / PNG
```

Machine decomposition stages beyond this boundary are not part of the current implementation.
