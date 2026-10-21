# `graphviz-core`

This module provides the shared Graphviz integration. `DotExporter<T>` defines how a domain object is converted to DOT. `GraphvizRenderer` runs Graphviz, creates output directories, and reports failures through `GraphvizException`. `GraphvizExecutable` resolves the executable to run.

The renderer supports `dot` for automatic layout and `neato` with fixed positions for the product automaton's grid layout. The renderer does not import domain classes or decide how states and transitions should be labelled.

Domain exporters should produce DOT source and delegate process execution to this module.
