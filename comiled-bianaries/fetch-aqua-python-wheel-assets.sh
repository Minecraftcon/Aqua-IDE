#!/usr/bin/env bash
set -euo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
PYTHON="${PYTHON:-python3}"
REQ="${AQUA_PURE_WHEEL_REQUIREMENTS:-$ROOT/assets/python/requirements-extra-ai-pure.txt}"
OUT="${AQUA_ASSET_WHEEL_OUT:-$ROOT/assets/python/wheels}"
CACHE="${AQUA_PIP_CACHE:-$ROOT/comiled-bianaries/.aqua-wheel-cache}"

mkdir -p "$OUT" "$CACHE"

if [[ ! -f "$REQ" ]]; then
  echo "Missing requirements file: $REQ" >&2
  exit 1
fi

tmp_req="$(mktemp)"
trap 'rm -f "$tmp_req"' EXIT
grep -Ev '^\s*(#|$)' "$REQ" > "$tmp_req"

if [[ ! -s "$tmp_req" ]]; then
  echo "No packages listed in $REQ" >&2
  exit 0
fi

echo "==> Downloading pure Python wheels into $OUT"
"$PYTHON" -m pip download \
  --dest "$OUT" \
  --cache-dir "$CACHE" \
  --resume-retries "${AQUA_PIP_RESUME_RETRIES:-10}" \
  --retries "${AQUA_PIP_RETRIES:-8}" \
  --timeout "${AQUA_PIP_TIMEOUT:-60}" \
  --no-deps \
  --only-binary=:all: \
  --implementation py \
  --python-version 313 \
  --abi none \
  --platform any \
  -r "$tmp_req"

echo "==> Verifying mirrored wheels are py3-none-any"
bad=0
while IFS= read -r wheel; do
  name="${wheel##*/}"
  case "$name" in
    *-py3-none-any.whl|*-py2.py3-none-any.whl|*-py313-none-any.whl) ;;
    *)
      echo "WARN: removing non-universal wheel: $name" >&2
      rm -f "$wheel"
      bad=1
      ;;
  esac
done < <(find "$OUT" -maxdepth 1 -type f -name '*.whl' | sort)

if [[ "$bad" == 1 ]]; then
  echo "Removed non-universal wheels. Native packages must be built separately." >&2
fi

find "$OUT" -maxdepth 1 -type f -name '*.whl' -printf '%f\n' | sort > "$OUT/manifest-extra-ai-wheels.txt"
echo "==> Wrote $OUT/manifest-extra-ai-wheels.txt"
