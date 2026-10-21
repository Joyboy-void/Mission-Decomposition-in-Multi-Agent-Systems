# `product-automaton`

This module constructs the product between an `AgentModel` and a `ResidualAutomaton`.

A product state pairs one agent state with one residual state. Its label is inherited from the agent state, its acceptance status is determined by the residual state, and each product transition retains the cost of the corresponding agent transition.

## Atomic propositions

The RA used for product construction must be built over the union of the propositions in the formula and those in the agent-model labels:

```text
AP_product = AP_formula ∪ AP_agent
```

A valuation is a complete assignment over that universe. The agent label lists the propositions that are true; all propositions not listed are false. The builder creates transitions only for valuations compatible with the source agent state and for which the residual automaton has a corresponding transition.

## Initial and accepting states

Each initial agent state is paired with the RA's initial state. Product states are accepting when their residual component is accepting.

The builder contains product semantics only. File loading belongs to `agent-model`/`product-automaton-app`; DOT export belongs to `product-automaton-graphviz`.
