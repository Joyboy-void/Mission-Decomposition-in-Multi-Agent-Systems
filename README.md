# Machine Decomposition

A modular Java project for building LTLf Residual Automata and, later, using them for machine decomposition.

The repository currently implements the LTLf-to-Residual-Automaton pipeline. The agent model, product automaton, DRES, and decomposition stages are planned as future modules.

## Project Structure

```text
machine-decomposition/
├── automata-core/
├── ltlf2ra-core/
├── ltlf2ra-spot/
├── ltlf2ra-app/
├── cli/
│
├── docs/
│
├── scripts/
│   ├── setup.ps1
│   ├── setup.sh
│   ├── run-cli.ps1
│   └── run-cli.sh
│
├── tools/
│   └── spot/
│       ├── downloads/
│       ├── src/
│       └── install/
│
├── .gitignore
├── pom.xml
└── README.md
```

The `tools/spot/` directory is created locally by the setup scripts and is ignored by Git.

## Module Responsibilities

### `automata-core`

Contains generic automaton abstractions and transition types.

This module is independent of LTLf and Spot so that the same automaton infrastructure can be reused by later modules.

### `ltlf2ra-core`

Contains the LTLf-specific implementation:

- LTLf formula AST
- Atomic propositions
- Valuations and alphabets
- Formula transformations and normalization
- Residual semantics
- Residual-state representation
- Residual-state registry
- Residual Automaton construction

`ltlf2ra-core` does not depend on Spot.

### `ltlf2ra-spot`

Contains the Spot integration used by the LTLf pipeline:

- Spot-backed formula parsing
- Spot-based semantic equivalence checking
- Formula printing
- JNI bridge between Java and Spot's native C++ library

Spot-specific functionality is isolated in this module.

### `ltlf2ra-app`

Contains the application-level service that wires together parsing, residual construction, and semantic equivalence checking for the complete LTLf-to-RA workflow.

### `cli`

Contains the command-line entry point.

The CLI accepts an LTLf formula, automatically discovers its atomic propositions, constructs the Residual Automaton, and prints the result.

More detailed module information is available in the [architecture documentation](docs/architecture.md).

## Architecture

The dependency direction is:

```text
cli
  ↓
ltlf2ra-app
  ↓
ltlf2ra-spot
  ↓
ltlf2ra-core
  ↓
automata-core
```

The main architectural rule is that `ltlf2ra-core` must not depend directly on Spot.

Instead, external functionality is accessed through abstractions such as:

```text
FormulaParser
FormulaEquivalenceChecker
```

This keeps the core residual-automaton implementation independent of the concrete formula parser or equivalence-checking backend.

## How the Residual Automaton Is Built

At a high level, the pipeline is:

```text
LTLf formula
      │
      ▼
    Parse
      │
      ▼
Collect atomic propositions
      │
      ▼
Create alphabet
      │
      ▼
Compute one-step residuals
      │
      ▼
Normalize residual formulas
      │
      ▼
Merge semantically equivalent residuals
      │
      ▼
Explore reachable residual states
      │
      ▼
Construct Residual Automaton
```

The automaton uses valuations as transition labels.

The initial state corresponds to the original LTLf formula.

A state is accepting when its residual formula is `⊤`.

Semantic equivalence is used when determining whether two residual formulas represent the same state.

## Requirements

The project requires:

- Java 23
- Maven
- CMake 3.20+
- Ninja
- A C++20 compiler
- pkg-config
- Spot 2.16

Spot is used through JNI by the native integration module.

The setup scripts handle the external dependency setup so that a global Spot installation is not required.

## Spot Setup

The project uses a project-local installation of Spot.

The setup scripts download the pinned Spot source distribution and build it under:

```text
tools/spot/install
```

The source archive is downloaded from:

```text
https://www.lre.epita.fr/dload/spot/spot-2.16.tar.gz
```


The setup script normally configures `SPOT_HOME` automatically, so it should not need to be set manually.

An existing global Spot installation is not required by the project.

## Setup Scripts

The repository provides separate setup scripts for Windows and Unix-like systems.

The setup process is intended to be idempotent. If the required dependencies and project-local Spot installation already exist, the script reuses them instead of rebuilding everything unnecessarily.

### Windows

Run PowerShell from the repository root:

```powershell
.\scripts\setup.ps1
```

The script:

1. Checks the required development tools.
2. Asks before installing missing dependencies.
3. Uses `winget` for Windows development tools when required.
4. Installs the MSYS2 UCRT64 C++ toolchain.
5. Downloads Spot if it is not already present.
6. Builds Spot from source using the MSYS2 UCRT64 environment.
7. Installs Spot under `tools/spot/install`.
8. Sets the user-level `SPOT_HOME` environment variable.

### Check the Windows Environment

To check the environment without performing installation:

```powershell
.\scripts\setup.ps1 -Check
```

This checks the required tools and whether the project-local Spot installation exists.

### Linux / macOS

Run:

```bash
./scripts/setup.sh
```

The script:

