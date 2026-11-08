# Agent model

The `agent-model` module defines the state-transition model for an individual agent and parses its text representation.

## Main concepts

- **`AgentModel`** stores the states, initial states, labels, and outgoing transitions.
- **`AgentState`** identifies a state.
- **`AgentTransition`** records a source, destination, and non-negative integer cost.
- **State label** is a set of atomic propositions that are true at that state.
- **`AgentModelParser`** reads and validates the file format described in [Agent-model input format](agent-model-format.md).

The model can expose the union of propositions used in its state labels. Product construction uses this set together with the propositions in the LTLf formula.

## Semantics and boundaries

A proposition absent from a state's label is false. Edge costs represent the cost of the corresponding agent action and are retained in product transitions.

This module owns the model and parser only. It does not construct residual or product automata, interpret CLI options, or render graphs. Those responsibilities belong to the corresponding application and exporter modules.
