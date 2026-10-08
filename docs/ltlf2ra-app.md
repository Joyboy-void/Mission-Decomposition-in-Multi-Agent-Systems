# `ltlf2ra-app`

The application module assembles the project into a usable LTLf-to-RA service.

## `Ltlf2RaService`

`build(String)`:

1. validates the input
2. parses the formula
3. collects atomic propositions
4. constructs the alphabet
5. creates the residual automaton builder
6. builds the reachable Residual Automaton

The default configuration uses:

- `SpotFormulaParser`
- `DefaultResidualSemantics`
- `Simplifier`
- `BooleanCanonicalizer`
- `SpotEquivalenceChecker`

Dependencies can also be supplied explicitly for tests or alternate implementations.

## `Ltlf2RaApplication`

The facade exposes four main operations:

```java
build(formula)
execute(formula)
exportDot(formula)
render(formula, format, output)
```

`execute` produces terminal text.

`exportDot` returns Graphviz DOT.

`render` produces an image or other Graphviz-supported format.

The application layer contains orchestration, not residual semantics.
