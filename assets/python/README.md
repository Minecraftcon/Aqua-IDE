# Aqua Python Assets

Prebuilt Python packages for Aqua live here.

Current layout:

- `debs/<arch>/`: Termux-style `.deb` packages containing Python modules.
- `wheels/`: Universal `py3-none-any` Python wheels that should appear in
  Aqua's GitHub Pages pip index before PyPI fallback.
- `requirements-extra-ai-pure.txt`: pure Python packages mirrored by
  `comiled-bianaries/fetch-aqua-python-wheel-assets.sh`.
- `requirements-extra-ai-native.txt`: Android-native packages that must be
  built per ABI before datasets/transformers/tokenizers stacks are complete.
- `requirements-extra-ai.txt`: combined high-level optional AI package list.

Native AI dependency notes:

- `python-tflite-runtime`, `python-numpy`, and `xxhash` are staged as `.deb`
  assets when available.
- `tokenizers`, `safetensors`, `pyarrow`, `pandas`, `regex`, `pyyaml`, and
  `aiohttp` should not be copied from Linux wheels. Build them for Android and
  stage the resulting ABI-specific assets.
