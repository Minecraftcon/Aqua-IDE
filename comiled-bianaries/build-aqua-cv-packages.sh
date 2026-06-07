#!/usr/bin/env bash
set -euo pipefail
#
# Useful modes:
#   AQUA_PREFETCH_ONLY=1 bash comiled-bianaries/build-aqua-cv-packages.sh
#     Download/cache all selected package sources first, then stop.
#   AQUA_PREFETCH=1 bash comiled-bianaries/build-aqua-cv-packages.sh
#     Download/cache selected package sources first, then build. This is default.
#   AQUA_PREFETCH=0 bash comiled-bianaries/build-aqua-cv-packages.sh
#     Build immediately, reusing existing caches and .built-packages markers.
#   AQUA_STAGE_ONLY=1 bash comiled-bianaries/build-aqua-cv-packages.sh
#     Do not build; split already-built debs into runtime-base/ and assets/.

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
WORK="${AQUA_CV_WORK:-$ROOT/comiled-bianaries/.aqua-cv-build}"
OUT="${AQUA_CV_OUT:-$ROOT/comiled-bianaries/out/aqua-cv}"
BASE_RUNTIME_OUT="${AQUA_BASE_RUNTIME_OUT:-$ROOT/runtime-base}"
ASSETS_OUT="${AQUA_ASSETS_OUT:-${AQUA_FAT_RUNTIME_OUT:-$ROOT/assets}}"
PYTHON_ASSETS_OUT="${AQUA_PYTHON_ASSETS_OUT:-${AQUA_FAT_PYTHON_RUNTIME_OUT:-$ASSETS_OUT/python}}"
ARCHES="${AQUA_CV_ARCHES:-x86_64 aarch64}"
BUILD_JOBS="${AQUA_BUILD_JOBS:-10}"
TERMUX_PACKAGES_URL="${TERMUX_PACKAGES_URL:-https://github.com/termux/termux-packages.git}"
AQUA_PREFETCH="${AQUA_PREFETCH:-1}"
AQUA_PREFETCH_ONLY="${AQUA_PREFETCH_ONLY:-0}"
AQUA_STAGE_ONLY="${AQUA_STAGE_ONLY:-0}"
AQUA_STAGE_CLEAN="${AQUA_STAGE_CLEAN:-1}"
AQUA_SYNC_PREFIX_FROM_OUTPUT="${AQUA_SYNC_PREFIX_FROM_OUTPUT:-1}"
AQUA_SYNC_DEB_DIRS="${AQUA_SYNC_DEB_DIRS:-output $ROOT/docs/apt/pool/main $OUT/debs $BASE_RUNTIME_OUT/debs $ASSETS_OUT/debs $PYTHON_ASSETS_OUT/debs}"
AQUA_ENABLE_REAL_TKINTER="${AQUA_ENABLE_REAL_TKINTER:-1}"
AQUA_TKINTER_EGL_SOURCE_ROOT="${AQUA_TKINTER_EGL_SOURCE_ROOT:-${AQUA_TKINTER_SOURCE_ROOT:-$ROOT/comiled-bianaries/sources/aqua-tkinter-egl}}"
AQUA_EXCLUDED_RUNTIME_PACKAGES="${AQUA_EXCLUDED_RUNTIME_PACKAGES:-\
dialog dialog-static glib glib-cross termux-am termux-am-socket termux-exec termux-exec-static}"
AQUA_FAT_PACKAGES="${AQUA_FAT_PACKAGES:-\
blas-openblas clang cmake cmake-curses-gui libcompiler-rt libllvm libllvm-static \
libopenblas libopenblas-static libpolly lld llvm llvm-tools make ndk-sysroot ninja \
pkg-config}"
AQUA_FAT_PYTHON_PACKAGES="${AQUA_FAT_PYTHON_PACKAGES:-\
freetype giflib libjpeg-turbo libpng libtiff libwebp littlecms openjpeg \
python-numpy python-numpy-static python-pillow python-tflite-runtime python-kivy \
xxhash}"
AQUA_APT_PACKAGES="${AQUA_APT_PACKAGES:-\
termux-core termux-keyring termux-licenses termux-elf-cleaner \
apt dpkg bash brotli ca-certificates coreutils diffutils findutils gawk grep \
gzip less nano ncurses readline sed tar unzip util-linux xxhash zlib zstd \
libandroid-glob libandroid-posix-semaphore libandroid-shmem libandroid-spawn \
libandroid-support libbz2 libc++ libcrypt libcurl libexpat libffi libgcrypt \
libgnutls libiconv liblz4 liblzma libmd libpng libsqlite libxml2 openssl \
cmake make ninja patch pkg-config ndk-sysroot libllvm tcl tk \
python python-pip python-numpy python-pillow python-tflite-runtime python-kivy \
libopenblas libwayland libwayland-protocols libxkbcommon xkeyboard-config \
xwayland sway swaybg}"
AQUA_MOBILE_WAYLAND_PACKAGES="${AQUA_MOBILE_WAYLAND_PACKAGES:-}"

