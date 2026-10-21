# Command-line interface

The CLI provides access to the RA, agent-model, and product-automaton workflows. Use `scripts/run-cli.sh` on Linux/WSL/macOS and `scripts/run-cli.ps1` on Windows. The scripts configure the runtime paths needed by Spot's JNI bridge and Graphviz.

## Commands

### Residual Automaton

```bash
./scripts/run-cli.sh --ra "F(p)"a
./scripts/run-cli.sh --ra-dot "p | X(q & p)"
./scripts/run-cli.sh --ra-svg output/ra.svg "F G p"
./scripts/run-cli.sh --ra-png output/ra.png "F G p"
```

`--ra` prints the formatted automaton. `--ra-dot` writes DOT to standard output. The SVG and PNG variants render to the requested file.

### Agent model

```bash
./scripts/run-cli.sh --agent-svg output/agent.svg examples/agent-models/example.txt
./scripts/run-cli.sh --agent-png output/agent.png examples/agent-models/example.txt
```

### Product automaton

```bash
./scripts/run-cli.sh --product-svg output/product.svg examples/agent-models/example.txt "F(p)"
./scripts/run-cli.sh --product-png output/product.png examples/agent-models/example.txt "F(p)"
```

The product command takes an output path, an agent-model file, and an LTLf formula. The RA is built over the union of the formula and agent-model atomic propositions before the product is constructed.

On Windows, replace `./scripts/run-cli.sh` with `.\scripts
un-cli.ps1`.

## Paths and formulas

Input and output paths may be relative to the current working directory or absolute. Quote paths containing spaces. Quoting the complete formula is recommended; the CLI joins the remaining formula arguments after the command's fixed arguments.

## Errors and exit codes

- `--help` prints usage.
- Invalid command or arguments return exit code `1`.
- Application and rendering failures return exit code `2`.
- SVG/PNG rendering creates missing output directories.

Use the launchers rather than invoking the JAR directly unless you have configured the Spot and Graphviz runtime environment yourself.
