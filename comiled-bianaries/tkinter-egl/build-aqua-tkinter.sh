#!/usr/bin/env bash
set -euo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
SRC="${AQUA_TKINTER_EGL_SRC:-${AQUA_TKINTER_SRC:-$ROOT/comiled-bianaries/sources/aqua-tkinter-egl}}"
OUT="${AQUA_TKINTER_EGL_OUT:-${AQUA_TKINTER_OUT:-$ROOT/comiled-bianaries/out/tkinter-egl}}"
ARCH="${AQUA_TKINTER_EGL_ARCH:-${AQUA_TKINTER_ARCH:-x86_64}}"
JOBS="${AQUA_BUILD_JOBS:-10}"
API="${ANDROID_API:-29}"
NDK="${NDK:-/home/shado/envs/android-ndk-r29}"

"$ROOT/comiled-bianaries/tkinter-egl/fetch-tkinter-sources.sh"

case "$ARCH" in
  x86_64)
    TRIPLE="x86_64-linux-android"
    HOST="x86_64-linux-android"
    ;;
  aarch64|arm64-v8a)
    TRIPLE="aarch64-linux-android"
    HOST="aarch64-linux-android"
    ;;
  armv7|armeabi-v7a)
    TRIPLE="armv7a-linux-androideabi"
    HOST="arm-linux-androideabi"
    ;;
  x86|i686)
    TRIPLE="i686-linux-android"
    HOST="i686-linux-android"
    ;;
  *)
    echo "unknown arch: $ARCH" >&2
    exit 2
    ;;
esac

TOOLCHAIN="$NDK/toolchains/llvm/prebuilt/linux-x86_64"
if [[ ! -d "$TOOLCHAIN" ]]; then
  echo "missing Android NDK toolchain: $TOOLCHAIN" >&2
  exit 1
fi

PREFIX="$OUT/$ARCH/prefix"
BUILD="$OUT/$ARCH/build"
mkdir -p "$PREFIX" "$BUILD"

export CC="$TOOLCHAIN/bin/${TRIPLE}${API}-clang"
export CXX="$TOOLCHAIN/bin/${TRIPLE}${API}-clang++"
export AR="$TOOLCHAIN/bin/llvm-ar"
export RANLIB="$TOOLCHAIN/bin/llvm-ranlib"
export STRIP="$TOOLCHAIN/bin/llvm-strip"
export CFLAGS="${CFLAGS:-} -fPIC"
export LDFLAGS="${LDFLAGS:-}"

echo "==> Building Tcl for $ARCH"
mkdir -p "$BUILD/tcl"
(
  cd "$BUILD/tcl"
  "$SRC/tcl/unix/configure" \
    --host="$HOST" \
    --prefix="$PREFIX" \
    --enable-shared \
    --disable-load \
    ac_cv_func_strtod=yes \
    tcl_cv_strtod_buggy=ok
  make -j"$JOBS"
  make install
)

echo "==> Tcl installed into $PREFIX"

if [[ "${AQUA_BUILD_NATIVE_TK:-0}" != 1 ]]; then
  cat <<EOF
Native Tk build intentionally stopped here.

The X11 Tk backend must not be compiled into Aqua. Continue native Tk only after
the Android RGBA backend under:
  $SRC/tk/generic/tkAquaAndroidBridge.c
is wired into Tk's platform/display layer.

To experiment anyway:
  AQUA_BUILD_NATIVE_TK=1 AQUA_TKINTER_EGL_ARCH=$ARCH bash comiled-bianaries/tkinter-egl/build-aqua-tkinter.sh
EOF
  exit 0
fi

echo "==> Experimental Tk configure for $ARCH"
mkdir -p "$BUILD/tk"
(
  cd "$BUILD/tk"
  "$SRC/tk/unix/configure" \
    --host="$HOST" \
    --prefix="$PREFIX" \
    --with-tcl="$PREFIX/lib" \
    --enable-shared \
    --without-x
  make -j"$JOBS"
  make install
)

echo "==> Experimental Tk installed into $PREFIX"
