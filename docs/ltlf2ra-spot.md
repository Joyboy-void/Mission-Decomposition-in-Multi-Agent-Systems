# Spot integration

`ltlf2ra-spot` provides the Spot-backed services used by the LTLf application.

## Components

- **`SpotFormulaParser`** parses formula text and converts it into the project's formula representation.
- **`SpotFormulaPrinter`** converts supported AST nodes into Spot-compatible formula syntax.
- **`SpotEquivalenceChecker`** uses Spot to decide semantic equivalence between formulas.
- **`SpotNativeBridge`** declares the Java/JNI boundary.
- **`SpotNativeBridge.cpp`** implements the native side of that boundary.

The residual core depends on interfaces rather than on Spot classes. This keeps formula semantics and automaton construction separable from the external library.

## Build and runtime

The native bridge is built with Maven and CMake. The environment requires a compatible Spot installation, a C++ toolchain, CMake, and Ninja. `SPOT_HOME` identifies the Spot installation. The provided setup and run scripts configure platform-specific paths.

If native loading fails, first run the setup check and verify that the Spot installation and runtime library paths match the current platform.
