# Development

## 1. Toolchain

The project targets Java 21 and uses Maven. The Spot integration adds native build requirements:

- Spot installation;
- CMake and Ninja;
- a compatible C++ compiler.

Graphviz is required for SVG and PNG rendering.

## 2. Setup and validation

Linux / WSL:

```bash
./scripts/setup.sh
./scripts/setup.sh --check
```

Windows PowerShell:

```powershell
.\scripts\setup.ps1
.\scripts\setup.ps1 -Check
```

The check mode reports whether the required tools and native dependencies can be found.

## 3. Build and test

```bash
mvn clean test
mvn clean package
```

Use the Maven reactor from the repository root so inter-module dependencies are built in the correct order.

Useful targeted builds:

```bash
mvn -pl agent-model-app -am test
mvn -pl product-automaton-app -am test
mvn -pl decomposition-app -am test
mvn -pl cli -am package
```

## 4. Run

```bash
./scripts/run-cli.sh --help
./scripts/run-cli.sh --ra "F(p)"
```

On Windows:

```powershell
.\scripts\run-cli.ps1 --help
.\scripts\run-cli.ps1 --ra "F(p)"
```

Prefer the launchers over direct JAR execution because they configure the native Spot and Graphviz runtime paths.

## 5. Native runtime

The RA application calls Spot through JNI. `SPOT_HOME` should point to the Spot installation. The launcher configures the native bridge and Spot libraries for the current platform.

Graphviz is invoked as an external process. The shared renderer uses `dot` for ordinary graphs and `neato` for fixed-position graph layouts.

## 6. Module boundaries

When extending the project, keep responsibilities in their owning modules:

- formula syntax and residual semantics: `ltlf2ra-core`;
- Spot-specific parsing/equivalence/JNI: `ltlf2ra-spot`;
- agent model and file parsing: `agent-model`;
- product semantics: `product-automaton`;
- DRes: the LTLf analysis package;
- Team Automaton: `team-automaton`;
- shortest-path search and result: `decomposition`;
- orchestration and presentation: application modules;
- DOT conversion: domain Graphviz modules;
- process execution: `graphviz-core`;
- argument parsing and dependency wiring: `cli`.

Keep domain algorithms independent from terminal formatting and image rendering.
