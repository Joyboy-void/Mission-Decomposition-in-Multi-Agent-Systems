# `ltlf2ra-core`

The core module implements LTLf formulas and construction of a Residual Automaton.

## Formula Model

Supported operators:

```text
⊤, ⊥
atomic propositions
¬
X
F
G
∧
∨
U
```

The AST uses `Formula` and the visitor pattern through `FormulaVisitor<T>`.

## Alphabet

`AtomicPropositionSet` stores the propositions of a formula in deterministic order.

`Valuation` represents one truth assignment.

`Alphabet` enumerates all valuations of the proposition set. Its size is:

```text
2^|AP|
```

## Residual Semantics

`ResidualSemantics` defines:

```java
residual(formula, valuation)
```

`DefaultResidualSemantics` delegates to `ResidualVisitor`.

Examples:

```text
Res(X φ, a) = φ

Res(F φ, a) = Res(φ, a) ∨ F φ

Res(G φ, a) = Res(φ, a) ∧ G φ

Res(φ U ψ, a)
  = Res(ψ, a) ∨ (Res(φ, a) ∧ (φ U ψ))
```

## Normalization

The default normalization pipeline is:

```text
Simplifier
    ↓
BooleanCanonicalizer
```

This removes local redundancies and gives equivalent Boolean structures a deterministic representation before state lookup.

## Residual States

A `ResidualState` contains:

- integer ID
- canonical residual formula

`ResidualStateRegistry` maps residual formulas to states.

Lookup first uses normalized structural representation and can then use `FormulaEquivalenceChecker` to merge semantically equivalent formulas.

## Automaton Construction

`ResidualAutomatonBuilder` performs a reachable-state exploration.

For every discovered state:

1. iterate over every alphabet valuation
2. compute the one-step residual
3. normalize the residual
4. find or create its state
5. add the transition
6. continue until no new state is discovered

The resulting automaton is deterministic and total over its alphabet.

A state is accepting iff its residual formula is `TrueFormula`.

## Independence from Spot

The module defines abstractions rather than choosing external implementations:

```java
FormulaParser
        FormulaEquivalenceChecker
```

This is the main boundary that keeps the residual algorithm independent from the Spot integration.
