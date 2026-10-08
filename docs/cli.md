# `cli`

The CLI is the user-facing entry point.

## Usage

```text
./scripts/run-cli.sh "<LTLf formula>"
./scripts/run-cli.sh --dot "<LTLf formula>"
./scripts/run-cli.sh --svg <output.svg> "<LTLf formula>"
./scripts/run-cli.sh --png <output.png> "<LTLf formula>"
```

On Windows:

```text
.\scripts\run-cli.ps1 "<LTLf formula>"
```

## Examples

```bash
./scripts/run-cli.sh "F G p"

./scripts/run-cli.sh --dot "p | X(q & p)"

./scripts/run-cli.sh \
  --svg output/automaton/example.svg \
  "p | X(q & p)"

./scripts/run-cli.sh \
  --png output/automaton/example.png \
  "p | X(q & p)"
```

Formula arguments are joined automatically, so quoting the complete formula is recommended but not required by the parser wrapper.

Atomic propositions are inferred from the formula.

## Modes

| Mode | Result |
|---|---|
| default | formatted Residual Automaton |
| `--dot` | DOT source |
| `--svg` | rendered SVG file |
| `--png` | rendered PNG file |

## Exit Codes

| Code | Meaning |
|---:|---|
| `0` | success / help |
| `1` | invalid CLI usage |
| `2` | runtime/application error |

## Launcher Responsibility

The scripts do more than invoke Java. They configure the native runtime:

```text
Spot JNI library
Spot libraries
Graphviz
MSYS2 UCRT64   (Windows)
```

For this reason, the launcher scripts are preferred over invoking `java -jar cli/target/cli.jar` directly.
