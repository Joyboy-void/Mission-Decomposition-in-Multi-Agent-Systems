# `ltlf2ra-core`

This module contains the LTLf formula model and the algorithm for constructing a Residual Automaton.

The AST supports truth constants, atomic propositions, negation, next, eventually, always, conjunction, disjunction, and until. `AtomicPropositionSet`, `Alphabet`, and `Valuation` represent the proposition universe and its complete truth assignments.

`ResidualSemantics` defines the one-step residual operation. The default semantics use the residual visitor, while `Simplifier` and `BooleanCanonicalizer` normalize formulas before state registration. `FormulaEquivalenceChecker` is an interface, so the core does not require Spot directly.

`ResidualAutomatonBuilder` explores reachable residual formulas. For every discovered state and alphabet valuation it computes and normalizes the next residual, finds or registers the destination state, and records the transition. A state is accepting when its residual formula is `TrueFormula`.

The module does not parse files, render graphs, or launch external processes.