mkdir -p "$WORK" "$OUT" "$BASE_RUNTIME_OUT" "$ASSETS_OUT" "$PYTHON_ASSETS_OUT"

HOST_SHIMS="$WORK/host-shims"
mkdir -p "$HOST_SHIMS"
rm -f "$HOST_SHIMS/automake-1.16"
cat > "$HOST_SHIMS/automake-1.16" <<'EOF'
#!/usr/bin/env bash
# Release tarballs already ship generated Makefile.in files. If maintainer
# timestamps ask for automake-1.16 on hosts that only have another Automake,
# do not regenerate with the wrong version.
echo "aqua: skipping unavailable automake-1.16 regeneration in $(pwd)" >&2
exit 0
EOF
chmod +x "$HOST_SHIMS/automake-1.16"
export PATH="$HOST_SHIMS:$PATH"

SOURCE_TREE="${AQUA_CV_SOURCE_TREE:-$ROOT/comiled-bianaries/sources/termux-packages-aqua}"
if [[ ! -d "$WORK/termux-packages/.git" ]]; then
  if [[ -f "$SOURCE_TREE/build-package.sh" ]]; then
    cp -a "$SOURCE_TREE" "$WORK/termux-packages"
  else
    git clone --depth 1 "$TERMUX_PACKAGES_URL" "$WORK/termux-packages"
  fi
fi

cd "$WORK/termux-packages"
if [[ "${AQUA_CV_REFRESH_SOURCES:-0}" == 1 && -d .git ]]; then
  git fetch --depth 1 origin master
  git reset --hard FETCH_HEAD
fi
if [[ -f "$SOURCE_TREE/build-package.sh" && "$SOURCE_TREE" != "$WORK/termux-packages" ]]; then
  for custom_name in python python-kivy tcl tk json-c wlroots sway swaybg phoc \
    hwdata libdisplay-info libseat scdoc libevdev libxkbcommon mtdev \
    xkeyboard-config xwayland; do
    custom_pkg="$SOURCE_TREE/packages/$custom_name"
    [[ -d "$custom_pkg" ]] || continue
    target_pkg="$WORK/termux-packages/packages/${custom_pkg##*/}"
    rm -rf "$target_pkg"
    cp -a "$custom_pkg" "$target_pkg"
  done
fi
"$ROOT/comiled-bianaries/patch-aqua-termux-packages.sh" "$WORK/termux-packages"
termux_tools_build="$WORK/termux-packages/packages/termux-tools/build.sh"
if [[ -f "$termux_tools_build" ]]; then
  python3 - "$termux_tools_build" <<'PY'
from pathlib import Path
import re
import sys

