# `ltlf2ra-spot`

This module supplies the Spot-backed implementations used by the RA application. `SpotFormulaParser` parses formula text, `SpotFormulaPrinter` converts supported AST nodes to Spot syntax, and `SpotEquivalenceChecker` asks Spot whether two formulas are semantically equivalent.

`SpotNativeBridge` declares the Java/JNI boundary and the native C++ implementation calls Spot. Maven and CMake build the native bridge. `SPOT_HOME` identifies the Spot installation, and the launch scripts configure the runtime library paths.

Spot stays out of `ltlf2ra-core`; the core relies on interfaces and can be tested independently of the native implementation.
