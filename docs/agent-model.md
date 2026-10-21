# `agent-model`

This module defines the agent-model data structures and reads the text input format.

`AgentModel` stores the ordered state list, initial states, labels, and outgoing transitions. `AgentState` identifies a state, while `AgentTransition` records source, destination, and non-negative integer cost. State labels are sets of atomic propositions.

`AgentModelParser` validates and parses the format documented in [Agent-model input format](agent-model-format.md). The model can report the union of all propositions used by its labels; this is used when constructing a product with an RA.

A proposition absent from a state's label is interpreted as false. The module does not know about CLI options or Graphviz rendering. Those responsibilities belong to `agent-model-app` and `agent-model-graphviz`.
