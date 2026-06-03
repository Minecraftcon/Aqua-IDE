#!/usr/bin/env python3
"""Prune a Termux package source checkout to Aqua IDE runtime package closure."""

from __future__ import annotations

import argparse
import os
import re
import shutil
from pathlib import Path


DEFAULT_PACKAGES = """
termux-core termux-keyring termux-licenses termux-elf-cleaner
apt dpkg bash brotli ca-certificates coreutils diffutils findutils gawk grep
gzip less nano ncurses readline sed tar unzip util-linux xxhash zlib zstd
libandroid-glob libandroid-posix-semaphore libandroid-shmem libandroid-spawn
libandroid-support libbz2 libc++ libcrypt libcurl libexpat libffi libgcrypt
libgnutls libiconv liblz4 liblzma libmd libpng libsqlite libxml2 openssl
cmake make ninja patch pkg-config ndk-sysroot libllvm
python python-pip python-numpy python-tflite-runtime
libopenblas
"""

PACKAGE_GROUPS = ("packages", "x11-packages", "root-packages", "disabled-packages")
DEPEND_FIELDS = (
    "TERMUX_PKG_DEPENDS",
    "TERMUX_PKG_BUILD_DEPENDS",
    "TERMUX_PKG_RECOMMENDS",
    "TERMUX_PKG_SUGGESTS",
)


def read_package_list() -> set[str]:
    raw = os.environ.get("AQUA_APT_PACKAGES", DEFAULT_PACKAGES)
    return {part.strip() for part in raw.split() if part.strip()}


def collect_dirs(tree: Path) -> tuple[dict[str, Path], dict[str, str]]:
    package_dirs: dict[str, Path] = {}
    subpackage_to_parent: dict[str, str] = {}
    for group in PACKAGE_GROUPS:
        base = tree / group
        if not base.is_dir():
            continue
        for child in base.iterdir():
            if not child.is_dir():
                continue
            package_dirs.setdefault(child.name, child)
            for subpkg in child.glob("*.subpackage.sh"):
                subpackage_to_parent[subpkg.name.removesuffix(".subpackage.sh")] = child.name
    return package_dirs, subpackage_to_parent


def shell_value(text: str, key: str) -> str:
    match = re.search(
        rf"^\s*{re.escape(key)}\s*(?:\+?=)\s*(?P<quote>['\"])(?P<value>.*?)(?P=quote)",
        text,
        flags=re.M | re.S,
    )
    if match:
        return match.group("value")
    match = re.search(rf"^\s*{re.escape(key)}\s*(?:\+?=)\s*(?P<value>[^\n#]+)", text, flags=re.M)
    if match:
        return match.group("value")
    return ""


def normalize_dep(dep: str) -> str | None:
    dep = dep.strip().strip("'\"")
    if not dep:
        return None
    dep = dep.split("|", 1)[0].strip()
    dep = dep.split("(", 1)[0].strip()
    dep = dep.split("[", 1)[0].strip()
    dep = dep.split(":", 1)[0].strip()
    dep = dep.strip("<>=! \t\n\r")
    if not dep or "$" in dep or dep.startswith("-"):
        return None
    return dep


def parse_deps(package_dir: Path) -> set[str]:
    build_sh = package_dir / "build.sh"
    if not build_sh.is_file():
        return set()
    text = build_sh.read_text(errors="ignore")
    deps: set[str] = set()
    for field in DEPEND_FIELDS:
        value = shell_value(text, field)
        for raw in re.split(r"[,\n]", value):
            dep = normalize_dep(raw)
            if dep:
                deps.add(dep)
    return deps


def resolve_closure(roots: set[str], package_dirs: dict[str, Path], subpackages: dict[str, str]) -> set[str]:
    keep: set[str] = set()
    queue = list(roots)
    while queue:
        name = queue.pop()
        parent = subpackages.get(name, name)
        if parent in keep:
            continue
        if parent not in package_dirs:
            continue
        keep.add(parent)
        for dep in parse_deps(package_dirs[parent]):
            dep_parent = subpackages.get(dep, dep)
            if dep_parent not in keep:
                queue.append(dep_parent)
    return keep


def prune_group(tree: Path, group: str, keep: set[str], apply: bool) -> tuple[int, int]:
    base = tree / group
    if not base.is_dir():
        return 0, 0
    removed = 0
    kept = 0
    for child in sorted(base.iterdir()):
        if not child.is_dir():
            continue
        if child.name in keep:
            kept += 1
            continue
        removed += 1
        print(f"remove {group}/{child.name}")
        if apply:
            shutil.rmtree(child)
    return kept, removed


def remove_optional_metadata(tree: Path, apply: bool) -> None:
    for name in (".git", ".github", "site"):
        path = tree / name
        if path.exists():
            print(f"remove {path.relative_to(tree)}")
            if apply:
                shutil.rmtree(path)


def main() -> int:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("tree", nargs="?", default="comiled-bianaries/sources/termux-packages-aqua")
    parser.add_argument("--apply", action="store_true", help="actually delete pruned source directories")
    parser.add_argument("--keep-git", action="store_true", help="keep .git/.github metadata")
    args = parser.parse_args()

    tree = Path(args.tree).resolve()
    package_dirs, subpackages = collect_dirs(tree)
    roots = read_package_list()
    keep = resolve_closure(roots, package_dirs, subpackages)

    print(f"requested roots: {len(roots)}")
    print(f"resolved package source closure: {len(keep)}")
    missing = sorted(name for name in roots if name not in package_dirs and name not in subpackages)
    if missing:
        print("missing roots:")
        for name in missing:
            print(f"  {name}")

    total_kept = total_removed = 0
    for group in PACKAGE_GROUPS:
        kept, removed = prune_group(tree, group, keep, args.apply)
        total_kept += kept
        total_removed += removed

    if not args.keep_git:
        remove_optional_metadata(tree, args.apply)

    mode = "applied" if args.apply else "dry-run"
    print(f"{mode}: kept {total_kept} dirs, removed {total_removed} dirs")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
