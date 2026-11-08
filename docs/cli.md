# Command-line interface

The CLI exposes the intermediate automata and the complete decomposition workflow. Use the supplied launcher scripts so that Spot's native libraries and Graphviz are configured.

- Linux / WSL: `./scripts/run-cli.sh`
- Windows PowerShell: `.\scripts\run-cli.ps1`

Run `--help` to print the command summary.

## 1. Residual Automaton

```bash
./scripts/run-cli.sh --ra "F(p)"
./scripts/run-cli.sh --ra-dot "p | X(q & p)"
./scripts/run-cli.sh --ra-svg output/ra.svg "F G p"
./scripts/run-cli.sh --ra-png output/ra.png "F G p"
```

`--ra` prints the automaton, `--ra-dot` prints DOT source, and the image commands render the automaton.

## 2. DRes analysis

```bash
./scripts/run-cli.sh --dres "F(p & F(q))"
./scripts/run-cli.sh --dres-img-h "F(p & F(q))"
```

`--dres` computes and displays the DRes result. `--dres-img-h` creates a Residual Automaton visualization with DRes states highlighted. The image name and output location are selected by the application.

## 3. Agent model

```bash
./scripts/run-cli.sh --agent-svg output/agent.svg input/agent-model/agent1.txt
./scripts/run-cli.sh --agent-png output/agent.png input/agent-model/agent1.txt
```

The model file is the final argument.

## 4. Product Automaton

```bash
./scripts/run-cli.sh --product-svg output/product.svg input/agent-model/agent1.txt "F(p)"
./scripts/run-cli.sh --product-png output/product.png input/agent-model/agent1.txt "F(p)"
```

The application constructs the RA over `AP_formula ∪ AP_agent` before building the product. This is important when the model contains propositions that do not occur in the formula.

## 5. Team Automaton

```bash
./scripts/run-cli.sh --team-svg output/team.svg input/agent-model/agent1.txt "F(p)" 3
./scripts/run-cli.sh --team-png output/team.png input/agent-model/agent1.txt "F(p)" 3
```

The final argument is the number of homogeneous agents. It must be a positive integer.

## 6. Minimum-cost decomposition

```bash
./scripts/run-cli.sh --decom input/agent-model/agent1.txt "F(p & F(q))" 3
```

This runs the complete pipeline and prints the work assigned to each agent, the state sequence, valuation actions, per-agent costs, and total minimum cost. If no accepting Team Automaton path exists, the CLI reports that no valid decomposition exists.

## 7. Decomposition images

Export the intermediate automata and print the decomposition:

```bash
./scripts/run-cli.sh --decom-img input/agent-model/agent1.txt "F(p & F(q))" 3
```

Export the selected decomposition path highlighted on the Team Automaton:

```bash
./scripts/run-cli.sh --decom-img-h input/agent-model/agent1.txt "F(p & F(q))" 3
```

The intermediate export includes the Residual, Product, and Team Automata in SVG and PNG formats. The highlighted export is generated only when a decomposition exists. The implementation uses a deterministic run identifier derived from the model file contents, formula, and agent count to distinguish runs.

## 8. Argument and path rules

- Quote formulas containing spaces or shell-special characters.
- Quote paths containing spaces.
- Relative paths are resolved from the current working directory, not from the script directory.
- Absolute paths are supported.
- For commands that accept an output path, the application creates missing output directories when rendering.

## 9. Exit codes

| Exit code | Meaning |
|---|---|
| `0` | Command completed |
| `1` | Invalid command or arguments |
| `2` | Application, native integration, or rendering failure |

Use the launcher scripts rather than invoking `cli/target/cli.jar` directly unless the Spot and Graphviz runtime environment has already been configured.