path = Path(sys.argv[1])
text = path.read_text()
text = re.sub(
    r'TERMUX_PKG_DEPENDS="[^"]*"',
    'TERMUX_PKG_DEPENDS="bzip2, coreutils, curl, dash, diffutils, findutils, gawk, '
    'grep, gzip, less, procps, psmisc, sed, tar, termux-core, util-linux, xz-utils"',
    text,
)
text = text.replace('TERMUX_PKG_SUGGESTS="termux-api"', 'TERMUX_PKG_SUGGESTS=""')
path.write_text(text)
PY
fi

export TERMUX_NDK_VERSION_NUM="${TERMUX_NDK_VERSION_NUM:-29}"
export ANDROID_HOME="${ANDROID_HOME:-/home/shado/envs/android-sdk}"
export NDK="${NDK:-/home/shado/envs/android-ndk-r${TERMUX_NDK_VERSION_NUM}}"
export TERMUX_PACKAGES_OFFLINE=false
export AQUA_ENABLE_REAL_TKINTER
export AQUA_TKINTER_EGL_SOURCE_ROOT
export AQUA_PROJECT_ROOT="$ROOT"
if [[ "$AQUA_ENABLE_REAL_TKINTER" == 1 ]]; then
  if [[ -d "$AQUA_TKINTER_EGL_SOURCE_ROOT/tk" ]]; then
    python3 "$ROOT/comiled-bianaries/tkinter-egl/patch-aqua-tk-android.py" "$AQUA_TKINTER_EGL_SOURCE_ROOT"
  else
    echo "WARN: Aqua Tkinter EGL source tree not found: $AQUA_TKINTER_EGL_SOURCE_ROOT" >&2
  fi
fi
if [[ -f build-tools/.installed ]]; then
  mv -f build-tools/.installed build-tools/.installed.aqua-disabled
fi
if [[ ! -d "$NDK" ]]; then
  echo "==> Installing Termux-required Android SDK/NDK into $WORK"
  scripts/setup-android-sdk.sh
fi

host_app_package="${ANDROPY_APP_PACKAGE:-com.andropy.ide}"
host_app_data="/data/data/$host_app_package"
if [[ ! -w "$host_app_data" ]]; then
  sudo mkdir -p "$host_app_data/files/home" "$host_app_data/files/usr"
  sudo chown -R "$USER" "$host_app_data"
fi

read -r -a requested_packages <<< "$AQUA_APT_PACKAGES $AQUA_MOBILE_WAYLAND_PACKAGES"
package_dirs="$(jq --raw-output 'del(.pkg_format) | keys | .[]' repo.json)"
packages=()
for pkg in "${requested_packages[@]}"; do
  found=0
  for package_dir in $package_dirs; do
    if [[ -d "$package_dir/$pkg" ]]; then
      found=1
      break
    fi
  done
  if [[ "$found" == 1 ]]; then
    packages+=("$pkg")
  else
    echo "WARN: skipping unknown Termux package: $pkg" >&2
  fi
done

progress_bar() {
  local current="$1"
  local total="$2"
  local label="$3"
  local width="${AQUA_PROGRESS_WIDTH:-28}"
  local filled=0
  if (( total > 0 )); then
    filled=$(( current * width / total ))
  fi
  local empty=$(( width - filled ))
  local bar_fill bar_empty
  printf -v bar_fill '%*s' "$filled" ''
  printf -v bar_empty '%*s' "$empty" ''
  bar_fill="${bar_fill// /=}"
  bar_empty="${bar_empty// / }"
  printf '\r[%s%s] %3d/%-3d %s' "$bar_fill" "$bar_empty" "$current" "$total" "$label"
}

progress_line() {
  local current="$1"
  local total="$2"
  local label="$3"
  local width="${AQUA_PROGRESS_WIDTH:-28}"
  local filled=0
  if (( total > 0 )); then
    filled=$(( current * width / total ))
  fi
  local empty=$(( width - filled ))
  local bar_fill bar_empty
  printf -v bar_fill '%*s' "$filled" ''
  printf -v bar_empty '%*s' "$empty" ''
  bar_fill="${bar_fill// /=}"
  bar_empty="${bar_empty// / }"
  printf '[%s%s] %3d/%-3d %s\n' "$bar_fill" "$bar_empty" "$current" "$total" "$label"
}

