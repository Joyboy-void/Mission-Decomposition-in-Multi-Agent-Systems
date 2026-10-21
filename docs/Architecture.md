# Architecture

The repository is split into Maven modules with explicit responsibilities. The CLI is the entry point and delegates to application modules. Domain modules own models and algorithms; Graphviz exporter modules convert domain objects into DOT; `graphviz-core` handles rendering.

## Module relationships

```text
cli
 ├── ltlf2ra-app
 │    ├── ltlf2ra-core
 │    │    └── automata-core
 │    ├── ltlf2ra-spot
 │    └── ltlf2ra-graphviz
 ├── agent-model-app
 │    ├── agent-model
 │    └── agent-model-graphviz
 └── product-automaton-app
      ├── product-automaton
      ├── product-automaton-graphviz
      ├── agent-model
      └── ltlf2ra-core

Domain exporters and applications use graphviz-core for DOT rendering.
```

The POM files define the exact dependencies; the diagram describes the responsibility boundaries.

## Module responsibilities

- **`automata-core`** — generic automaton, edge, transition, and weighted-transition abstractions. It contains no LTLf semantics.
- **`ltlf2ra-core`** — formula AST, propositions, valuations, alphabets, residual semantics, formula transformations, residual-state registry, and RA construction.
- **`ltlf2ra-spot`** — Spot formula parsing/printing, semantic equivalence checks, and the Java/JNI bridge to native Spot.
- **`ltlf2ra-graphviz`** — converts a `ResidualAutomaton` into DOT, including residual formula labels and accepting-state styling.
- **`ltlf2ra-app`** — coordinates the RA workflow: build, format, export DOT, and render. It does not load agent models or build products.
- **`agent-model`** — agent states, initial states, state labels, weighted transitions, and the text parser.
- **`agent-model-graphviz`** — converts an `AgentModel` into DOT.
- **`agent-model-app`** — loads agent models and coordinates their visualization.
- **`product-automaton`** — product state model and construction, AP compatibility, initial states, acceptance, and transition costs.
- **`product-automaton-graphviz`** — converts product automata into DOT and assigns fixed grid positions: agent states define columns and residual states define rows.
- **`product-automaton-app`** — loads the model, builds the RA over the required AP universe, constructs the product, and renders it.
- **`graphviz-core`** — generic `DotExporter<T>`, executable discovery, rendering, and rendering exceptions. It has no domain-specific graph knowledge.
- **`cli`** — command parsing, dependency wiring, help text, and exit-code handling. It does not implement domain algorithms.

## Data flows

### Residual automaton

```text
formula → parser → AST → AP collection → alphabet
        → reachable residual-state exploration
        → Residual Automaton → text / DOT / SVG / PNG
```

### Agent model

```text
model file → AgentModelParser → AgentModel
           → AgentModelDotExporter → GraphvizRenderer → SVG / PNG
```

### Product automaton

```text
agent-model file → AgentModel
formula → AP_formula ∪ AP_agent → Residual Automaton
AgentModel + Residual Automaton → ProductAutomaton
                              → DOT → neato fixed layout → SVG / PNG
```

## Atomic-proposition universe

A formula may use fewer propositions than the agent model. For product construction, the RA alphabet must use the union:

```text
AP_product = AP_formula ∪ AP_agent
```

Valuations are complete assignments over this universe. A state's label identifies the propositions that are true there; any proposition not listed is false. Product transitions are generated only when the valuation is compatible with the source agent state's label and the RA has the corresponding transition.

## Design boundaries

Domain modules do not invoke the CLI. Exporters produce DOT but do not execute Graphviz. The shared renderer does not import domain classes. Spot remains behind interfaces so residual construction is not tied to one external parser or equivalence engine.