1. Checks the required development tools.
2. Detects the available package manager.
3. Asks before installing missing dependencies.
4. Downloads Spot when necessary.
5. Builds Spot from source.
6. Installs Spot under `tools/spot/install`.
7. Configures `SPOT_HOME`.

### Check the Unix Environment

To check the environment without installing anything:

```bash
./scripts/setup.sh --check
```

## Build

Build and run all tests from the repository root:

```text
mvn clean test
```

To package the project:

```text
mvn clean package
```

The project can also be built using IntelliJ IDEA's bundled Maven.

The native Spot bridge is built automatically as part of the Maven build.

## Native Integration

The Spot integration uses JNI:

```text
Java
  │
  ▼
JNI
  │
  ▼
SpotNativeBridge.cpp
  │
  ▼
Spot
```

The native bridge is built using CMake.

The generated native library is placed under:

```text
ltlf2ra-spot/target/native-build/native/
```

The CMake configuration obtains the Spot installation from `SPOT_HOME` rather than using a machine-specific path.

This allows the project to be moved to another directory or another machine without modifying the source code.

## Run the CLI

Because Spot is a native library accessed through JNI, the native runtime libraries must be available when the CLI starts.

The provided launcher scripts configure the required runtime paths automatically.

### Windows PowerShell

```powershell
.\scripts\run-cli.ps1 "F G p"
```

### Linux / macOS

```bash
./scripts/run-cli.sh "F G p"
```

The launcher scripts are preferred over running the JAR directly because they configure the native Spot runtime environment.

## CLI Input

The CLI accepts the LTLf formula directly.

Atomic propositions are discovered automatically, so they do not need to be supplied separately.

For example:

```text
F G p
```

is sufficient.

## Example

Run:

```powershell
.\scripts\run-cli.ps1 "F G p"
```

Example output:

```text
Residual Automaton
==============================

Number of states : 1
Alphabet Size : 2
Atomic Propositions : [p]

States :
-------------------
q0[accepting] : F(G(p))

Transitions :
-------------------------
q0--{ }-->q0
q0--{p }-->q0
```

The production implementation uses semantic equivalence checking through Spot, allowing semantically equivalent residual formulas to be represented by the same automaton state.

## Tests

All modules are built and tested from the root Maven reactor:

```text
mvn clean test
```

The test suites cover the different layers of the project.

### Core tests

The core tests cover:

- LTLf AST construction
- Formula transformations
- Alphabets and valuations
- Residual semantics
- Normalization
- Residual-state registration
- Residual Automaton construction

### Spot tests

The Spot module tests cover:

- Native library loading
- Formula parsing
- Semantic equivalence checking
- Integration with residual automaton construction

### Application tests

The application tests cover the complete formula-to-automaton service.

## Documentation

Detailed documentation is available in the `docs/` directory:

- [Architecture](docs/architecture.md)
- [Automata Core](docs/automata-core.md)
- [LTLf to RA Core](docs/ltlf2ra-core.md)
- [Spot Integration](docs/ltlf2ra-spot.md)
- [Application Layer](docs/ltlf2ra-app.md)
- [CLI](docs/cli.md)
- [Development](docs/development.md)

## Development Workflow

A typical development workflow is:

```text
Clone repository
      │
      ▼
Run setup script
      │
      ▼
Check environment
      │
      ▼
Run tests
      │
      ▼
Build project
      │
      ▼
Run CLI
```

### Windows

```powershell
.\scripts\setup.ps1 -Check
.\scripts\setup.ps1
mvn clean test
mvn clean package
.\scripts\run-cli.ps1 "F G p"
```

### Linux / macOS

```bash
./scripts/setup.sh --check
./scripts/setup.sh
mvn clean test
mvn clean package
./scripts/run-cli.sh "F G p"
```

## Generated and Local Files

The following files and directories are generated locally and should not be committed:

```text
target/
tools/spot/
```

In particular, `tools/spot/` contains the local Spot source archive, source tree, and compiled installation.

This keeps the repository lightweight and allows each developer to build the native dependency for their own platform.

## Current Status

### Implemented

- Generic automata core
- LTLf AST
- Formula transformations
- Finite-trace residual semantics
- Residual Automaton construction
- Residual-state registry
- Spot formula parser
- Spot semantic equivalence checker
- JNI bridge
- Application service
- Executable shaded CLI JAR
- Windows setup script
- Linux/macOS setup script
- CLI runtime launcher scripts
- Project-local Spot installation workflow

### Planned

- Agent model
- Product automaton
- Replicated product automata
- DRES computation
- Augmented automaton construction
- Reachability algorithms
- Shortest-path based decomposition
- Projection-based machine decomposition

## Long-Term Pipeline

The intended overall pipeline is:

```text
LTLf Machine Specification
          │
          ▼
   Residual Automaton
          │
          ▼
      Agent Model
          │
          ▼
    Product Automaton
          │
          ▼
 Replicated Product Systems
          │
          ▼
         DRES
          │
          ▼
 Augmented Automaton
          │
          ▼
 Reachability / Shortest Path
          │
          ▼
      Projection
          │
          ▼
  Machine Decomposition
```

The current repository implements the first stage of this pipeline while keeping the architecture open for the later stages.