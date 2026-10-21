# `ltlf2ra-app`

This module assembles the LTLf-to-RA workflow for use by the CLI and other callers.

`Ltlf2RaService` parses the formula, collects propositions, creates the alphabet, configures residual semantics and normalization, and invokes the RA builder. `Ltlf2RaApplication` exposes operations to build the RA, format it for terminal output, export DOT, and render an output file.

The service also accepts additional atomic propositions. Product construction uses that overload to build the RA over the union of the formula's propositions and the agent model's propositions. Ordinary RA commands continue to use the formula's own proposition set.

The module owns orchestration and RA presentation, not the residual semantics, agent-model parser, or product construction.
