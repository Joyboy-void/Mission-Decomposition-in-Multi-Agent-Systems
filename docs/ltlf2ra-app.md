# `ltlf2ra-app`

This module is the application layer for the current LTLf-to-Residual-Automaton use case.

## `Ltlf2RaService`

The main application service.

`build(String)` does the following:

1. Validates and trims the input.
2. Parses it into a `Formula`.
3. Collects its atomic propositions.
4. Builds the `Alphabet`.
5. Creates the residual automaton builder with the configured semantics, normalizer, and equivalence checker.
6. Builds and returns the `ResidualAutomaton`.

The default constructor uses the Spot parser and Spot equivalence checker.

The second constructor allows the dependencies to be supplied explicitly, which is useful for focused tests.

## `Ltlf2RaApplication`

A small facade around the service and formatter. Its `execute(String)` method returns the formatted residual automaton as a string.

## `ResidualAutomatonFormatter`

Turns a `ResidualAutomaton` into readable terminal text, including the alphabet, states, and transitions.

The application module is intentionally small. It is about wiring and presentation, not about implementing residual semantics itself.
