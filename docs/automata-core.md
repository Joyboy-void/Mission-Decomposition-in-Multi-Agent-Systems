# `automata-core`

This module contains generic automaton abstractions shared by the domain modules. It defines automaton interfaces, deterministic and state-labelled variants, edges, transitions, and weighted transitions.

The types are generic over state, label, and weight. They do not assume that a state is an LTLf residual, an agent state, or a product state. The module has no parser, CLI, Spot, or Graphviz responsibilities.
