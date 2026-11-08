# Architecture

The application is organized as a Maven multi-module project. Its central pipeline turns a finite-trace temporal specification and a weighted agent model into a minimum-cost assignment of work across a fixed number of homogeneous agents.

## 1. System overview

```text
                         LTLf formula
                              │
                              ▼
                       Spot formula parser
                              │
                              ▼
                       Formula AST / AP set
                              │
                              ▼
                       Residual Automaton
                              │
                              ├───────────────┐
                              │               │
                              ▼               │
                         DRes analysis        │
                              │               │
                              └───────┐       │
                                      ▼       ▼
Agent-model file ──> AgentModel ──> Product Automaton
                                      │
                                      ▼
                              Team Automaton (n copies)
                                      │
                                      ▼
                              Dijkstra shortest path
                                      │
                                      ▼
                           Decomposition / cost / path
```

The decomposition application coordinates these steps. The standalone RA, DRes, agent, product, and team commands expose intermediate stages for inspection.

## 2. Maven modules

| Module | Responsibility |
|---|---|
| `automata-core` | Generic automaton, edge, transition, and weighted-transition abstractions |
| `graphviz-core` | Generic DOT exporter contract, Graphviz executable resolution, rendering, and rendering errors |
| `ltlf2ra-core` | LTLf AST, propositions, valuations, alphabet, residual semantics, normalization, state registry, and RA builder |
| `ltlf2ra-spot` | Spot parser/printer, semantic equivalence checker, and JNI/native bridge |
| `ltlf2ra-graphviz` | Residual Automaton DOT exporter |
| `ltlf2ra-app` | RA construction, formatting, export, and rendering orchestration |
| `agent-model` | Agent states, labels, initial states, weighted transitions, and text parser |
| `agent-model-graphviz` | Agent-model DOT exporter |
| `agent-model-app` | Agent-model loading and visualization workflow |
| `product-automaton` | Product states, product construction, acceptance, AP compatibility, and transition costs |
| `product-automaton-graphviz` | Product Automaton DOT exporter and fixed-grid layout |
| `product-automaton-app` | Product workflow orchestration |
| `ltlf2ra-core` (DRes package) | DRes result and finder over the Residual Automaton |
| `team-automaton` | Team states/actions, implicit Team Automaton, and builder |
| `team-automaton-graphviz` | Team Automaton DOT exporter |
| `decomposition` | Minimum-cost path search and immutable decomposition result |
| `decomposition-app` | End-to-end pipeline, decomposition formatting, and image export |
| `cli` | Argument parsing, dependency wiring, help, and exit-code handling |

The root `pom.xml` is the reactor parent and targets Java 21. POM files define the exact compile-time dependencies.

## 3. Dependency direction

The design keeps algorithms independent from command-line and rendering concerns:

```text
cli
 ├── ltlf2ra-app
 ├── agent-model-app
 ├── product-automaton-app
 └── decomposition-app
       ├── agent-model
       ├── ltlf2ra-core / ltlf2ra-app
       ├── product-automaton
       ├── team-automaton
       └── decomposition

Domain graph exporters ──> graphviz-core
```

The diagram is conceptual; consult the module POM files for the exact dependency graph. Domain modules do not depend on the CLI. Exporters return DOT and do not launch Graphviz themselves.

## 4. End-to-end execution

The decomposition pipeline is coordinated by `DecompositionApplication`:

1. **Load the agent model.** The parser validates the input file and creates an `AgentModel`.
2. **Build the Residual Automaton.** The formula is parsed, its propositions are collected, and reachable residual states and transitions are generated.
3. **Compute DRes.** Synchronized reachability identifies residual states that permit the required exchange of finite task sequences.
4. **Build the Product Automaton.** The agent model is combined with the RA using the complete proposition universe.
5. **Build the Team Automaton.** The Product Automaton is reused across `n` logical copies, with DRes-governed epsilon switches.
6. **Find a minimum-cost accepting path.** Dijkstra searches from the Team Automaton's initial states to an accepting state.
7. **Format or export the result.** The selected path is split at epsilon switches to produce per-agent work segments.

## 5. Data flow by subsystem

### Residual Automaton

```text
formula text
   → SpotFormulaParser
   → formula AST
   → AP collection and alphabet
   → residual semantics + normalization
   → reachable-state exploration
   → ResidualAutomaton
   → formatter / DOT exporter
```

### Agent model

```text
model file
   → AgentModelParser
   → AgentModel
   → AgentModelDotExporter
   → DOT
   → GraphvizRenderer
```

### Product Automaton

```text
AgentModel + ResidualAutomaton
   → compatible state/valuation transitions
   → ProductAutomaton
```

### Team Automaton and decomposition

```text
ProductAutomaton + DRes + n
   → implicit TeamAutomaton
   → Dijkstra
   → optimal path and cost
   → per-agent segments
```

## 6. Atomic-proposition universe

A formula may mention fewer propositions than the agent model. Constructing the RA only over formula propositions would make it impossible to represent all valuations needed by the product.

The product workflow therefore uses:

```text
AP_product = AP_formula ∪ AP_agent
```

Each valuation assigns truth values to the full universe. A state label is the set of propositions true at that agent state; propositions not present are false. Product transitions are created only when the source agent state's valuation is compatible with the corresponding RA transition.

## 7. Key model relationships

- A `ResidualState` represents a residual LTLf formula.
- A `ProductState` pairs an `AgentState` with a `ResidualState`.
- A `TeamState` pairs a copy identifier with a `ProductState`.
- A `ValuationAction` represents an ordinary product transition.
- `SwitchAction.EPSILON` moves from one agent copy to the next without consuming a valuation and with zero cost.
- A final Team Automaton state must be accepting in the Product Automaton's residual component and belong to the final agent copy.

## 8. Why the boundaries matter

The architecture allows each layer to be tested and changed independently. Formula semantics do not depend on Graphviz. The generic renderer does not know about residual or product states. DRes does not need to know how a decomposition is printed. The CLI can expose new workflows without moving domain algorithms into argument-parsing code.