package_dir_for() {
  local pkg="$1"
  local package_dir
  for package_dir in $package_dirs; do
    if [[ -d "$package_dir/$pkg" ]]; then
      printf '%s\n' "$package_dir/$pkg"
      return 0
    fi
  done
  return 1
}

ordered_prefetch_packages() {
  local pkg target_path line dep dep_path
  declare -A seen=()
  for pkg in "${packages[@]}"; do
    target_path="$(package_dir_for "$pkg")" || continue
    while read -r dep dep_path; do
      [[ -n "${dep:-}" && -n "${dep_path:-}" ]] || continue
      if [[ -z "${seen[$dep]:-}" ]]; then
        seen[$dep]=1
        printf '%s\t%s\n' "$dep" "$dep_path"
      fi
    done < <(python3 scripts/buildorder.py "$target_path" $package_dirs)
    if [[ -z "${seen[$pkg]:-}" ]]; then
      seen[$pkg]=1
      printf '%s\t%s\n' "$pkg" "$target_path"
    fi
  done
}

prefetch_one_source() {
  local arch="$1"
  local pkg="$2"
  local rel_path="$3"

  (
    set -euo pipefail
    export TERMUX_SCRIPTDIR="$PWD"
    export TERMUX_ON_DEVICE_BUILD=false
    export TERMUX_REPO_PKG_FORMAT="$(jq --raw-output '.pkg_format // "debian"' repo.json)"
    export TERMUX_PACKAGE_FORMAT="debian"
    export TERMUX_PACKAGE_LIBRARY="bionic"
    export TERMUX_ARCH="$arch"
    export TERMUX_TOPDIR="$WORK/topdir-$arch"
    export TERMUX_PKG_NAME="$pkg"
    export TERMUX_PKG_BUILDER_DIR="$PWD/$rel_path"
    export TERMUX_PKG_BUILDER_SCRIPT="$TERMUX_PKG_BUILDER_DIR/build.sh"
    export TERMUX_DEBUG_BUILD=false
    export TERMUX_FORCE_BUILD=false
    export TERMUX_FORCE_BUILD_DEPENDENCIES=false
    export TERMUX_INSTALL_DEPS=false
    export TERMUX_CONTINUE_BUILD=false
    export TERMUX_QUIET_BUILD=true
    export TERMUX_PACKAGES_OFFLINE=false

    # Source the minimal same helpers used by build-package.sh, but never start a build.
    source "$TERMUX_SCRIPTDIR/scripts/utils/termux/package/termux_package.sh"
    source "$TERMUX_SCRIPTDIR/scripts/properties.sh"
    source "$TERMUX_SCRIPTDIR/scripts/build/termux_error_exit.sh"
    source "$TERMUX_SCRIPTDIR/scripts/build/termux_download.sh"
    source "$TERMUX_SCRIPTDIR/scripts/build/termux_step_setup_variables.sh"
    source "$TERMUX_SCRIPTDIR/scripts/build/termux_step_handle_buildarch.sh"
    source "$TERMUX_SCRIPTDIR/scripts/build/get_source/termux_step_get_source.sh"
    source "$TERMUX_SCRIPTDIR/scripts/build/get_source/termux_git_clone_src.sh"
    source "$TERMUX_SCRIPTDIR/scripts/build/get_source/termux_download_src_archive.sh"
    source "$TERMUX_SCRIPTDIR/scripts/build/get_source/termux_unpack_src_archive.sh"

    termux_extract_src_archive() { :; }
    termux_download() {
      if [[ $# != 2 ]] && [[ $# != 3 ]]; then
        echo "termux_download(): Invalid arguments - expected <URL> <DESTINATION> [<CHECKSUM>]" >&2
        return 1
      fi
      local url="$1"
      local destination="$2"
      local checksum="${3:-SKIP_CHECKSUM}"
      local actual

      if [[ "$url" =~ ^file://(/[^/]+)+$ ]]; then
        local source="${url:7}"
        if [[ -d "$source" ]]; then
          echo "  local directory -> $(basename "$destination")"
          rm -f "$destination"
          (cd "$(dirname "$source")" && tar -cf "$destination" --exclude=".git" "$(basename "$source")")
          return 0
        fi
        if [[ ! -f "$source" ]]; then
          echo "  missing local source: $source" >&2
          return 1
        fi
        ln -sf "$source" "$destination"
        return 0
      fi

      if [[ -f "$destination" && "$checksum" != "SKIP_CHECKSUM" ]]; then
        actual="$(sha256sum "$destination" | cut -d' ' -f1)"
        if [[ "$actual" == "$checksum" ]]; then
          echo "  cached ok: $(basename "$destination")"
          return 0
        fi
        echo "  stale cache: $(basename "$destination")"
        echo "    expected $checksum"
        echo "    actual   $actual"
        rm -f "$destination"
      elif [[ -f "$destination" && "$checksum" == "SKIP_CHECKSUM" ]]; then
        echo "  cached unchecked: $(basename "$destination")"
        return 0
      fi

      rm -f "$TERMUX_PKG_TMPDIR"/download."${TERMUX_PKG_NAME:-unnamed}".* 2>/dev/null || true
      local tmpfile
      tmpfile="$(mktemp "$TERMUX_PKG_TMPDIR/download.${TERMUX_PKG_NAME:-unnamed}.XXXXXXXXX")"
      echo "  download: $url"
      if ! curl \
        --fail \
        --retry 5 \
        --retry-connrefused \
        --retry-delay 5 \
        --connect-timeout 30 \
        --retry-max-time 240 \
        --speed-limit 1000 \
        --speed-time 90 \
        --location \
        --progress-bar \
        --write-out $'\n  speed: %{speed_download} bytes/s, size: %{size_download} bytes, time: %{time_total}s\n' \
        --output "$tmpfile" \
        "$url"; then
        rm -f "$tmpfile"
        echo "  failed: $url" >&2
        return 1
      fi

      actual="$(sha256sum "$tmpfile" | cut -d' ' -f1)"
      if [[ -n "$checksum" && "$checksum" != "SKIP_CHECKSUM" && "$checksum" != "$actual" ]]; then
        echo "  bad download checksum: $(basename "$destination")" >&2
        echo "    expected $checksum" >&2
        echo "    actual   $actual" >&2
        rm -f "$tmpfile"
        return 1
      fi
      if [[ -z "$checksum" ]]; then
        echo "  warning: no checksum for $(basename "$destination")"
        echo "    sha256 $actual"
      fi
      mv -f "$tmpfile" "$destination"
      echo "  saved: $(basename "$destination")"
    }

    termux_step_setup_variables
    source "$TERMUX_PKG_BUILDER_SCRIPT"
    termux_step_handle_buildarch

    if [[ -n "${TERMUX_PKG_EXCLUDED_ARCHES:=""}" && "$TERMUX_PKG_EXCLUDED_ARCHES" != "${TERMUX_PKG_EXCLUDED_ARCHES/$TERMUX_ARCH/}" ]]; then
      exit 0
    fi
    if [[ "${TERMUX_PKG_METAPACKAGE:-false}" == "true" || "${TERMUX_PKG_SKIP_SRC_EXTRACT:-false}" == "true" || -z "${TERMUX_PKG_SRCURL:-}" ]]; then
      exit 0
    fi

    mkdir -p "$TERMUX_PKG_CACHEDIR" "$TERMUX_PKG_TMPDIR" "$TERMUX_PKG_SRCDIR"
    cd "$TERMUX_PKG_CACHEDIR"
    termux_step_get_source
    rm -rf "$TERMUX_PKG_TMPDIR"
  )
}

prefetch_sources_for_arch() {
  local arch="$1"
  local manifest="$WORK/prefetch-$arch.tsv"
  local done_marker="$WORK/topdir-$arch/.aqua-downloads-done"
  mkdir -p "$WORK/topdir-$arch"
  ordered_prefetch_packages > "$manifest"

  local total current pkg rel_path
  total="$(wc -l < "$manifest" | tr -d ' ')"
  current=0
  echo "==> Prefetching Aqua source assets for $arch ($total packages)"
  while IFS=$'\t' read -r pkg rel_path; do
    current=$(( current + 1 ))
    progress_line "$current" "$total" "$arch/$pkg"
    prefetch_one_source "$arch" "$pkg" "$rel_path"
  done < "$manifest"
  date -Is > "$done_marker"
  echo "==> Prefetch complete for $arch; marker: $done_marker"
}

deb_package_name() {
  local deb="$1"
  local file="${deb##*/}"
  printf '%s\n' "${file%%_*}"
}

is_fat_package() {
  local pkg="$1"
  local fat
  for fat in $AQUA_FAT_PACKAGES; do
    if [[ "$pkg" == "$fat" ]]; then
      return 0
    fi
  done
  return 1
}

is_fat_python_package() {
  local pkg="$1"
  local fat
  for fat in $AQUA_FAT_PYTHON_PACKAGES; do
    if [[ "$pkg" == "$fat" ]]; then
      return 0
    fi
  done
  return 1
}

is_built_for_arch() {
  local arch="$1"
  local pkg="$2"
  [[ -f "$WORK/topdir-$arch/.built-packages/$pkg" ]]
}

deb_matches_arch() {
  local deb="$1"
  local arch="$2"
  [[ "$deb" == *_all.deb || "$deb" == *_"$arch".deb ]]
}

sync_prefix_from_output_debs() {
  local arch="$1"
  local deb deb_dir count=0 package version marker_dir

  [[ "$AQUA_SYNC_PREFIX_FROM_OUTPUT" == 1 ]] || return 0

  echo "==> Syncing $TERMUX__PREFIX from already-built $arch debs"
  marker_dir="$WORK/topdir-$arch/.built-packages"
  mkdir -p "$marker_dir"
  for deb_dir in $AQUA_SYNC_DEB_DIRS; do
    [[ -d "$deb_dir" ]] || continue
    while IFS= read -r deb; do
      deb_matches_arch "$deb" "$arch" || continue
      package="$(dpkg-deb -f "$deb" Package 2>/dev/null || true)"
      version="$(dpkg-deb -f "$deb" Version 2>/dev/null || true)"
      [[ -n "$package" && -n "$version" ]] || continue
      if dpkg-deb --fsys-tarfile "$deb" | tar -tf - | grep -q '^\./data/data/com\.termux/'; then
        echo "WARN: Skipping stale Termux-path deb during sync: $deb" >&2
        continue
      fi
      dpkg-deb --fsys-tarfile "$deb" | tar \
        --extract \
        --directory /data/data \
        --strip-components=3 \
        --no-overwrite-dir \
        --no-same-owner \
        --no-same-permissions
      printf '%s\n' "$version" > "$marker_dir/$package"
      count=$(( count + 1 ))
    done < <(find "$deb_dir" -maxdepth 1 -type f -name '*.deb' | sort)
  done
  echo "==> Synced $count local debs into build prefix"
}

repair_x11_internal_headers() {
  local x11_include="$TERMUX_TOPDIR/libx11/massage$TERMUX__PREFIX/include/X11"

  [[ -f "$x11_include/Xlib.h" && -f "$x11_include/Xlibint.h" ]] || return 0

  mkdir -p "$TERMUX__PREFIX/include/X11"
  cp -a "$x11_include/." "$TERMUX__PREFIX/include/X11/"
  find "$TERMUX__PREFIX/include/X11" -type f -exec chmod 0644 {} +
  echo "==> Refreshed libX11 headers from current build tree"
}

repair_wayland_scanner_for_host_build() {
  local host_scanner="$TERMUX_TOPDIR/libwayland/host-build/src/wayland-scanner"

  [[ -x "$host_scanner" ]] || return 0

  mkdir -p "$TERMUX__PREFIX/bin" "$TERMUX__PREFIX/opt/libwayland/cross/bin"
  install -m 0755 "$host_scanner" "$TERMUX__PREFIX/bin/wayland-scanner"
  install -m 0755 "$host_scanner" "$TERMUX__PREFIX/opt/libwayland/cross/bin/wayland-scanner"
  echo "==> Refreshed host wayland-scanner wrappers in build prefix"
}

repair_python_crossenv_wrappers() {
  local arch="$1"
  local crossenv build_python wrapper

  for crossenv in "$WORK/topdir-$arch"/python*-crossenv-prefix-bionic-"$arch"; do
    [[ -d "$crossenv/build/bin" ]] || continue
    build_python="$(readlink -f "$crossenv/build/bin/python" 2>/dev/null || true)"
    [[ -n "$build_python" && -x "$build_python" ]] || continue
    for wrapper in build-pip build-pip3 build-pip3.13; do
      if [[ ! -x "$crossenv/build/bin/pip" || ! -e "$crossenv/build/bin/${wrapper#build-}" ]]; then
        printf '#!/bin/sh\nexec %s -m pip "$@"\n' "$build_python" > "$crossenv/bin/$wrapper"
        chmod +x "$crossenv/bin/$wrapper"
      fi
    done
  done
}

write_allowed_runtime_packages() {
  local manifest="$1"
  local pkg rel_path subpackage
  : > "$manifest"
  while IFS=$'\t' read -r pkg rel_path; do
    [[ -n "${pkg:-}" && -n "${rel_path:-}" ]] || continue
    printf '%s\n' "$pkg" >> "$manifest"
    if [[ -d "$rel_path" ]]; then
      while IFS= read -r subpackage; do
        subpackage="${subpackage##*/}"
        printf '%s\n' "${subpackage%.subpackage.sh}" >> "$manifest"
      done < <(find "$rel_path" -maxdepth 1 -type f -name '*.subpackage.sh' | sort)
    fi
  done < <(ordered_prefetch_packages)
  sort -u -o "$manifest" "$manifest"
}

is_allowed_runtime_package() {
  local manifest="$1"
  local pkg="$2"
  local excluded
  for excluded in $AQUA_EXCLUDED_RUNTIME_PACKAGES; do
    if [[ "$pkg" == "$excluded" ]]; then
      return 1
    fi
  done
  grep -qxF "$pkg" "$manifest"
}

stage_runtime_debs_for_arch() {
  local arch="$1"
  local base_dir="$BASE_RUNTIME_OUT/debs/$arch"
  local fat_dir="$ASSETS_OUT/debs/$arch"
  local fat_python_dir="$PYTHON_ASSETS_OUT/debs/$arch"
  local mixed_dir="$OUT/debs/$arch"
  local base_manifest="$BASE_RUNTIME_OUT/manifest-$arch.txt"
  local fat_manifest="$ASSETS_OUT/manifest-$arch.txt"
  local fat_python_manifest="$PYTHON_ASSETS_OUT/manifest-$arch.txt"
  local allowed_manifest="$WORK/stage-allowed-packages.txt"
  local deb pkg target_dir

  write_allowed_runtime_packages "$allowed_manifest"
  if [[ "$AQUA_STAGE_CLEAN" == 1 ]]; then
    rm -rf "$base_dir" "$fat_dir" "$fat_python_dir" "$mixed_dir"
  fi
  mkdir -p "$base_dir" "$fat_dir" "$fat_python_dir" "$mixed_dir"
  : > "$base_manifest"
  : > "$fat_manifest"
  : > "$fat_python_manifest"

  while IFS= read -r deb; do
    if ! deb_matches_arch "$deb" "$arch"; then
      continue
    fi
    pkg="$(deb_package_name "$deb")"
    if ! is_allowed_runtime_package "$allowed_manifest" "$pkg"; then
      continue
    fi
    if [[ "$deb" != *_all.deb ]] && ! is_built_for_arch "$arch" "$pkg"; then
      continue
    fi

    cp -f "$deb" "$mixed_dir/"
    if is_fat_python_package "$pkg"; then
      target_dir="$fat_python_dir"
      printf '%s\n' "${deb##*/}" >> "$fat_python_manifest"
    elif is_fat_package "$pkg"; then
      target_dir="$fat_dir"
      printf '%s\n' "${deb##*/}" >> "$fat_manifest"
    else
      target_dir="$base_dir"
      printf '%s\n' "${deb##*/}" >> "$base_manifest"
    fi
    cp -f "$deb" "$target_dir/"
  done < <(find output -maxdepth 1 -type f -name '*.deb' | sort)

  sort -u -o "$base_manifest" "$base_manifest"
  sort -u -o "$fat_manifest" "$fat_manifest"
  sort -u -o "$fat_python_manifest" "$fat_python_manifest"
  echo "==> Staged $arch base runtime debs: $(find "$base_dir" -maxdepth 1 -type f -name '*.deb' | wc -l)"
  echo "==> Staged $arch asset debs:        $(find "$fat_dir" -maxdepth 1 -type f -name '*.deb' | wc -l)"
  echo "==> Staged $arch Python asset debs: $(find "$fat_python_dir" -maxdepth 1 -type f -name '*.deb' | wc -l)"
}

if [[ "$AQUA_STAGE_ONLY" == 1 ]]; then
  for arch in $ARCHES; do
    stage_runtime_debs_for_arch "$arch"
  done
  echo "Base runtime debs staged under $BASE_RUNTIME_OUT/debs"
  echo "Asset debs staged under $ASSETS_OUT/debs"
  echo "Python asset debs staged under $PYTHON_ASSETS_OUT/debs"
  exit 0
fi

if [[ "$AQUA_PREFETCH" == 1 || "$AQUA_PREFETCH_ONLY" == 1 ]]; then
  for arch in $ARCHES; do
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
    prefetch_sources_for_arch "$arch"
  done
fi

if [[ "$AQUA_PREFETCH_ONLY" == 1 ]]; then
  echo "Aqua source assets are cached. Re-run without AQUA_PREFETCH_ONLY=1 to build."
  exit 0
fi

for arch in $ARCHES; do
  echo "==> Building Aqua packages for $arch with $BUILD_JOBS jobs"
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

  prefix_arch_file="$TERMUX__PREFIX/.aqua-build-arch"
  if [[ -f "$prefix_arch_file" && "$(cat "$prefix_arch_file")" != "$arch" ]]; then
    rm -rf "$TERMUX__PREFIX"
  fi
  mkdir -p "$TERMUX__PREFIX" "$TERMUX__HOME"
  echo "$arch" > "$prefix_arch_file"
  sync_prefix_from_output_debs "$arch"
  repair_x11_internal_headers
  repair_wayland_scanner_for_host_build
  repair_python_crossenv_wrappers "$arch"

  for pkg in "${packages[@]}"; do
    ./build-package.sh -j "$BUILD_JOBS" -a "$arch" "$pkg"
  done

  stage_runtime_debs_for_arch "$arch"
done

echo "Aqua debs staged under $OUT/debs"
echo "Base runtime debs staged under $BASE_RUNTIME_OUT/debs"
echo "Asset debs staged under $ASSETS_OUT/debs"
echo "Python asset debs staged under $PYTHON_ASSETS_OUT/debs"
echo "Run comiled-bianaries/update-aqua-package-repos.sh after adding wheels/debs to publish indexes."
