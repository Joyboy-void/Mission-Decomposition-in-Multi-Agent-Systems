# `ltlf2ra-core`

This module contains the LTLf model and the residual automaton algorithm. It has no dependency on Spot.

## Formula model

The AST supports:

`TrueFormula`, `FalseFormula`, atomic propositions, `Not`, `Next`, `Eventually`, `Always`, `And`, `Or`, and `Until`.

All formulas implement `Formula` and support the visitor pattern through `FormulaVisitor<T>`.

### AST helpers

- `UnaryFormula` stores one operand for `Not`, `Next`, `Eventually`, and `Always`.
- `BinaryFormula` stores two operands for `And`, `Or`, and `Until`.
- `AtomicProposition` stores the proposition name.

## Alphabet

### `AtomicPropositionSet`

Stores propositions in deterministic name order and assigns each proposition an index.

### `Valuation`

Represents one truth assignment over an `AtomicPropositionSet`. Internally it uses a `BitSet`.

### `Alphabet`

Enumerates all valuations for a proposition set. Its size is `2^|AP|` and the implementation limits the count to values that fit in a Java `long`.

## Analysis

### `AtomicPropositionCollector`

Walks a formula and returns the set of atomic propositions occurring in it.

### `FormulaSizeVisitor`

Computes the size of a formula tree.

## Parsing

### `FormulaParser`

A small abstraction for turning input text into a `Formula`. The core module only defines the interface; the current implementation is provided by Spot.

## Residual semantics

### `ResidualSemantics`

Defines `residual(formula, valuation)`.

### `DefaultResidualSemantics`

Delegates the actual work to `ResidualVisitor`.

### `ResidualVisitor`

Implements the one-step residual rules for the supported LTLf operators. For example:

- `Res(X φ, a) = φ`
- `Res(F φ, a) = Res(φ, a) ∨ F φ`
- `Res(G φ, a) = Res(φ, a) ∧ G φ`
- `Res(φ U ψ, a) = Res(ψ, a) ∨ (Res(φ, a) ∧ (φ U ψ))`

## Normalization

### `FormulaTransformer`

Common interface for formula transformations.

### `Simplifier`

Performs local structural simplifications such as `¬⊤ = ⊥`, `¬¬φ = φ`, and the usual `And`/`Or` identities.

### `BooleanCanonicalizer`

Provides deterministic ordering for Boolean structure.

### `Normalizer`

Runs an ordered list of `FormulaTransformer`s as a pipeline.

### `FormulaComparator`

Provides a deterministic ordering over formula structures.

## Residual automaton

### `ResidualState`

A state has an integer ID and one canonical residual formula.

### `ResidualStateRegistry`

Maps formulas to states. It first tries normalized structural lookup and then falls back to the supplied `FormulaEquivalenceChecker` for semantic equivalence.

### `StateLookupResult`

Returns the state found or created together with a boolean indicating whether a new state was created.

### `ResidualAutomatonBuilder`

Builds a reachable residual automaton with BFS. For every state, it evaluates every valuation in the alphabet, computes the residual, registers the resulting state, and adds the transition.

### `ResidualAutomaton`

The concrete deterministic automaton. Its labels are `Valuation`s and its states are `ResidualState`s. The transition relation is total over its alphabet. A state is accepting when its formula is `TrueFormula`.

## Semantics boundary

The core module defines:

```java
FormulaParser
FormulaEquivalenceChecker
ResidualSemantics
```

but does not choose external implementations. This is what lets Spot stay in its own module.
