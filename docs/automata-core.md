# `automata-core`

A small generic layer for automata that can be reused by different parts of the project.

## Core interfaces

### `Automaton<S, L>`

The base abstraction. An automaton exposes its states, initial state, final-state check, and outgoing edges.

### `DeterministicAutomaton<S, L>`

Extends `Automaton` with lookup by state and label:

- `hasTransition(state, label)`
- `getDestination(state, label)`

### `WeightedAutomaton<S, L, W>`

Extends `Automaton` for automata whose outgoing edges carry a weight of type `W`.

### `StateLabeledAutomaton<S, L, SL>`

Adds a label of type `SL` to each state.

## Edge types

### `Edge<S, L>`

Common read-only view of an edge: source, label, and destination.

### `Transition<S, L>`

A plain edge represented as a Java record.

### `WeightedTransition<S, L, W>`

A transition record with an additional `weight` field.

## Design note

The generic layer intentionally does not impose rules such as positive weights or a particular state representation. Those constraints belong to the concrete automata that need them.
