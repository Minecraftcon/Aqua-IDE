#!/usr/bin/env bash
set -euo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
TERMUX_PACKAGES="$ROOT/termux-packages"
APP_PACKAGE="${ANDROPY_APP_PACKAGE:-com.andropy.ide}"
BOOTSTRAP_ARCHES="${ANDROPY_BOOTSTRAP_ARCHES:-x86_64,aarch64}"
TOPDIR="${ANDROPY_TERMUX_TOPDIR:-$ROOT/.termux-build-andropy}"
OUTDIR="$ROOT/out/termux-bootstrap"

if [[ ! -d "$TERMUX_PACKAGES/scripts" ]]; then
  echo "Missing Termux package tree at $TERMUX_PACKAGES" >&2
  exit 1
fi

mkdir -p "$OUTDIR"

cat > "$OUTDIR/package-set.txt" <<'PACKAGES'
apt
bash
bzip2
command-not-found
coreutils
curl
dash
debianutils
diffutils
dos2unix
ed
findutils
gawk
grep
gzip
inetutils
less
lsof
nano
net-tools
patch
procps
psmisc
sed
tar
termux-core
termux-exec
termux-keyring
termux-tools
unzip
util-linux
xz-utils
clang
libllvm
lld
llvm
make
pkg-config
PACKAGES

export TERMUX_APP__PACKAGE_NAME="$APP_PACKAGE"
export TERMUX_APP_PACKAGE="$APP_PACKAGE"
export TERMUX_APP__DATA_DIR="/data/data/$APP_PACKAGE"
export TERMUX__ROOTFS="/data/data/$APP_PACKAGE/files"
export TERMUX_BASE_DIR="$TERMUX__ROOTFS"
export TERMUX__HOME="/data/data/$APP_PACKAGE/files/home"
export TERMUX_ANDROID_HOME="$TERMUX__HOME"
export TERMUX__PREFIX="/data/data/$APP_PACKAGE/files/usr"
export TERMUX_PREFIX="$TERMUX__PREFIX"
export TERMUX_PREFIX_CLASSICAL="$TERMUX__PREFIX"
export TERMUX_TOPDIR="$TOPDIR"

echo "AndroPy Termux bootstrap"
echo "  package:  $APP_PACKAGE"
echo "  prefix:   $TERMUX_PREFIX"
echo "  home:     $TERMUX_ANDROID_HOME"
echo "  arches:   $BOOTSTRAP_ARCHES"
echo "  topdir:   $TERMUX_TOPDIR"
echo "  package set written to $OUTDIR/package-set.txt"
echo

cd "$TERMUX_PACKAGES"

if [[ "${ANDROPY_BOOTSTRAP_DRY_RUN:-0}" == "1" ]]; then
  echo "Dry run only. Command that will be used:"
  printf 'TERMUX_APP__PACKAGE_NAME=%q TERMUX_TOPDIR=%q scripts/build-bootstraps.sh --architectures %q' \
    "$APP_PACKAGE" "$TERMUX_TOPDIR" "$BOOTSTRAP_ARCHES"
  while read -r pkg; do
    [[ -n "$pkg" ]] && printf ' --add %q' "$pkg"
  done < "$OUTDIR/package-set.txt"
  printf '\n'
  exit 0
fi

args=(--architectures "$BOOTSTRAP_ARCHES")
while read -r pkg; do
  [[ -n "$pkg" ]] && args+=(--add "$pkg")
done < "$OUTDIR/package-set.txt"

scripts/build-bootstraps.sh "${args[@]}"

find "$TERMUX_PACKAGES" -maxdepth 1 -name 'bootstrap-*.zip' -print -exec mv -f {} "$OUTDIR/" \;
echo "Bootstrap archives moved to $OUTDIR"
