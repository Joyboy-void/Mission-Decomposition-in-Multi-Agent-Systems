# Architecture

## Overview

The project is organized as small Maven modules with explicit responsibilities.

```text
cli
 ↓
ltlf2ra-app
 ├── ltlf2ra-core
 │    └── automata-core
 ├── ltlf2ra-spot
 └── ltlf2ra-graphviz
```

## Boundaries

### `automata-core`

Generic automaton abstractions. It contains no LTLf-specific semantics.

### `ltlf2ra-core`

Owns the domain model and algorithm:

- formula AST
- atomic propositions
- valuations and alphabets
- transformations
- residual semantics
- residual states
- Residual Automaton construction

It depends only on generic automata abstractions and its own interfaces.

### `ltlf2ra-spot`

Provides concrete external implementations for parsing and semantic equivalence.

### `ltlf2ra-graphviz`

Provides visualization without making the core automaton depend on Graphviz.

### `ltlf2ra-app`

Wires the parser, semantics, normalization, equivalence checker and presentation components.

### `cli`

Provides the user-facing process and exit-code handling.

## Main Data Flow

```text
formula text
    │
    ▼
FormulaParser
    │
    ▼
Formula AST
    │
    ├── AtomicPropositionCollector
    │
    ▼
AtomicPropositionSet
    │
    ▼
Alphabet
    │
    ▼
ResidualAutomatonBuilder
    │
    ├── ResidualSemantics
    ├── Normalizer
    └── FormulaEquivalenceChecker
    │
    ▼
ResidualAutomaton
    │
    ├── Formatter
    ├── DOT exporter
    └── Graphviz renderer
```

## Design Principle

External dependencies are accessed through interfaces. In particular:

```java
FormulaParser
        FormulaEquivalenceChecker
ResidualSemantics
```

This keeps the algorithm testable and prevents Spot or Graphviz from becoming part of the LTLf core.
