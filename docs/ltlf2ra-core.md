# LTLf-to-Residual-Automaton core

`ltlf2ra-core` contains the formula representation and the algorithm for constructing a Residual Automaton (RA).

## Formula model

The AST represents the supported LTLf operators and atoms, including truth constants, atomic propositions, negation, next, eventually, always, conjunction, disjunction, and until.

`AtomicPropositionSet`, `Alphabet`, and `Valuation` represent the proposition universe and its valuations. The alphabet enumerates the possible truth assignments over the selected proposition set.

## Residual semantics

`ResidualSemantics` defines the one-step residual operation: given a formula and the current valuation, it computes the formula that must hold over the remaining suffix for the original formula to be satisfied.

The default implementation uses the residual visitor. Formula simplification and Boolean canonicalization normalize formulas before state registration. `FormulaEquivalenceChecker` is an abstraction, so the core does not directly depend on Spot.

## Residual Automaton construction

`ResidualAutomatonBuilder` performs reachable-state exploration:

1. Register the initial formula as the initial residual state.
2. Remove a discovered state from the exploration queue.
3. For each valuation in the alphabet, compute the next residual formula.
4. Normalize the result and check whether an equivalent residual state is already registered.
5. Register a new state when necessary and add the transition.
6. Mark a state accepting when its residual formula is `TrueFormula`.

The result is a finite automaton representing the finite-trace behavior of the input formula.

## Boundary

This module owns formula and residual semantics. Parsing formula text, calling Spot, rendering, and command-line handling are provided by other modules.
