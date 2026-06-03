#!/usr/bin/env bash
set -euo pipefail

root="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
abi="${1:-x86_64}"
api="${ANDROID_API:-24}"
ndk="${NDK:-/home/shado/envs/android-ndk-r29}"
payload="$root/runtime-payloads/runtime-$abi"
out="$root/comiled-bianaries/opencam/out/$abi"

case "$abi" in
  x86_64)
    triple="x86_64-linux-android"
    suffix=".cpython-313-x86_64-linux-android.so"
    ;;
  arm64-v8a|aarch64)
    abi="arm64-v8a"
    payload="$root/runtime-payloads/runtime-arm64-v8a"
    triple="aarch64-linux-android"
    suffix=".cpython-313-aarch64-linux-android.so"
    ;;
  *)
    echo "unsupported abi: $abi" >&2
    exit 2
    ;;
esac

cxx="$ndk/toolchains/llvm/prebuilt/linux-x86_64/bin/${triple}${api}-clang++"
if [[ ! -x "$cxx" ]]; then
  echo "missing compiler: $cxx" >&2
  exit 1
fi
if [[ ! -f "$payload/include/python3.13/Python.h" ]]; then
  echo "missing Python headers under $payload" >&2
  exit 1
fi

mkdir -p "$out"
"$cxx" \
  -shared -fPIC -Oz -std=c++17 \
  -I"$payload/include/python3.13" \
  "$root/comiled-bianaries/opencam/opencam.cpp" \
  -L"$payload/lib" -lpython3.13 \
  -o "$out/opencam$suffix"

echo "$out/opencam$suffix"
