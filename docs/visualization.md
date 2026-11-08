# Visualization and Graphviz

The project separates graph description from graph rendering.

## 1. Export pipeline

Each domain exporter converts its automaton into DOT text. The shared `graphviz-core` module resolves and runs the Graphviz executable:

```text
domain automaton
    → domain DotExporter
    → DOT source
    → GraphvizRenderer
    → SVG / PNG
```

Exporters do not launch external processes. The renderer does not know domain-specific state semantics.

## 2. Exporters

| Module | Exported model |
|---|---|
| `ltlf2ra-graphviz` | Residual Automaton |
| `agent-model-graphviz` | Agent model |
| `product-automaton-graphviz` | Product Automaton |
| `team-automaton-graphviz` | Team Automaton |

## 3. Layout engines

- `dot` is used for ordinary hierarchical layouts.
- `neato` is used for graphs whose exporter assigns explicit positions, including the Product and Team Automata.

The Product Automaton exporter uses a fixed grid arrangement. The Team Automaton exporter lays out the logical copies and their transitions for inspection.

## 4. Highlighted output

The decomposition image exporter can render the Team Automaton with the exact weighted transitions selected by Dijkstra highlighted. The ordinary, unhighlighted automaton exports remain available separately.

DRes image output highlights DRes states in the Residual Automaton. These visualizations are diagnostic views; the underlying algorithms operate on automaton objects, not on rendered images.

## 5. Troubleshooting

- Confirm Graphviz is installed and that the launcher can resolve its executable.
- Check that the installed Graphviz build supports both SVG and PNG output.
- Use the supplied setup check to diagnose environment configuration.
- If a graph is written but not rendered, inspect the DOT output and Graphviz error message separately.
