#!/usr/bin/env bash
set -euo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "$ROOT/.." && pwd)"
GNU_BASH_VERSION="${ANDROPY_BASH_VERSION:-5.2.37}"
NDK="${NDK:-/home/shado/envs/android-ndk-r25c}"
TOOLCHAIN="$NDK/toolchains/llvm/prebuilt/linux-x86_64"
PREFIX="/data/data/com.andropy.ide/files/usr"
TARBALL="$ROOT/src/bash-$GNU_BASH_VERSION.tar.gz"
URL="https://ftp.gnu.org/gnu/bash/bash-$GNU_BASH_VERSION.tar.gz"

mkdir -p "$ROOT/src" "$ROOT/build" "$ROOT/out"

if [[ ! -f "$TARBALL" ]]; then
  curl -L --fail "$URL" -o "$TARBALL"
fi

build_one() {
  local abi="$1"
  local host="$2"
  local cc="$3"
  local build_dir="$ROOT/build/bash-$GNU_BASH_VERSION-$abi"
  local out_dir="$ROOT/out/$abi/bin"

  rm -rf "$build_dir"
  mkdir -p "$build_dir" "$out_dir"
  tar -xf "$TARBALL" -C "$build_dir" --strip-components=1

  cd "$build_dir"
  chmod +x configure support/config.guess support/config.sub

  export PATH="$TOOLCHAIN/bin:$PATH"
  export CC="$cc"
  export AR=llvm-ar
  export RANLIB=llvm-ranlib
  export CFLAGS="-Os -fPIE"
  export LDFLAGS="-pie"

  ./configure \
    --host="$host" \
    --build="$(sh ./support/config.guess)" \
    --prefix="$PREFIX" \
    --without-bash-malloc \
    --disable-nls \
    bash_cv_job_control_missing=present \
    bash_cv_getcwd_malloc=yes \
    ac_cv_func_getpwent=no \
    ac_cv_func_getpwnam=no \
    ac_cv_func_getpwuid=no \
    ac_cv_func_getgrgid=no \
    ac_cv_func_getgrnam=no \
    ac_cv_func_setpwent=no \
    ac_cv_func_endpwent=no

  make -j"$(nproc)"
  cp bash "$out_dir/bash"
  "$TOOLCHAIN/bin/llvm-strip" "$out_dir/bash"
  file "$out_dir/bash"
}

build_one x86_64 x86_64-linux-android x86_64-linux-android26-clang
build_one arm64-v8a aarch64-linux-android aarch64-linux-android26-clang

build_launcher() {
  local abi="$1"
  local cc="$2"
  local out="$ROOT/out/$abi/bin/andropy-bash-launcher"

  "$TOOLCHAIN/bin/$cc" -Os -fPIE -pie "$ROOT/andropy-bash-launcher.c" -o "$out"
  "$TOOLCHAIN/bin/llvm-strip" "$out"
  file "$out"
}

build_launcher x86_64 x86_64-linux-android26-clang
build_launcher arm64-v8a aarch64-linux-android26-clang

mkdir -p "$PROJECT_ROOT/app/src/main/jniLibs/x86_64"
mkdir -p "$PROJECT_ROOT/app/src/main/jniLibs/arm64-v8a"
cp "$ROOT/out/x86_64/bin/bash" "$PROJECT_ROOT/app/src/main/jniLibs/x86_64/libandropy_bash.so"
cp "$ROOT/out/arm64-v8a/bin/bash" "$PROJECT_ROOT/app/src/main/jniLibs/arm64-v8a/libandropy_bash.so"
cp "$ROOT/out/x86_64/bin/andropy-bash-launcher" "$PROJECT_ROOT/app/src/main/jniLibs/x86_64/libandropy_bash_launcher.so"
cp "$ROOT/out/arm64-v8a/bin/andropy-bash-launcher" "$PROJECT_ROOT/app/src/main/jniLibs/arm64-v8a/libandropy_bash_launcher.so"
