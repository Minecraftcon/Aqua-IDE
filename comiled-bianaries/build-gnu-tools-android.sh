#!/usr/bin/env bash
set -euo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "$ROOT/.." && pwd)"
TERMUX_PACKAGES="$ROOT/termux-packages/packages"
NDK="${NDK:-/home/shado/envs/android-ndk-r25c}"
TOOLCHAIN="$NDK/toolchains/llvm/prebuilt/linux-x86_64"
RUNTIME_PREFIX="/data/data/com.andropy.ide/files/usr"
BUILD_TRIPLET="${BUILD_TRIPLET:-x86_64-pc-linux-gnu}"
COREUTILS_VERSION="${ANDROPY_COREUTILS_VERSION:-9.11}"
NANO_VERSION="${ANDROPY_NANO_VERSION:-9.0}"
NCURSES_VERSION="${ANDROPY_NCURSES_VERSION:-6.5}"
ANDROID_SUPPORT_VERSION="${ANDROPY_ANDROID_SUPPORT_VERSION:-29}"
WCWIDTH_VERSION="${ANDROPY_WCWIDTH_VERSION:-4}"

COREUTILS_URL="https://mirrors.kernel.org/gnu/coreutils/coreutils-${COREUTILS_VERSION}.tar.xz"
NANO_URL="https://nano-editor.org/dist/latest/nano-${NANO_VERSION}.tar.xz"
NCURSES_URL="https://ftp.gnu.org/pub/gnu/ncurses/ncurses-${NCURSES_VERSION}.tar.gz"
ANDROID_SUPPORT_URL="https://github.com/termux/libandroid-support/archive/refs/tags/v${ANDROID_SUPPORT_VERSION}.tar.gz"
WCWIDTH_URL="https://github.com/termux/wcwidth/archive/refs/tags/v${WCWIDTH_VERSION}.tar.gz"

mkdir -p "$ROOT/src" "$ROOT/build" "$ROOT/out" "$ROOT/stage"

fetch() {
  local url="$1"
  local file="$2"
  [[ -f "$file" ]] || curl -L --fail "$url" -o "$file"
}

apply_termux_patch() {
  local patch_file="$1"
  sed "s|@TERMUX_PREFIX@|$RUNTIME_PREFIX|g" "$patch_file" | patch -p1
}

setup_toolchain() {
  local abi="$1"
  case "$abi" in
    x86_64)
      HOST="x86_64-linux-android"
      CC_NAME="x86_64-linux-android26-clang"
      ;;
    arm64-v8a)
      HOST="aarch64-linux-android"
      CC_NAME="aarch64-linux-android26-clang"
      ;;
    *)
      echo "Unknown ABI: $abi" >&2
      exit 2
      ;;
  esac

  export PATH="$TOOLCHAIN/bin:$PATH"
  export CC="$CC_NAME"
  export AR=llvm-ar
  export RANLIB=llvm-ranlib
  export STRIP=llvm-strip
}

build_ncurses() {
  local abi="$1"
  local build_dir="$ROOT/build/ncurses-${NCURSES_VERSION}-$abi"
  local stage="$ROOT/stage/$abi"

  rm -rf "$build_dir"
  mkdir -p "$build_dir"
  tar -xf "$ROOT/src/ncurses-${NCURSES_VERSION}.tar.gz" -C "$build_dir" --strip-components=1
  cd "$build_dir"

  apply_termux_patch "$TERMUX_PACKAGES/ncurses/fix-paths.patch" || true

  export CPPFLAGS="-fPIC"
  export CFLAGS="-Os -fPIE"
  export LDFLAGS="-pie"
  export LIBS=""

  ./configure \
    --host="$HOST" \
    --build="$BUILD_TRIPLET" \
    --prefix="$RUNTIME_PREFIX" \
    --disable-stripping \
    --enable-const \
    --enable-ext-colors \
    --enable-ext-mouse \
    --enable-overwrite \
    --enable-termcap \
    --enable-widec \
    --without-ada \
    --without-cxx \
    --without-cxx-binding \
    --without-debug \
    --without-tests \
    --with-normal \
    --with-shared \
    --without-static

  make -j"$(nproc)"
  make DESTDIR="$stage" install
}

