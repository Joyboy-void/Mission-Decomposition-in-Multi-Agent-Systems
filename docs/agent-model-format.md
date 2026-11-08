# Agent-model input format

The `agent-model` module reads a plain-text description of one agent's finite-state model. The model is reused to represent each homogeneous agent in the Team Automaton.

## 1. Example

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

## 2. Sections

The file contains three sections in this order.

### Initial states

```text
initial: A C
```

Declare one or more initial state identifiers, separated by whitespace. Every initial state must also appear in the `states:` section.

### States and labels

```text
states:
  A: p q
  B: q r
  C: p
  D: -
```

Each line declares a state and the propositions true at that state. A proposition omitted from the label is false. Use `-` for an empty label.

### Edges

```text
edges:
  A B 5
  B D 2
```

Each edge contains exactly three fields:

```text
source destination cost
```

The endpoints must be declared states. Costs are non-negative integers.

## 3. Lexical rules and validation

- Blank lines are ignored.
- A comment begins with `#`; comment lines are ignored.
- Section order is `initial:`, `states:`, then `edges:`.
- The initial declaration appears once.
- State identifiers may contain letters, digits, underscores, and hyphens, and must begin with a letter or underscore.
- Proposition identifiers follow the same general rule, but hyphens are not allowed.
- Duplicate declarations, repeated propositions, undeclared states, malformed edges, invalid identifiers, and invalid costs are rejected.
- Parser errors include the source file and line number when available.

## 4. Paths

The parser accepts a `Path`. Relative paths are resolved against the process working directory from which the launcher was invoked. They are not resolved relative to the launcher's own directory.

## 5. Semantics

A state's label is interpreted as a set of true atomic propositions. If the label is `{p, q}`, then `p` and `q` are true and every other proposition in the product universe is false at that state. The edge cost is carried into the Product Automaton and contributes to the decomposition cost.
