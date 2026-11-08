# Machine Decomposition

A modular Java 21 implementation for decomposing an LTLf task specification among a fixed number of homogeneous agents. The project constructs a Residual Automaton (RA), combines it with a weighted agent model, identifies decomposition-responsible residual states (DRes), builds an implicit Team Automaton, and finds a minimum-cost accepting execution.

The command-line interface also exposes the intermediate automata and DRes analysis independently.

## 1. What the project does

Given:

- an **LTLf formula** describing the task or mission;
- an **agent model** describing the states, capabilities, and transition costs of one agent;
- a positive integer **number of agents**;

the application searches for a least-cost way to distribute the required work across the agents, in order, while preserving the formula's finite-trace semantics.

At a high level:

```text
LTLf formula ──> Residual Automaton ───────────────┐
                                                   ├─> Product Automaton
Agent model ───────────────────────────────────────┘
                         │
                         ├─> DRes analysis
                         │
                         └─> Team Automaton (n copies)
                                      │
                                      └─> Dijkstra
                                             │
                                             └─> Minimum-cost decomposition
```

The Team Automaton is represented implicitly: it reuses Product Automaton states and creates transitions when requested, rather than eagerly copying the entire product graph `n` times.

## 2. Main capabilities

- Parse supported LTLf formulas through the Spot integration.
- Construct reachable Residual Automata using formula residuals, normalization, and semantic equivalence.
- Parse weighted agent models from a small text format.
- Construct Product Automata over the combined formula/agent atomic-proposition universe.
- Find DRes states using synchronized product-state BFS.
- Construct an implicit Team Automaton for a requested number of homogeneous agents.
- Find a minimum-cost accepting path using Dijkstra's algorithm.
- Print the decomposition as per-agent work segments.
- Export RA, agent-model, Product Automaton, Team Automaton, and highlighted decomposition graphs to SVG/PNG.
- Run on Windows and Linux/WSL using the supplied setup and launcher scripts.

## 3. Requirements

| Requirement | Purpose |
|---|---|
| Java 21 | Main implementation |
| Maven | Multi-module build |
| Spot | Formula parsing and semantic equivalence through JNI |
| CMake, Ninja, C++ compiler | Build the native Spot bridge |
| Graphviz | Render DOT as SVG/PNG |

The setup scripts help configure and check the development environment. See [Development](docs/development.md).

## 4. Setup and build

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

For normal execution, use the launcher scripts. They configure the native-library and Graphviz paths expected by the application.

```bash
./scripts/run-cli.sh --help
```

```powershell
.\scripts\run-cli.ps1 --help
```

## 5. Quick start

Run a decomposition for three homogeneous agents:

```bash
./scripts/run-cli.sh --decom input/agent-model/agent1.txt "F(p & F(q))" 3
```

Print the optimal decomposition without rendering images:

```bash
./scripts/run-cli.sh --decom input/agent-model/agent1.txt "F(p & F(q))" 3
```

Export all intermediate automata and print the decomposition:

```bash
./scripts/run-cli.sh --decom-img input/agent-model/agent1.txt "F(p & F(q))" 3
```

Export the Team Automaton with the selected minimum-cost path highlighted:

```bash
./scripts/run-cli.sh --decom-img-h input/agent-model/agent1.txt "F(p & F(q))" 3
```

On Windows, replace `./scripts/run-cli.sh` with `.\scripts\run-cli.ps1`. Quote formulas and paths containing spaces.

## 6. CLI reference

| Command | Purpose |
|---|---|
| `--help` | Show usage |
| `--ra "<formula>"` | Build and print a Residual Automaton |
| `--ra-dot "<formula>"` | Print RA DOT source |
| `--ra-svg <output.svg> "<formula>"` | Render an RA as SVG |
| `--ra-png <output.png> "<formula>"` | Render an RA as PNG |
| `--dres "<formula>"` | Compute and print DRes analysis |
| `--dres-img-h "<formula>"` | Render the RA with DRes states highlighted |
| `--agent-svg <output.svg> <model-file>` | Render an agent model as SVG |
| `--agent-png <output.png> <model-file>` | Render an agent model as PNG |
| `--product-svg <output.svg> <model-file> "<formula>"` | Render the Product Automaton as SVG |
| `--product-png <output.png> <model-file> "<formula>"` | Render the Product Automaton as PNG |
| `--team-svg <output.svg> <model-file> "<formula>" <n>` | Render the Team Automaton as SVG |
| `--team-png <output.png> <model-file> "<formula>" <n>` | Render the Team Automaton as PNG |
| `--decom <model-file> "<formula>" <n>` | Find and print a minimum-cost decomposition |
| `--decom-img <model-file> "<formula>" <n>` | Run decomposition and export intermediate automata |
| `--decom-img-h <model-file> "<formula>" <n>` | Run decomposition and export the selected path highlighted |

