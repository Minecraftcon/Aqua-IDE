#!/usr/bin/env bash
set -euo pipefail

root="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
payload_dir="$root/runtime-payloads"
out_dir="$root/release-assets/runtime-v9"

mkdir -p "$out_dir"

package_one() {
  local abi="$1"
  local name="$2"
  local src="$payload_dir/runtime-$abi"
  local out="$out_dir/$name"

  if [[ ! -d "$src" ]]; then
    echo "missing runtime payload: $src" >&2
    return 1
  fi

  rm -f "$out"
  (
    cd "$src"
    tar -cf - . | zstd -19 -T0 -q -o "$out"
  )
  du -h "$out"
}

package_one "x86_64" "aqua-runtime-x86_64-v9.tar.zst"
package_one "arm64-v8a" "aqua-runtime-arm64-v8a-v9.tar.zst"
