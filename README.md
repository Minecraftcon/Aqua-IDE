# Aqua IDE

Aqua IDE is a multilingual Android programming IDE focused on a clean editor, an integrated terminal, and an app-local Linux-style prefix for language tooling.

## Current Features

- Full-screen code editor with line numbers, active-line highlighting, and Python syntax colors.
- Drawer actions for Terminal, Interpreter, and Pip.
- Termux terminal-view based terminal surface.
- App-local runtime layout:
  - `PREFIX=/data/data/com.andropy.ide/files/usr`
  - `HOME=/data/data/com.andropy.ide/files/home`
- Bundled Python runtime bootstrap hooks with working `pip`.
- Native bash/coreutils/nano launcher integration for local builds.

## Repository Scope

This private repository intentionally excludes heavy generated artifacts:

- Gradle and APK build outputs.
- Emulator screenshots and debug dumps.
- Generated runtime payloads under `runtime-payloads/`.
- Generated JNI shared libraries under `app/src/main/jniLibs`.
- Downloaded/extracted cross-compile caches under `comiled-bianaries`.

Keep those artifacts local or rebuild them from the scripts in `comiled-bianaries/`.

## Build Notes

The source tree expects Android SDK/NDK paths from your local environment. Do not commit `local.properties`.

Runtime payloads are no longer bundled inside the APK. First launch downloads
only the device ABI payload from the `runtime-v8` GitHub release:

- `aqua-runtime-x86_64-v8.zip`
- `aqua-runtime-arm64-v8a-v8.zip`

Regenerate those release files with:

```bash
tools/package-runtime-payloads.sh
```

For the slim Python-first runtime used by the APK, generate:

```bash
tools/package-slim-runtime-payloads.sh
```

Before a fully runnable APK build, regenerate or restore:

- `runtime-payloads/runtime-x86_64`
- `runtime-payloads/runtime-arm64-v8a`
- `app/src/main/jniLibs`

The smaller common assets under `app/src/main/assets/runtime-common` are kept in Git.

## License

This project is closed source and proprietary. See [LICENSE](LICENSE).
