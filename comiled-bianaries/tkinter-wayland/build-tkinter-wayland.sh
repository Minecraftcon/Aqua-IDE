#!/usr/bin/env bash
set -euo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
ARCHES="${AQUA_TKINTER_WAYLAND_ARCHES:-${AQUA_CV_ARCHES:-x86_64 aarch64}}"
PACKAGES="${AQUA_TKINTER_WAYLAND_PACKAGES:-glib json-c libdisplay-info libevdev libseat libwayland libwayland-protocols libxkbcommon mtdev scdoc xcb-util-renderutil wlroots sway xkeyboard-config tcl tk python}"

"$ROOT/comiled-bianaries/tkinter-wayland/fetch-tkinter-wayland-sources.sh"

echo "==> Building Tkinter Wayland lane through real sway/Xwayland dependencies"
echo "    Note: upstream Tk has no native Wayland backend; this builds the supported Unix Tk backend for Xwayland."
echo "    Packages: $PACKAGES"

AQUA_CV_ARCHES="$ARCHES" \
AQUA_APT_PACKAGES="$PACKAGES" \
AQUA_ENABLE_REAL_TKINTER=0 \
AQUA_TKINTER_BACKEND=wayland \
AQUA_PREFETCH="${AQUA_PREFETCH:-1}" \
bash "$ROOT/comiled-bianaries/build-aqua-cv-packages.sh"
