#!/usr/bin/env bash
set -euo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "$ROOT/.." && pwd)"
REPO="${TERMUX_REPO_URL:-https://packages-cf.termux.dev/apt/termux-main}"
TERMUX_PREFIX="data/data/com.termux/files/usr"
ASSET_ROOT="$PROJECT_ROOT/app/src/main/assets"
WORK="$ROOT/termux-userland"

PACKAGES=(
  apt bash binutils build-essential bzip2 clang command-not-found coreutils curl
  dash debianutils diffutils dos2unix dpkg ed findutils gawk grep gzip inetutils
  less lld llvm make nano ncurses ndk-sysroot net-tools patch pkg-config procps
  psmisc sed tar termux-core termux-exec termux-keyring termux-tools unzip
  util-linux xz-utils
)

mkdir -p "$WORK"

resolve_and_stage() {
  local repo_arch="$1"
  local asset_abi="$2"
  local packages_file="$WORK/Packages.$repo_arch"
  local deb_dir="$WORK/debs-$repo_arch"
  local extract_dir="$WORK/extract-$repo_arch"
  local stage="$ASSET_ROOT/runtime-$asset_abi"

  mkdir -p "$deb_dir"
  curl -L --fail "$REPO/dists/stable/main/binary-$repo_arch/Packages" -o "$packages_file"

  python3 - "$packages_file" "$deb_dir" "$REPO" "${PACKAGES[@]}" <<'PY'
import os
import re
import subprocess
import sys

packages_file, deb_dir, repo = sys.argv[1:4]
roots = sys.argv[4:]
records = {}

with open(packages_file, encoding="utf-8", errors="replace") as f:
    for block in f.read().split("\n\n"):
        if not block.strip():
            continue
        fields = {}
        current = None
        for line in block.splitlines():
            if line.startswith(" ") and current:
                fields[current] += "\n" + line
                continue
            if ":" in line:
                key, value = line.split(":", 1)
                current = key
                fields[key] = value.strip()
        name = fields.get("Package")
        if name and name not in records:
            records[name] = fields

seen = set()
order = []

def clean_dep(token):
    token = token.strip().split("|", 1)[0]
    token = re.sub(r"\s*\(.*?\)", "", token)
    return token.strip()

def add(name):
    if not name or name in seen:
        return
    if name not in records:
        print(f"missing package metadata: {name}", file=sys.stderr)
        return
    seen.add(name)
    deps = []
    for field in ("Pre-Depends", "Depends"):
        value = records[name].get(field, "")
        if value:
            deps.extend(value.split(","))
    for dep in deps:
        add(clean_dep(dep))
    order.append(name)

for root in roots:
    add(root)

with open(os.path.join(deb_dir, "package-order.txt"), "w") as f:
    for name in order:
        f.write(name + "\n")

for name in order:
    rec = records[name]
    filename = rec["Filename"]
    url = repo.rstrip("/") + "/" + filename
    out = os.path.join(deb_dir, os.path.basename(filename))
    if not os.path.exists(out):
        print(f"download {name}: {filename}")
        subprocess.run(["curl", "-L", "--fail", url, "-o", out], check=True)
PY

  rm -rf "$extract_dir" "$stage"
  mkdir -p "$extract_dir" "$stage"

  while read -r package; do
    local deb
    deb="$(find "$deb_dir" -maxdepth 1 -type f -name "${package}_*.deb" | head -n1)"
    [[ -n "$deb" ]] || continue
    dpkg-deb -x "$deb" "$extract_dir"
  done < "$deb_dir/package-order.txt"

  if [[ -d "$extract_dir/$TERMUX_PREFIX" ]]; then
    find "$extract_dir/$TERMUX_PREFIX" -xtype l -delete
    cp -aL "$extract_dir/$TERMUX_PREFIX/." "$stage/"
  fi

  # Termux currently has clang but no separate gcc package. Provide the
  # conventional compiler command names as direct copies, not scripts.
  if [[ -f "$stage/bin/clang" && ! -f "$stage/bin/gcc" ]]; then
    cp -aL "$stage/bin/clang" "$stage/bin/gcc"
  fi
  if [[ -f "$stage/bin/clang++" && ! -f "$stage/bin/g++" ]]; then
    cp -aL "$stage/bin/clang++" "$stage/bin/g++"
  fi

  find "$stage" -type f \( -path '*/bin/*' -o -path '*/libexec/*' -o -name '*.so*' \) -exec chmod 700 {} +
  while IFS= read -r -d '' file; do
    if file --mime "$file" | grep -qE 'charset=(us-ascii|utf-8|utf-16|iso-8859|unknown-8bit)'; then
      sed -i 's|/data/data/com.termux/files/usr|/data/data/com.andropy.ide/files/usr|g' "$file"
    fi
  done < <(find "$stage" -type f \( -name '*.pc' -o -name '*.la' -o -name '*config' -o -path '*/etc/*' \) -print0)
  echo "staged $asset_abi from $repo_arch at $stage"
  du -sh "$stage"
}

resolve_and_stage x86_64 x86_64
resolve_and_stage aarch64 arm64-v8a
