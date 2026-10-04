# Architecture

The project is split into small modules so that the LTLf logic, Spot integration, application wiring, and CLI stay separate.

```text
cli
  -> ltlf2ra-app
  -> ltlf2ra-spot
  -> ltlf2ra-core
  -> automata-core
```

## Modules

| Module | Role |
|---|---|
| `automata-core` | Generic automaton abstractions and edge types. |
| `ltlf2ra-core` | LTLf formulas, alphabets, residual semantics, normalization, and residual automaton construction. |
| `ltlf2ra-spot` | Spot parser/equivalence support through JNI. |
| `ltlf2ra-app` | Wires the parser, residual builder, Spot equivalence, and output formatting into one application API. |
| `cli` | Small command-line frontend. |

## Design rules

`automata-core` does not know about LTLf.

`ltlf2ra-core` does not know about Spot. It depends on `FormulaParser` and `FormulaEquivalenceChecker` abstractions instead.

`ltlf2ra-spot` provides the concrete Spot implementations.

`ltlf2ra-app` owns the default wiring of the components.

`cli` only handles arguments, exit codes, and terminal output.

## Current flow

```text
formula string
    -> SpotFormulaParser
    -> Formula
    -> AtomicPropositionCollector
    -> AtomicPropositionSet / Alphabet
    -> ResidualAutomatonBuilder
    -> ResidualAutomaton
    -> ResidualAutomatonFormatter
    -> terminal
```

The builder explores reachable residual states with the supplied alphabet and uses normalization plus the supplied equivalence checker when registering states.

## Planned direction

The current repository stops at LTLf to Residual Automaton conversion. Future work can add `agent-model`, `product-automaton`, and `decomposition` without changing the existing layer boundaries.
