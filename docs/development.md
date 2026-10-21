# Development

## Toolchain

The project targets Java 21 and uses Maven. The Spot integration additionally requires CMake, Ninja, a C++ compiler, and a Spot installation. Graphviz is used for SVG and PNG output.

Run the environment setup and check:

```bash
./scripts/setup.sh
./scripts/setup.sh --check
```

On Windows:

```powershell
.\scripts\setup.ps1
.\scripts\setup.ps1 -Check
```

## Build and run

```bash
mvn clean test
mvn clean package
./scripts/run-cli.sh --help
./scripts/run-cli.sh --ra "F(p)"
```

On Windows use `.\scripts
un-cli.ps1` with the same command arguments. The packaged JAR is `cli/target/cli.jar`; the launchers are preferred because they configure native library paths.

## Native runtime

The RA application calls Spot through JNI. `SPOT_HOME` should identify the Spot installation. The launcher configures the native bridge and Spot libraries for the current platform. Graphviz is invoked as an external process; the shared renderer uses `dot` for ordinary graphs and `neato` for fixed-position product graphs.

## Module boundaries while developing

Keep formula and residual semantics in `ltlf2ra-core`, agent parsing and model types in `agent-model`, and product semantics in `product-automaton`. Each visualization module should only turn its domain object into DOT. Command parsing and application wiring stay in `cli`.

Useful targeted builds include:

```bash
mvn -pl agent-model-app -am test
mvn -pl product-automaton-app -am test
mvn -pl cli -am package
```
