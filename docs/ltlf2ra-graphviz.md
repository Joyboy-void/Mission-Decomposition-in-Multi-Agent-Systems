# `ltlf2ra-graphviz`

This module converts a `ResidualAutomaton` into Graphviz DOT. `ResidualAutomatonDotExporter` controls RA-specific graph details, including state IDs, residual-formula labels, the initial arrow, accepting-state shapes, and transition labels.

The exporter implements `DotExporter<ResidualAutomaton>` from `graphviz-core`. It returns DOT text and does not start a Graphviz process. Rendering and executable resolution belong to the shared infrastructure.
