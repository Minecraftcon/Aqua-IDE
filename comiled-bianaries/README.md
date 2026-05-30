# AndroPy compiled binaries

Put host-built or cross-compiled runtime binaries here before packaging them into
the Android app prefix.
NDK_TOOLCHAIN=~/envs (on pc)
Runtime prefix on device:

```text
/data/data/com.andropy.ide/files/usr
```

Runtime home on device:

```text
/data/data/com.andropy.ide/files/home
```

Expected runtime layout:

```text
$PREFIX/bin
$PREFIX/etc
$PREFIX/lib
$PREFIX/share
$PREFIX/tmp
$PREFIX/var
$PREFIX/var/log
$PREFIX/var/run
$PREFIX/var/tmp
$HOME
$HOME/.bashrc
```

## Bash

Built GNU bash 5.2.37 for Android API 26 with the NDK toolchain:

```text
comiled-bianaries/out/x86_64/bin/bash
comiled-bianaries/out/arm64-v8a/bin/bash
```

The APK packages matching copies under:

```text
app/src/main/jniLibs/x86_64/libandropy_bash.so
app/src/main/jniLibs/arm64-v8a/libandropy_bash.so
app/src/main/jniLibs/x86_64/libandropy_bash_launcher.so
app/src/main/jniLibs/arm64-v8a/libandropy_bash_launcher.so
```

Android blocks normal app processes from executing files copied into writable
app data on modern targets, so `MainActivity` symlinks `$PREFIX/bin/bash` to
the extracted packaged native payload before the Termux `TerminalSession`
starts. The terminal starts through `andropy-bash-launcher`, which chdirs into
the real app prefix before execing bash so GNU bash does not die while resolving
Android's app-data paths.

The build uses Termux's important Android configure fix
`bash_cv_getcwd_malloc=yes`; without it, upstream bash falls back to cwd logic
that breaks under Android app-private `/data` paths.

Rebuild command:

```sh
bash comiled-bianaries/build-bash-android.sh
```

## GNU coreutils, nano, and ncurses

GNU coreutils 9.11 and GNU nano 9.0 are built from upstream source with the
current Termux package patches applied where they match this app prefix.
Ncurses 6.5 is built shared for nano, clear, and tset/reset.

```text
comiled-bianaries/out/<abi>/bin/bash
app/src/main/jniLibs/<abi>/libandropy_coreutils.so
app/src/main/jniLibs/<abi>/libandropy_nano.so
app/src/main/jniLibs/<abi>/libandropy_clear.so
app/src/main/jniLibs/<abi>/libandropy_tset.so
app/src/main/jniLibs/<abi>/libandropy_tool_launcher.so
app/src/main/jniLibs/<abi>/libncursesw.so
app/src/main/assets/runtime-common/etc/nanorc
app/src/main/assets/runtime-common/share/nano
app/src/main/assets/runtime-common/share/terminfo
```

The app copies `runtime-common` into `$PREFIX` on startup. Commands in
`$PREFIX/bin` are symlinked to `libandropy_tool_launcher.so`, which dispatches
to the packaged native payload while preserving the command name (`ls`, `cat`,
`nano`, `clear`, and so on).

Rebuild command:

```sh
bash comiled-bianaries/build-gnu-tools-android.sh
```
