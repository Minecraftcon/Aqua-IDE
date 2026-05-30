#!/usr/bin/env bash
set -euo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
WORK="${AQUA_CV_WORK:-$ROOT/comiled-bianaries/.aqua-cv-build}"
OUT="${AQUA_CV_OUT:-$ROOT/comiled-bianaries/out/aqua-cv}"
PY_REPO="$ROOT/docs/python/simple"
APT_REPO="$ROOT/docs/apt"
ARCHES="${AQUA_CV_ARCHES:-x86_64 aarch64}"
TERMUX_PACKAGES_URL="${TERMUX_PACKAGES_URL:-https://github.com/termux/termux-packages.git}"

mkdir -p "$WORK" "$OUT"

if [[ ! -d "$WORK/termux-packages/.git" ]]; then
  git clone --depth 1 "$TERMUX_PACKAGES_URL" "$WORK/termux-packages"
fi

cd "$WORK/termux-packages"
git fetch --depth 1 origin master
git reset --hard FETCH_HEAD

export TERMUX_NDK_VERSION_NUM="${TERMUX_NDK_VERSION_NUM:-29}"
export ANDROID_HOME="${ANDROID_HOME:-$WORK/android-sdk}"
export NDK="${NDK:-$WORK/android-ndk-r${TERMUX_NDK_VERSION_NUM}}"
if [[ ! -d "$NDK" ]]; then
  echo "==> Installing Termux-required Android SDK/NDK into $WORK"
  scripts/setup-android-sdk.sh
fi

packages=(python-numpy opencv)
for arch in $ARCHES; do
  echo "==> Building Aqua CV packages for $arch"
  export TERMUX_APP__PACKAGE_NAME="${ANDROPY_APP_PACKAGE:-com.andropy.ide}"
  export TERMUX_APP_PACKAGE="$TERMUX_APP__PACKAGE_NAME"
  export TERMUX_APP__DATA_DIR="/data/data/$TERMUX_APP__PACKAGE_NAME"
  export TERMUX__ROOTFS="/data/data/$TERMUX_APP__PACKAGE_NAME/files"
  export TERMUX_BASE_DIR="$TERMUX__ROOTFS"
  export TERMUX__HOME="/data/data/$TERMUX_APP__PACKAGE_NAME/files/home"
  export TERMUX_ANDROID_HOME="$TERMUX__HOME"
  export TERMUX__PREFIX="/data/data/$TERMUX_APP__PACKAGE_NAME/files/usr"
  export TERMUX_PREFIX="$TERMUX__PREFIX"
  export TERMUX_PREFIX_CLASSICAL="$TERMUX__PREFIX"
  export TERMUX_TOPDIR="$WORK/topdir-$arch"

  for pkg in "${packages[@]}"; do
    ./build-package.sh -a "$arch" "$pkg"
  done

  mkdir -p "$OUT/debs/$arch"
  find output -maxdepth 1 -type f -name '*.deb' -exec cp -f {} "$OUT/debs/$arch/" \;
done

echo "Aqua CV debs staged under $OUT/debs"
echo "Run comiled-bianaries/update-aqua-package-repos.sh after adding wheels/debs to publish indexes."
