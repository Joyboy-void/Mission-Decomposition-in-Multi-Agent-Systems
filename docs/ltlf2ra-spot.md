# `ltlf2ra-spot`

This module contains the native Spot integration.

## Responsibilities

- Parse LTLf formulas using Spot
- Print the project AST in Spot syntax
- Check semantic equivalence of formulas
- Expose Spot functionality to Java through JNI

## Components

### `SpotFormulaParser`

Implements `FormulaParser` using the native bridge.

### `SpotFormulaPrinter`

Converts supported AST nodes to Spot syntax.

### `SpotEquivalenceChecker`

Implements `FormulaEquivalenceChecker` by asking Spot whether two formulas are semantically equivalent.

### `SpotNativeBridge`

The Java/JNI boundary. The native API currently exposes parsing and equivalence operations.

### `SpotNativeBridge.cpp`

Implements the JNI functions and calls Spot's native API.

## Runtime

The project expects:

```text
SPOT_HOME
```

to identify the Spot installation.

The native bridge is built with CMake and placed under:

```text
ltlf2ra-spot/target/native-build/native/
```

The launcher scripts configure the required native library search paths.

## Why Spot Is Isolated

Semantic equivalence is needed by residual-state registration, but the core algorithm should not depend on one particular equivalence engine.

Therefore:

```text
ltlf2ra-core
      │
      ▼
FormulaEquivalenceChecker
      ▲
      │
ltlf2ra-spot
      │
      ▼
     Spot
```
