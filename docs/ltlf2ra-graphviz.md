# `ltlf2ra-graphviz`

The Graphviz module converts a `ResidualAutomaton` into a visual graph.

It is intentionally independent of the command-line layer.

## Components

### `ResidualAutomatonDotExporter`

Produces Graphviz DOT source.

The exporter:

- uses left-to-right layout
- marks accepting states with `doublecircle`
- adds a separate invisible initial node
- displays `qN` in bold
- displays the residual formula below the state name
- uses a smaller formula font
- groups valuations that share the same source and destination

The exporter only creates DOT text. It does not start Graphviz.

### `GraphvizExecutable`

Resolves the Graphviz `dot` executable in this order:

1. `GRAPHVIZ_DOT`
2. `dot` on `PATH`
3. `GRAPHVIZ_HOME/bin/dot` or `dot.exe`

### `GraphvizRenderer`

Runs:

```text
dot -T<format> -o<output>
```

with the generated DOT supplied through standard input.

The renderer creates the output directory when necessary and reports Graphviz failures through `GraphvizException`.

## Supported CLI Formats

The current CLI exposes:

```text
--dot
--svg
--png
```

DOT is returned as text. SVG and PNG are rendered through the installed Graphviz executable.

## PNG Support

When Graphviz is built from source, the setup process installs the platform-specific GD development dependency (`libgd-dev` on Debian/Ubuntu) so the PNG renderer is available.

## Output Example

```bash
./scripts/run-cli.sh --png output/automaton/example.png "F G p"
```

The generated image contains the residual states, accepting-state styling, initial arrow and grouped valuation transitions.
