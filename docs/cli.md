# `cli`

The CLI is deliberately thin. It does not know how Spot or the residual automaton builder works.

## `Main`

Creates a `CommandLineApplication`, passes `System.in`/`System.out`/`System.err` equivalents through the application boundary, and exits with the returned status when necessary.

## `CommandLineApplication`

Handles:

- missing arguments
- `--help`
- joining formula arguments into one formula string
- calling `Ltlf2RaApplication`
- printing the result
- mapping failures to an exit code

Exit codes:

| Code | Meaning |
|---:|---|
| `0` | Success or help message |
| `1` | Invalid command-line usage |
| `2` | Application/runtime error while processing the formula |

## Example

```text
./run-cli.sh "F G p"
```

or on Windows PowerShell:

```text
./run-cli.ps1 "F G p"
```

The CLI accepts a formula only; atomic propositions are discovered automatically.
