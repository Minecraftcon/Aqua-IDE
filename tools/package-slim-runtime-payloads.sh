#!/usr/bin/env bash
set -euo pipefail

root="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
payload_dir="$root/runtime-payloads"
work_dir="$root/release-assets/runtime-v8-slim-work"
out_dir="$root/release-assets/runtime-v8"

libs=(
  libandroid-support.so
  libandroid-posix-semaphore.so
  libbz2.so libbz2.so.1.0 libbz2.so.1.0.8
  libc++_shared.so
  libcrypt.so
  libcrypto.so libcrypto.so.3
  libexpat.so libexpat.so.1 libexpat.so.1.12.1
  libffi.so
  libgdbm.so libgdbm.so.6 libgdbm.so.6.0.0
  libgdbm_compat.so libgdbm_compat.so.4 libgdbm_compat.so.4.0.0
  liblzma.so liblzma.so.5 liblzma.so.5.8.3
  libncursesw.so libncursesw.so.6 libncursesw.so.6.5
  libpanelw.so libpanelw.so.6 libpanelw.so.6.5
  libpython3.13.so
  libreadline.so libreadline.so.8 libreadline.so.8.3
  libsqlite3.so libsqlite3.so.0 libsqlite3.so.3.53.1 libsqlite3.53.1.so
  libssl.so libssl.so.3
  libz.so libz.so.1 libz.so.1.3.2
)

copy_if_exists() {
  local src="$1"
  local dst="$2"
  [[ -e "$src" ]] || return 0
  mkdir -p "$(dirname "$dst")"
  cp -a "$src" "$dst"
}

package_one() {
  local abi="$1"
  local name="$2"
  local src="$payload_dir/runtime-$abi"
  local slim="$work_dir/runtime-$abi"
  local out="$out_dir/$name"

  if [[ ! -d "$src" ]]; then
    echo "missing runtime payload: $src" >&2
    return 1
  fi

  rm -rf "$slim"
  mkdir -p "$slim/bin" "$slim/lib" "$slim/etc" "$slim/var" "$slim/tmp"

  copy_if_exists "$src/bin/python" "$slim/bin/python"
  copy_if_exists "$src/lib/python3.13" "$slim/lib/python3.13"
  copy_if_exists "$src/etc/tls" "$slim/etc/tls"

  for lib in "${libs[@]}"; do
    copy_if_exists "$src/lib/$lib" "$slim/lib/$lib"
  done

  # Keep runtime metadata dirs present, but do not ship package-manager databases.
  mkdir -p "$slim/var/lib/dpkg/info" "$slim/var/lib/dpkg/triggers" "$slim/var/lib/dpkg/updates"
  : > "$slim/var/lib/dpkg/status"
  : > "$slim/var/lib/dpkg/available"

  rm -f "$out"
  mkdir -p "$out_dir"
  (
    cd "$slim"
    zip -qry "$out" .
  )
  du -sh "$slim" "$out"
}

package_one "x86_64" "aqua-runtime-x86_64-v8.zip"
package_one "arm64-v8a" "aqua-runtime-arm64-v8a-v8.zip"
