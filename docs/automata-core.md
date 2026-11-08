# Generic automata core

`automata-core` contains reusable abstractions shared by the domain-specific automata.

It defines generic automaton interfaces and supporting types such as edges, transitions, and weighted transitions. The abstractions are parameterized by state, action/label, and weight types, allowing the same traversal and rendering infrastructure to work with residual, product, and team models.

The module deliberately has no knowledge of LTLf formulas, agent capabilities, DRes, the CLI, Spot, or Graphviz. Domain modules provide the concrete semantics.
