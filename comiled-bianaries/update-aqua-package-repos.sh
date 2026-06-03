#!/usr/bin/env bash
set -euo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
DOCS="$ROOT/docs"
PY_SIMPLE="$DOCS/python/simple"
APT="$DOCS/apt"
DEB_IN="${AQUA_DEB_IN:-$ROOT/comiled-bianaries/out/aqua-cv/debs}"
WHEEL_IN="${AQUA_WHEEL_IN:-$ROOT/comiled-bianaries/out/aqua-cv/wheels}"
ASSET_WHEEL_IN="${AQUA_ASSET_WHEEL_IN:-$ROOT/assets/python/wheels}"

normalize_wheel_project() {
  local name="$1"
  local project="${name%%-*}"
  project="${project//_/-}"
  printf '%s\n' "${project,,}"
}

copy_wheels_to_simple_index() {
  local source_dir="$1"
  [[ -d "$source_dir" ]] || return 0
  find "$source_dir" -type f -name '*.whl' | while read -r wheel; do
    name="$(basename "$wheel")"
    project="$(normalize_wheel_project "$name")"
    case "$name" in
      numpy-*) project="numpy" ;;
      opencv_python_headless-*|opencv-python-headless-*) project="opencv-python-headless" ;;
      opencv_python-*|opencv-python-*) project="opencv-python" ;;
    esac
    mkdir -p "$PY_SIMPLE/$project"
    cp -f "$wheel" "$PY_SIMPLE/$project/$name"
  done
}

rm -rf "$PY_SIMPLE"
mkdir -p "$PY_SIMPLE"
copy_wheels_to_simple_index "$ASSET_WHEEL_IN"
copy_wheels_to_simple_index "$WHEEL_IN"

projects=()
while IFS= read -r project_dir; do
  projects+=("$(basename "$project_dir")")
done < <(find "$PY_SIMPLE" -mindepth 1 -maxdepth 1 -type d | sort)

for project in "${projects[@]}"; do
  index="$PY_SIMPLE/$project/index.html"
  {
    printf '<!doctype html>\n<html><head><meta charset="utf-8"><title>Links for %s</title></head><body>\n' "$project"
    printf '<h1>Links for %s</h1>\n' "$project"
    find "$PY_SIMPLE/$project" -maxdepth 1 -type f -name '*.whl' -printf '%f\n' | sort | while read -r wheel; do
      printf '<a href="%s">%s</a><br>\n' "$wheel" "$wheel"
    done
    printf '</body></html>\n'
  } > "$index"
done

{
  printf '<!doctype html>\n<html><head><meta charset="utf-8"><title>Aqua Python Index</title></head><body>\n'
  printf '<h1>Aqua Python Index</h1>\n'
  for project in "${projects[@]}"; do
    printf '<a href="%s/">%s</a><br>\n' "$project" "$project"
  done
  printf '</body></html>\n'
} > "$PY_SIMPLE/index.html"

mkdir -p "$APT/pool/main" \
  "$APT/dists/stable/main/binary-all" \
  "$APT/dists/stable/main/binary-aarch64" \
  "$APT/dists/stable/main/binary-x86_64"

if [[ -d "$DEB_IN" ]]; then
  find "$DEB_IN" -type f -name '*.deb' | while read -r deb; do
    cp -f "$deb" "$APT/pool/main/$(basename "$deb")"
  done
fi

for arch in all aarch64 x86_64; do
  packages="$APT/dists/stable/main/binary-$arch/Packages"
  : > "$packages"
  find "$APT/pool/main" -maxdepth 1 -type f -name '*.deb' | sort | while read -r deb; do
    deb_arch="$(dpkg-deb -f "$deb" Architecture 2>/dev/null || true)"
    if [[ "$arch" == "all" ]]; then
      [[ "$deb_arch" == "all" ]] || continue
    else
      [[ "$deb_arch" == "$arch" || "$deb_arch" == "all" ]] || continue
    fi
    rel="${deb#$APT/}"
    {
      dpkg-deb -f "$deb"
      printf 'Filename: %s\n' "$rel"
      printf 'Size: %s\n' "$(stat -c '%s' "$deb")"
      printf 'SHA256: %s\n\n' "$(sha256sum "$deb" | awk '{print $1}')"
    } >> "$packages"
  done
  gzip -kf "$packages"
done

release="$APT/dists/stable/Release"
{
  printf 'Origin: Aqua IDE\n'
  printf 'Label: Aqua IDE\n'
  printf 'Suite: stable\n'
  printf 'Codename: stable\n'
  printf 'Date: %s\n' "$(date -Ru)"
  printf 'Architectures: all aarch64 x86_64\n'
  printf 'Components: main\n'
  printf 'Description: Aqua IDE Android package repository\n'
  printf 'MD5Sum:\n'
  find "$APT/dists/stable" -type f \( -name Packages -o -name Packages.gz \) -printf '%P\n' | sort | while read -r file; do
    path="$APT/dists/stable/$file"
    printf ' %s %16s %s\n' "$(md5sum "$path" | awk '{print $1}')" "$(stat -c '%s' "$path")" "$file"
  done
  printf 'SHA256:\n'
  find "$APT/dists/stable" -type f \( -name Packages -o -name Packages.gz \) -printf '%P\n' | sort | while read -r file; do
    path="$APT/dists/stable/$file"
    printf ' %s %16s %s\n' "$(sha256sum "$path" | awk '{print $1}')" "$(stat -c '%s' "$path")" "$file"
  done
} > "$release"

echo "Updated Aqua Python and APT repository indexes under docs/"
