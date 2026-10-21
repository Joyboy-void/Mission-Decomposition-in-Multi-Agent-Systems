# Machine Decomposition

A modular Java 21 project for constructing LTLf Residual Automata (RA), representing weighted agent models, and building product automata between the two. Automata can be printed in the terminal or exported through Graphviz to DOT, SVG, and PNG.

The project is split into Maven modules so that formula semantics, agent-model data, product construction, rendering, and command-line handling remain separate.

## Current capabilities

- Parse LTLf formulas through the Spot integration.
- Build reachable residual automata with formula normalization and semantic equivalence checking.
- Read weighted agent models from a plain-text format.
- Construct product automata over the combined atomic-proposition universe of the formula and agent model.
- Export RA, agent-model, and product-automaton graphs.
- Build and run the project on Windows and Linux/WSL using the supplied scripts.

## Setup and build

The project targets Java 21. The RA parser/equivalence integration uses Spot through JNI, and image output requires Graphviz.

Linux / WSL:

```bash
./scripts/setup.sh
./scripts/setup.sh --check
mvn clean test
mvn clean package
```

Windows PowerShell:

```powershell
.\scripts\setup.ps1
.\scripts\setup.ps1 -Check
mvn clean test
mvn clean package
```

The executable JAR is `cli/target/cli.jar`. For normal use, prefer the launcher scripts because they configure the native library paths and Graphviz environment.

## CLI examples

```bash
# Residual automaton
./scripts/run-cli.sh --ra "F(p)"
./scripts/run-cli.sh --ra-dot "p | X(q & p)"
./scripts/run-cli.sh --ra-svg output/ra.svg "F G p"
./scripts/run-cli.sh --ra-png output/ra.png "F G p"

# Agent model
./scripts/run-cli.sh --agent-svg output/agent.svg examples/agent-models/example.txt
./scripts/run-cli.sh --agent-png output/agent.png examples/agent-models/example.txt

# Product automaton
./scripts/run-cli.sh --product-svg output/product.svg examples/agent-models/example.txt "F(p)"
./scripts/run-cli.sh --product-png output/product.png examples/agent-models/example.txt "F(p)"
```

On Windows, use `.\scripts\run-cli.ps1` with the same options. Paths are resolved from the current working directory unless absolute; quote paths containing spaces.

## Agent-model format

```text
initial: A C

states:
  A: p q
  B: q r
  C: p
  D: -

edges:
  A B 5
  B D 2
  C D 3
```

`initial:` lists one or more initial states. Under `states:`, each state is followed by the propositions true at that state; `-` means an empty label. Each `edges:` line gives source, destination, and a non-negative integer cost. Blank lines and `#` comments are allowed. See [Agent-model input format](docs/agent-model-format.md).

## Modules

| Module | Responsibility |
|---|---|
| `automata-core` | Generic automaton and weighted-transition interfaces |
| `ltlf2ra-core` | LTLf AST, alphabet, residual semantics, transformations, and RA construction |
| `ltlf2ra-spot` | Spot parsing, formula printing, equivalence checking, and JNI |
| `ltlf2ra-graphviz` | RA-specific DOT export |
| `ltlf2ra-app` | RA build, formatting, DOT export, and rendering workflow |
| `agent-model` | Agent states, labels, weighted transitions, and text parser |
| `agent-model-graphviz` | Agent-model DOT export |
| `agent-model-app` | Agent loading and visualization workflow |
| `product-automaton` | Product states, construction, compatibility, acceptance, and costs |
| `product-automaton-graphviz` | Product DOT export and fixed-grid layout |
| `product-automaton-app` | Coordinates loading, RA construction, product construction, and rendering |
| `graphviz-core` | Shared DOT exporter contract, executable resolution, and rendering |
| `cli` | Command parsing, application wiring, and exit codes |

## Documentation

- [Architecture and module boundaries](docs/Architecture.md)
- [CLI usage](docs/cli.md)
- [Agent-model input format](docs/agent-model-format.md)
- [Generic automata abstractions](docs/automata-core.md)
- [LTLf-to-RA core](docs/ltlf2ra-core.md)
- [Spot integration](docs/ltlf2ra-spot.md)
- [RA application](docs/ltlf2ra-app.md)
- [RA Graphviz exporter](docs/ltlf2ra-graphviz.md)
- [Agent model](docs/agent-model.md)
- [Product automaton](docs/product-automaton.md)
- [Shared Graphviz infrastructure](docs/graphviz-core.md)
- [Development workflow](docs/development.md)

For the full construction flow and AP-universe semantics, start with [Architecture](docs/Architecture.md).
