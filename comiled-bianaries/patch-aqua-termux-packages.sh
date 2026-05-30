#!/usr/bin/env bash
set -euo pipefail

if [[ $# -ne 1 ]]; then
  echo "Usage: $0 /path/to/termux-packages" >&2
  exit 2
fi

TREE="$1"
APP_PACKAGE="${ANDROPY_APP_PACKAGE:-com.andropy.ide}"
APP_DATA="/data/data/$APP_PACKAGE"
ROOTFS="$APP_DATA/files"
HOME_DIR="$ROOTFS/home"
PREFIX="$ROOTFS/usr"
APT_REPO="${AQUA_APT_REPO:-https://minecraftcon.github.io/Aqua-IDE/apt}"

cd "$TREE"

python3 - "$APP_PACKAGE" "$APP_DATA" "$ROOTFS" "$HOME_DIR" "$PREFIX" "$APT_REPO" <<'PY'
from pathlib import Path
import sys

app, data, rootfs, home, prefix, apt_repo = sys.argv[1:]

def replace(path, replacements, optional=False):
    p = Path(path)
    if not p.exists():
        if optional:
            return
        raise FileNotFoundError(path)
    text = p.read_text()
    original = text
    for old, new in replacements:
        text = text.replace(old, new)
    if text != original:
        p.write_text(text)

replace("scripts/properties.sh", [
    ('TERMUX_APP__PACKAGE_NAME="com.termux"', f'TERMUX_APP__PACKAGE_NAME="{app}"'),
    ('TERMUX_APP__NAMESPACE="com.termux"', f'TERMUX_APP__NAMESPACE="{app}"'),
    ('TERMUX_API_APP__PACKAGE_NAME="com.termux.api"', 'TERMUX_API_APP__PACKAGE_NAME="com.andropy.ide.api"'),
    ('TERMUX_API_APP__NAMESPACE="com.termux.api"', 'TERMUX_API_APP__NAMESPACE="com.andropy.ide.api"'),
    ('TERMUX_AM_APP__NAMESPACE="com.termux.termuxam"', 'TERMUX_AM_APP__NAMESPACE="com.andropy.ide.termuxam"'),
    ('TERMUX_REPO_APP__PACKAGE_NAME="com.termux"', f'TERMUX_REPO_APP__PACKAGE_NAME="{app}"'),
    ('TERMUX_REPO_APP__DATA_DIR="/data/data/com.termux"', f'TERMUX_REPO_APP__DATA_DIR="{data}"'),
    ('TERMUX_REPO__CORE_DIR="/data/data/com.termux/termux/core"', f'TERMUX_REPO__CORE_DIR="{data}/aqua/core"'),
    ('TERMUX_REPO__APPS_DIR="/data/data/com.termux/termux/app"', f'TERMUX_REPO__APPS_DIR="{data}/aqua/app"'),
    ('TERMUX_REPO__ROOTFS="/data/data/com.termux/files"', f'TERMUX_REPO__ROOTFS="{rootfs}"'),
    ('TERMUX_REPO__HOME="/data/data/com.termux/files/home"', f'TERMUX_REPO__HOME="{home}"'),
    ('TERMUX_REPO__PREFIX="/data/data/com.termux/files/usr"', f'TERMUX_REPO__PREFIX="{prefix}"'),
    ('CGCT_DEFAULT_PREFIX="/data/data/com.termux/files/usr/glibc"', f'CGCT_DEFAULT_PREFIX="{prefix}/glibc"'),
    ('export CGCT_DIR="/data/data/com.termux/cgct"', f'export CGCT_DIR="{data}/cgct"'),
])

replace("scripts/properties.sh", [
    ("/data/data/com.termux", data),
    ("com.termux.api", f"{app}.api"),
    ("com.termux.termuxam", f"{app}.termuxam"),
    ("com.termux", app),
])

replace("scripts/build/termux_step_handle_buildarch.sh", [
    ('local TERMUX_ARCH_FILE=/data/TERMUX_ARCH', 'local TERMUX_ARCH_FILE="$TERMUX_TOPDIR/.data/TERMUX_ARCH"'),
    ('local TERMUX_DATA_BACKUPDIRS=$TERMUX_TOPDIR/_databackups', 'local TERMUX_DATA_BACKUPDIRS="$TERMUX_TOPDIR/.data/_databackups"'),
    ('if [ -d /data/data ]; then', 'if false && [ -d /data/data ]; then'),
    ('mv "$TERMUX_DATA_CURRENT_BACKUPDIR" /data/data', 'mv "$TERMUX_DATA_CURRENT_BACKUPDIR" "$TERMUX_TOPDIR/.data/data"'),
    ('echo "$TERMUX_ARCH" > $TERMUX_ARCH_FILE', 'mkdir -p "$(dirname "$TERMUX_ARCH_FILE")"\n\techo "$TERMUX_ARCH" > "$TERMUX_ARCH_FILE"'),
])

replace("scripts/build/termux_step_setup_variables.sh", [
    ('TERMUX_BUILT_PACKAGES_DIRECTORY="/data/data/.built-packages"', 'TERMUX_BUILT_PACKAGES_DIRECTORY="$TERMUX_TOPDIR/.built-packages"'),
])

replace("scripts/build/termux_step_setup_cgct_environment.sh", [
    ('local PREFIX_TMP_GLIBC="data/data/com.termux/files/usr/glibc"', f'local PREFIX_TMP_GLIBC="{prefix.removeprefix("/")}/glibc"'),
], optional=True)

replace("packages/apt/build.sh", [
    ('TERMUX_PKG_DEPENDS="coreutils, dpkg, findutils, gpgv, grep, libandroid-glob, libbz2, libc++, libiconv, libgcrypt, libgnutls, liblz4, liblzma, sed, termux-keyring, termux-licenses, xxhash, zlib, zstd"',
     'TERMUX_PKG_DEPENDS="coreutils, dpkg, findutils, grep, libandroid-glob, libbz2, libc++, libiconv, libgcrypt, libgnutls, liblz4, liblzma, sed, termux-keyring, termux-licenses, xxhash, zlib, zstd"'),
    ('echo "# The main termux repository, with cloudflare cache"\n\t\techo "deb https://packages-cf.termux.dev/apt/termux-main/ stable main"\n\t\techo "# The main termux repository, without cloudflare cache"\n\t\techo "# deb https://packages.termux.dev/apt/termux-main/ stable main"',
     f'echo "# Aqua IDE package repository"\n\t\techo "deb [trusted=yes] {apt_repo} stable main"'),
])

replace("packages/dpkg/build.sh", [
    ('TERMUX_PKG_DEPENDS="bzip2, coreutils, diffutils, gzip, less, libbz2, liblzma, libmd, tar, xz-utils, zlib, zstd"',
     'TERMUX_PKG_DEPENDS="coreutils, diffutils, gzip, less, libbz2, liblzma, libmd, tar, zlib, zstd"'),
])

replace("packages/dpkg/lib-dpkg-path-remove.c.patch", [
    ("/data/data/com.termux", data),
])
PY

echo "Patched Termux package tree for $APP_PACKAGE"
