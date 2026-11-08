# Product Automaton

`product-automaton` combines an `AgentModel` with a `ResidualAutomaton`. The product tracks both the agent's current state and the remaining temporal obligation.

## Product state

A `ProductState` pairs:

```text
(agentState, residualState)
```

Its proposition label is inherited from the agent state. Its acceptance status is determined by the residual component. Product transitions retain the cost of the corresponding agent-model transition.

## Atomic-proposition universe

The RA used for product construction must be built over the union of propositions in the formula and in the agent model:

```text
AP_product = AP_formula ∪ AP_agent
```

This is essential when the agent model contains propositions that are not mentioned in the formula. Valuations are complete assignments over this universe. A state label lists the propositions that are true; omitted propositions are false.

The builder creates transitions only when the valuation is compatible with the source agent state's label and the RA has the corresponding transition.

## Initial and accepting states

Each initial agent state is paired with the RA's initial state. Product states are accepting when their residual component is accepting.

## Module boundary

The module owns product-state semantics and construction. Loading model files belongs to `agent-model` and the application layer; DOT export belongs to `product-automaton-graphviz`.
