# FileVault WebAssembly Module

This directory will contain the WebAssembly version of FileVault core functionality.

## Compiling Java to WebAssembly

To compile Java code to WebAssembly, you'll need:

1. GraalVM with the WebAssembly feature
2. The `native-image` tool from GraalVM

### Installation Steps

1. Install GraalVM 22.3.0 or newer
2. Install WebAssembly support:
   ```
   gu install wasm
   ```
3. Set up environment variables:
   ```
   export GRAALVM_HOME=/path/to/graalvm
   export PATH=$GRAALVM_HOME/bin:$PATH
   ```

### Compilation Process

1. Create a simplified version of your core functionality
2. Use GraalVM to compile to WebAssembly:
   ```
   native-image --target=wasm --no-fallback -H:Name=filevault
   ```
3. Place the compiled `filevault.wasm` file in this directory

## Current Status

The WebAssembly module is currently under development. The demo currently uses a simulation mode.

## Alternative Approaches

If direct Java to WebAssembly compilation is challenging, consider:

1. Implementing core functionality in C/C++ and compiling with Emscripten
2. Using JavaScript/TypeScript to reimplement core encryption functionality
3. Using a service-based approach where encryption happens server-side

## Resources

- [GraalVM WebAssembly Documentation](https://www.graalvm.org/latest/reference-manual/wasm/)
- [Emscripten Documentation](https://emscripten.org/docs/getting_started/index.html)
- [WebAssembly Official Site](https://webassembly.org/) 