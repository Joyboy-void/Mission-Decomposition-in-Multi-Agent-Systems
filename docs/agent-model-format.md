# Agent-model input format

`AgentModelParser`, in the `agent-model` module, reads a plain-text representation of an agent model.

## Example

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

## Initial states

The `initial:` declaration appears once, before `states:`, and lists one or more initial state IDs separated by whitespace. Each initial state must be declared in the state section.

## States and labels

Each line in `states:` declares a state and its label. The label lists the atomic propositions true at that state. Use `-` for an empty label; it means no proposition is true there.

State IDs can contain letters, digits, underscores, and hyphens, and must begin with a letter or underscore. Proposition names follow the same rule except that hyphens are not allowed.

## Edges

Each line in `edges:` has three fields:

```text
source destination cost
```

Both endpoints must be declared states. Costs are non-negative integers and are retained in the product transitions.

## Whitespace, comments, and validation

Blank lines and comments beginning with `#` are ignored. The required section order is `initial:`, `states:`, then `edges:`. The parser reports malformed sections, duplicate declarations, invalid identifiers, repeated propositions, undeclared states, malformed edges, and invalid costs with the source file and line when available.

## Paths

The parser accepts a `Path`. Relative paths are resolved against the process working directory from which the launcher is invoked, not against the script's own directory. Absolute paths are supported as well.