build_android_support() {
  local abi="$1"
  local build_dir="$ROOT/build/libandroid-support-${ANDROID_SUPPORT_VERSION}-$abi"
  local wcwidth_dir="$ROOT/build/wcwidth-${WCWIDTH_VERSION}-$abi"
  local stage="$ROOT/stage/$abi"
  local prefix="$stage/$RUNTIME_PREFIX"

  rm -rf "$build_dir" "$wcwidth_dir"
  mkdir -p "$build_dir" "$wcwidth_dir" "$prefix/lib" "$prefix/include"
  tar -xf "$ROOT/src/libandroid-support-${ANDROID_SUPPORT_VERSION}.tar.gz" -C "$build_dir" --strip-components=1
  tar -xf "$ROOT/src/wcwidth-${WCWIDTH_VERSION}.tar.gz" -C "$wcwidth_dir" --strip-components=1
  cp "$wcwidth_dir/wcwidth.c" "$build_dir/src/"
  cd "$build_dir"

  export CPPFLAGS="-D__USE_FORTIFY_LEVEL=0"
  export CFLAGS="-Os -fPIC"
  export LDFLAGS="-shared"

  mkdir -p objects
  local c_file
  for c_file in $(find src -type f -iname '*.c' | sort); do
    "$CC" $CPPFLAGS $CFLAGS -std=c99 -DNULL=0 -Iinclude \
      -c "$c_file" -o "./objects/$(basename "$c_file").o"
  done

  cd objects
  "$AR" rcu ../libandroid-support.a *.o
  "$CC" -shared -o ../libandroid-support.so *.o
  "$RANLIB" ../libandroid-support.a
  cp ../libandroid-support.a "$prefix/lib/"
  cp ../libandroid-support.so "$prefix/lib/"
}

