#!/usr/bin/env bash
set -euo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
SRC="${AQUA_TKINTER_EGL_SRC:-${AQUA_TKINTER_SRC:-$ROOT/comiled-bianaries/sources/aqua-tkinter-egl}}"
TCL_BRANCH="${AQUA_TCL_BRANCH:-core-8-6-branch}"
TK_BRANCH="${AQUA_TK_BRANCH:-core-8-6-branch}"
CPYTHON_TAG="${AQUA_CPYTHON_TAG:-v3.13.13}"

mkdir -p "$SRC"

clone_or_refresh() {
  local url="$1"
  local branch="$2"
  local dir="$3"
  shift 3
  if [[ -d "$dir/.git" ]]; then
    git -C "$dir" fetch --depth 1 origin "$branch"
    git -C "$dir" checkout -q FETCH_HEAD
  else
    git clone --progress --depth 1 --filter=blob:none --sparse --single-branch --branch "$branch" "$url" "$dir"
  fi
  if [[ "$#" -gt 0 ]]; then
    git -C "$dir" sparse-checkout set --no-cone "$@"
  fi
}

clone_cpython_sparse() {
  local dir="$1"
  if [[ ! -d "$dir/.git" ]]; then
    git clone --depth 1 --filter=blob:none --sparse --branch "$CPYTHON_TAG" \
      https://github.com/python/cpython.git "$dir"
  else
    git -C "$dir" fetch --depth 1 origin "refs/tags/$CPYTHON_TAG:refs/tags/$CPYTHON_TAG"
    git -C "$dir" checkout -q "$CPYTHON_TAG"
  fi
  git -C "$dir" sparse-checkout set --no-cone \
    Lib/tkinter \
    Modules/_tkinter.c \
    Modules/clinic/_tkinter.c.h \
    Modules/tkinter.h
}

clone_or_refresh https://github.com/tcltk/tcl.git "$TCL_BRANCH" "$SRC/tcl" \
  generic unix library compat pkgs license.terms README.md ChangeLog
clone_or_refresh https://github.com/tcltk/tk.git "$TK_BRANCH" "$SRC/tk" \
  generic unix library xlib doc license.terms README.md ChangeLog
clone_cpython_sparse "$SRC/cpython"

python3 "$ROOT/comiled-bianaries/tkinter-egl/patch-aqua-tk-android.py" "$SRC"

printf 'Aqua Tkinter shallow sources ready:\n'
printf '  %s/tcl\n' "$SRC"
printf '  %s/tk\n' "$SRC"
printf '  %s/cpython\n' "$SRC"
