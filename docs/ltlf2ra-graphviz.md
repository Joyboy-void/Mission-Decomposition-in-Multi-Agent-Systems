# Residual Automaton Graphviz exporter

`ltlf2ra-graphviz` converts a `ResidualAutomaton` into Graphviz DOT source.

`ResidualAutomatonDotExporter` controls the RA-specific presentation:

- state identifiers and residual-formula labels;
- the initial-state indicator;
- accepting-state styling;
- transition labels derived from valuations.

The exporter implements the shared `DotExporter<ResidualAutomaton>` contract. It returns DOT text only. Executable discovery and rendering are delegated to `graphviz-core`.
