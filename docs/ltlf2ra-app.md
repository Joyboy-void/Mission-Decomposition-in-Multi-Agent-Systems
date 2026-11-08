# LTLf-to-RA application

`ltlf2ra-app` assembles the services required to build and present a Residual Automaton.

## Workflow

`Ltlf2RaService` parses a formula, collects its atomic propositions, creates the alphabet, configures residual semantics and normalization, and invokes the RA builder.

`Ltlf2RaApplication` exposes higher-level operations to:

- build a Residual Automaton;
- format it for terminal output;
- export its DOT representation;
- render an image.

The service also accepts additional atomic propositions. Product construction uses this capability to build the RA over the union of the formula propositions and the agent-model propositions. A standalone RA command normally uses only the propositions present in its formula.

## Boundary

This module coordinates the RA workflow. It does not own the AST/residual algorithm, parse agent models, construct product automata, or implement Graphviz process execution.
