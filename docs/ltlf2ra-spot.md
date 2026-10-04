# `ltlf2ra-spot`

This module contains the integration with Spot. It is the only module that needs JNI and native Spot libraries.

## Java classes

### `SpotFormulaParser`

Implements `FormulaParser` by sending the input string through `SpotNativeBridge.parse`.

### `SpotFormulaPrinter`

Converts the project's formula AST into Spot syntax. It handles the supported constants, propositions, Boolean operators, `X`, `F`, `G`, and `U`.

The printer assumes proposition names are valid Spot identifiers.

### `SpotEquivalenceChecker`

Implements `FormulaEquivalenceChecker`. It prints both formulas in Spot syntax and asks the native bridge whether they are equivalent.

### `SpotNativeBridge`

The Java/JNI boundary. It exposes two native operations:

- `equivalent(left, right)`
- `parse(input)`

The static initializer loads `ltlf_spot_bridge`.

## Native side

`SpotNativeBridge.cpp` implements the JNI functions. `CMakeLists.txt` builds the shared library and locates Spot from `SPOT_HOME`.

## Runtime requirement

The native bridge needs the Spot installation and its native dependencies at runtime. The launcher scripts set the appropriate library search paths for the platform.

## Why Spot is isolated

Semantic equivalence is useful when two syntactically different residual formulas represent the same language. Keeping this implementation here means the LTLf core can use the same abstraction without depending directly on Spot.