The exact argument order matters. For decomposition commands, the number of agents must be an integer greater than or equal to one. Input and output paths are interpreted relative to the current working directory unless absolute paths are supplied.

For detailed examples and output locations, see [CLI usage](docs/cli.md).

## 7. Agent-model input

```text
initial: A

states:
  A: p
  B: q
  C: p q
  D: -

edges:
  A B 4
  A C 2
  C D 3
  B D 1
```

- `initial:` lists one or more initial state IDs.
- Each state line gives a state ID and the propositions true in that state.
- `-` represents an empty proposition set.
- Each edge is `source destination non-negative-cost`.
- Blank lines and lines beginning with `#` are ignored.

See [Agent-model format](docs/agent-model-format.md) for validation rules.

## 8. Architecture and documentation

The repository is a Maven reactor. The CLI delegates to application modules; domain modules own models and algorithms; exporter modules produce DOT; `graphviz-core` invokes Graphviz.

| Document | Contents |
|---|---|
| [Architecture](docs/Architecture.md) | End-to-end architecture, module dependencies, and data flow |
| [CLI](docs/cli.md) | Commands, arguments, outputs, and exit codes |
| [Agent-model format](docs/agent-model-format.md) | Input grammar and validation |
| [Agent model](docs/agent-model.md) | Agent model types and semantics |
| [Automata core](docs/automata-core.md) | Generic automaton abstractions |
| [LTLf-to-RA core](docs/ltlf2ra-core.md) | Formula representation and residual construction |
| [Spot integration](docs/ltlf2ra-spot.md) | Parsing, equivalence, JNI, and native build |
| [RA application](docs/ltlf2ra-app.md) | RA workflow orchestration |
| [RA exporter](docs/ltlf2ra-graphviz.md) | Residual Automaton DOT representation |
| [Product Automaton](docs/product-automaton.md) | Product semantics and AP compatibility |
| [DRes analysis](docs/dres-analysis.md) | Definition, synchronized BFS, correctness, and complexity |
| [Team Automaton](docs/team-automaton.md) | Copies, epsilon switches, and implicit representation |
| [Decomposition algorithm](docs/decomposition.md) | Dijkstra search and result reconstruction |
| [Visualization](docs/visualization.md) | Exporters, layout engines, and highlighted paths |
| [Development](docs/development.md) | Build, test, native setup, and contribution boundaries |

## 9. Important semantic conventions

### Atomic propositions

The Product Automaton must account for every proposition appearing either in the formula or in the agent model:

```text
AP_product = AP_formula ∪ AP_agent
```

A valuation is a complete assignment over that universe. An agent-state label lists the propositions that are true; every proposition omitted from the label is false.

### Costs

Agent-model edges carry non-negative integer costs. These costs are retained by Product Automaton transitions and lifted into ordinary Team Automaton transitions. Epsilon switches between consecutive agent copies have cost zero. Dijkstra therefore operates on non-negative edge weights.

### Finite traces

The specification is LTLf: satisfaction is evaluated over finite traces. A residual state is accepting when its residual formula is `true`.

## 10. Scope and design boundaries

- Formula semantics and RA construction belong to `ltlf2ra-core`.
- Spot-specific behavior is isolated in `ltlf2ra-spot`.
- Agent-model parsing is independent of formula and rendering logic.
- Product and Team Automaton modules contain their respective construction semantics.
- Decomposition search is separate from CLI formatting and image export.
- Exporters generate DOT; the shared renderer executes Graphviz.
- The CLI wires the workflows together but does not implement the algorithms.
