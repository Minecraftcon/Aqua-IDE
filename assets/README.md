# Aqua Assets

This folder contains optional downloadable Aqua payloads that are not part of
the core runtime bootstrap.

- `debs/`: optional native/toolchain `.deb` packages such as LLVM, clang, CMake,
  Ninja, OpenBLAS, and sysroot pieces.
- `python/`: Aqua's prebuilt Python package area. Keep pip-installable or
  Python-module payloads here so the app can offer them separately from the base
  runtime.
