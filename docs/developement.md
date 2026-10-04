# Development notes

## Requirements

The project currently targets Java 23 and uses Maven for the multi-module build.

The Spot module also needs:

- CMake 3.20 or newer
- Ninja
- a C++ compiler
- a Spot installation
- `SPOT_HOME` pointing to that installation

On Windows, the current PowerShell launcher also expects the MSYS2 UCRT64 runtime used by the native bridge.

## Build

From the repository root:

```text
mvn clean test
```

For the complete packaged build:

```text
mvn clean package
```

When using IntelliJ, the bundled Maven works as well.

## Run the CLI

After packaging:

```text
./run-cli.ps1 "F G p"
```

or:

```text
./run-cli.sh "F G p"
```

`SPOT_HOME` should already be set in the environment.

## Native build layout

The Spot module generates JNI headers under its Maven `target` directory and places the native bridge under:

```text
ltlf2ra-spot/target/native-build/native
```

The CLI JAR is written to:

```text
cli/target/cli.jar
```