build_coreutils() {
  local abi="$1"
  local build_dir="$ROOT/build/coreutils-${COREUTILS_VERSION}-$abi"
  local stage="$ROOT/stage/$abi"
  local src="$stage/$RUNTIME_PREFIX"

  rm -rf "$build_dir"
  mkdir -p "$build_dir"
  tar -xf "$ROOT/src/coreutils-${COREUTILS_VERSION}.tar.xz" -C "$build_dir" --strip-components=1
  cd "$build_dir"

  for patch_file in "$TERMUX_PACKAGES"/coreutils/*.patch; do
    apply_termux_patch "$patch_file"
  done

  export CPPFLAGS="-D__USE_FORTIFY_LEVEL=0"
  export CFLAGS="-Os -fPIE"
  export LDFLAGS="-pie"
  export LIBS=""

  ./configure \
    --host="$HOST" \
    --build="$BUILD_TRIPLET" \
    --prefix="$RUNTIME_PREFIX" \
    --disable-nls \
    --disable-xattr \
    --without-openssl \
    --without-libgmp \
    --with-packager=AndroPy \
    --enable-single-binary=symlinks \
    --enable-no-install-program=pinky,df,users,who,chcon,runcon \
    --enable-install-program=kill \
    gl_cv_host_operating_system=Android \
    ac_cv_func_getpass=yes \
    ac_cv_func_sethostname=yes \
    ac_cv_func_getusershell=no \
    ac_cv_header_sys_random_h=no

  make -j"$(nproc)"
  make DESTDIR="$stage" install
  "$STRIP" "$src/bin/coreutils"
}

build_nano() {
  local abi="$1"
  local build_dir="$ROOT/build/nano-${NANO_VERSION}-$abi"
  local stage="$ROOT/stage/$abi"
  local prefix="$stage/$RUNTIME_PREFIX"

  rm -rf "$build_dir"
  mkdir -p "$build_dir"
  tar -xf "$ROOT/src/nano-${NANO_VERSION}.tar.xz" -C "$build_dir" --strip-components=1
  cd "$build_dir"

  for patch_file in "$TERMUX_PACKAGES"/nano/*.patch; do
    apply_termux_patch "$patch_file"
  done
  patch -p1 < "$ROOT/patches/nano-android-force-utf8-locale.patch"

  export CPPFLAGS="-I$prefix/include -I$prefix/include/ncursesw -D__USE_FORTIFY_LEVEL=0"
  export CFLAGS="-Os -fPIE"
  export LDFLAGS="-pie -L$prefix/lib -Wl,-rpath,$RUNTIME_PREFIX/lib"
  export LIBS="-landroid-support -lncursesw"

  ./configure \
    --host="$HOST" \
    --build="$BUILD_TRIPLET" \
    --prefix="$RUNTIME_PREFIX" \
    --disable-nls \
    --disable-libmagic \
    --enable-utf8 \
    --with-wordbounds \
    ac_cv_header_glob_h=no \
    ac_cv_header_pwd_h=no \
    gl_cv_func_strcasecmp_works=yes \
    ac_cv_lib_ncursesw_initscr=yes

  make -j"$(nproc)"
  mkdir -p "$prefix/bin" "$prefix/etc" "$prefix/share/nano"
  cp src/nano "$prefix/bin/nano"
  cp syntax/*.nanorc "$prefix/share/nano/"
  printf 'include "%s/share/nano/*nanorc"\n' "$RUNTIME_PREFIX" > "$prefix/etc/nanorc"
  "$STRIP" "$prefix/bin/nano"
}

stage_apk_payloads() {
  local abi="$1"
  local prefix="$ROOT/stage/$abi/$RUNTIME_PREFIX"
  local jni="$PROJECT_ROOT/app/src/main/jniLibs/$abi"
  mkdir -p "$jni"
  rm -f "$jni"/libandropy_{coreutils,nano,clear,tset,tool_launcher}.so
  rm -f "$jni"/libandroid-*.so "$jni"/libcrypto.so* "$jni"/libssl.so* "$jni"/libgmp*.so "$jni"/libiconv.so "$jni"/libcharset.so
  rm -f "$jni"/libcurses.so* "$jni"/libncurses.so* "$jni"/libncursesw.so* "$jni"/libtermcap.so* "$jni"/libtic.so* "$jni"/libtinfo.so*

  cp "$prefix/bin/coreutils" "$jni/libandropy_coreutils.so"
  cp "$prefix/bin/nano" "$jni/libandropy_nano.so"
  cp "$prefix/bin/clear" "$jni/libandropy_clear.so"
  cp "$prefix/bin/tset" "$jni/libandropy_tset.so"
  cp "$prefix/lib/libandroid-support.so" "$jni/libandroid-support.so"
  cp -L "$prefix/lib/libncursesw.so" "$jni/libncursesw.so"
  for tool in libandropy_nano.so libandropy_clear.so libandropy_tset.so; do
    patchelf --replace-needed libncursesw.so.6 libncursesw.so "$jni/$tool" 2>/dev/null || true
  done
  patchelf --set-rpath '$ORIGIN' "$jni/libandropy_nano.so" 2>/dev/null || true
  patchelf --set-rpath '$ORIGIN' "$jni/libandropy_clear.so" 2>/dev/null || true
  patchelf --set-rpath '$ORIGIN' "$jni/libandropy_tset.so" 2>/dev/null || true
  "$STRIP" "$jni"/libandropy_{coreutils,nano,clear,tset}.so "$jni/libandroid-support.so" "$jni/libncursesw.so" 2>/dev/null || true
}

stage_assets() {
  local prefix="$ROOT/stage/x86_64/$RUNTIME_PREFIX"
  local assets="$PROJECT_ROOT/app/src/main/assets/runtime-common"
  rm -rf "$assets"
  mkdir -p "$assets/etc" "$assets/share"
  cp "$prefix/etc/nanorc" "$assets/etc/nanorc"
  cp -a "$prefix/share/nano" "$assets/share/nano"
  mkdir -p "$assets/share/terminfo"/{a,d,s,t,x}
  for entry in a/ansi d/dumb s/screen s/screen-256color t/tmux t/tmux-256color x/xterm x/xterm-color x/xterm-256color x/xterm-new; do
    if [[ -f "$prefix/share/terminfo/$entry" ]]; then
      cp "$prefix/share/terminfo/$entry" "$assets/share/terminfo/$entry"
    fi
  done
}

build_launcher() {
  local abi="$1"
  local cc_name="$2"
  "$TOOLCHAIN/bin/$cc_name" -Os -fPIE -pie "$ROOT/andropy-tool-launcher.c" \
    -o "$PROJECT_ROOT/app/src/main/jniLibs/$abi/libandropy_tool_launcher.so"
  "$TOOLCHAIN/bin/llvm-strip" "$PROJECT_ROOT/app/src/main/jniLibs/$abi/libandropy_tool_launcher.so"
}

fetch "$COREUTILS_URL" "$ROOT/src/coreutils-${COREUTILS_VERSION}.tar.xz"
fetch "$NANO_URL" "$ROOT/src/nano-${NANO_VERSION}.tar.xz"
fetch "$NCURSES_URL" "$ROOT/src/ncurses-${NCURSES_VERSION}.tar.gz"
fetch "$ANDROID_SUPPORT_URL" "$ROOT/src/libandroid-support-${ANDROID_SUPPORT_VERSION}.tar.gz"
fetch "$WCWIDTH_URL" "$ROOT/src/wcwidth-${WCWIDTH_VERSION}.tar.gz"

rm -rf "$ROOT/stage"

for abi in x86_64 arm64-v8a; do
  setup_toolchain "$abi"
  build_android_support "$abi"
  build_ncurses "$abi"
  build_coreutils "$abi"
  build_nano "$abi"
  stage_apk_payloads "$abi"
done

stage_assets
build_launcher x86_64 x86_64-linux-android26-clang
build_launcher arm64-v8a aarch64-linux-android26-clang

file "$PROJECT_ROOT/app/src/main/jniLibs/x86_64/libandropy_coreutils.so"
file "$PROJECT_ROOT/app/src/main/jniLibs/x86_64/libandropy_nano.so"
