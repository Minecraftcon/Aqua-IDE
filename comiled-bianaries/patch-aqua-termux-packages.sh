#!/usr/bin/env bash
set -euo pipefail

if [[ $# -ne 1 ]]; then
  echo "Usage: $0 /path/to/termux-packages" >&2
  exit 2
fi

TREE="$1"
APP_PACKAGE="${ANDROPY_APP_PACKAGE:-com.andropy.ide}"
APP_DATA="/data/data/$APP_PACKAGE"
ROOTFS="$APP_DATA/files"
HOME_DIR="$ROOTFS/home"
PREFIX="$ROOTFS/usr"
APT_REPO="${AQUA_APT_REPO:-https://minecraftcon.github.io/Aqua-IDE/apt}"

cd "$TREE"

python3 - "$APP_PACKAGE" "$APP_DATA" "$ROOTFS" "$HOME_DIR" "$PREFIX" "$APT_REPO" <<'PY'
from pathlib import Path
import sys

app, data, rootfs, home, prefix, apt_repo = sys.argv[1:]

def replace(path, replacements, optional=False):
    p = Path(path)
    if not p.exists():
        if optional:
            return
        raise FileNotFoundError(path)
    text = p.read_text()
    original = text
    for old, new in replacements:
        text = text.replace(old, new)
    if text != original:
        p.write_text(text)

def append_unique(path, addition, optional=False):
    p = Path(path)
    if not p.exists():
        if optional:
            return
        raise FileNotFoundError(path)
    text = p.read_text()
    if addition not in text:
        p.write_text(text.rstrip() + "\n" + addition.lstrip())

def replace_between(path, start, end, replacement, optional=False):
    p = Path(path)
    if not p.exists():
        if optional:
            return
        raise FileNotFoundError(path)
    text = p.read_text()
    start_idx = text.find(start)
    if start_idx == -1:
        if optional:
            return
        raise ValueError(f"start marker not found in {path}")
    end_idx = text.find(end, start_idx)
    if end_idx == -1:
        if optional:
            return
        raise ValueError(f"end marker not found in {path}")
    end_idx += len(end)
    p.write_text(text[:start_idx] + replacement + text[end_idx:])

replace("scripts/properties.sh", [
    ('TERMUX_APP__PACKAGE_NAME="com.termux"', f'TERMUX_APP__PACKAGE_NAME="{app}"'),
    ('TERMUX_APP__NAMESPACE="com.termux"', f'TERMUX_APP__NAMESPACE="{app}"'),
    ('TERMUX_API_APP__PACKAGE_NAME="com.termux.api"', 'TERMUX_API_APP__PACKAGE_NAME="com.andropy.ide.api"'),
    ('TERMUX_API_APP__NAMESPACE="com.termux.api"', 'TERMUX_API_APP__NAMESPACE="com.andropy.ide.api"'),
    ('TERMUX_AM_APP__NAMESPACE="com.termux.termuxam"', 'TERMUX_AM_APP__NAMESPACE="com.andropy.ide.termuxam"'),
    ('TERMUX_REPO_APP__PACKAGE_NAME="com.termux"', f'TERMUX_REPO_APP__PACKAGE_NAME="{app}"'),
    ('TERMUX_REPO_APP__DATA_DIR="/data/data/com.termux"', f'TERMUX_REPO_APP__DATA_DIR="{data}"'),
    ('TERMUX_REPO__CORE_DIR="/data/data/com.termux/termux/core"', f'TERMUX_REPO__CORE_DIR="{data}/aqua/core"'),
    ('TERMUX_REPO__APPS_DIR="/data/data/com.termux/termux/app"', f'TERMUX_REPO__APPS_DIR="{data}/aqua/app"'),
    ('TERMUX_REPO__ROOTFS="/data/data/com.termux/files"', f'TERMUX_REPO__ROOTFS="{rootfs}"'),
    ('TERMUX_REPO__HOME="/data/data/com.termux/files/home"', f'TERMUX_REPO__HOME="{home}"'),
    ('TERMUX_REPO__PREFIX="/data/data/com.termux/files/usr"', f'TERMUX_REPO__PREFIX="{prefix}"'),
    ('CGCT_DEFAULT_PREFIX="/data/data/com.termux/files/usr/glibc"', f'CGCT_DEFAULT_PREFIX="{prefix}/glibc"'),
    ('export CGCT_DIR="/data/data/com.termux/cgct"', f'export CGCT_DIR="{data}/cgct"'),
])

replace("scripts/properties.sh", [
    ("/data/data/com.termux", data),
    ("com.termux.api", f"{app}.api"),
    ("com.termux.termuxam", f"{app}.termuxam"),
    ("com.termux", app),
])

replace("scripts/build/termux_step_handle_buildarch.sh", [
    ('local TERMUX_ARCH_FILE=/data/TERMUX_ARCH', 'local TERMUX_ARCH_FILE="$TERMUX_TOPDIR/.data/TERMUX_ARCH"'),
    ('local TERMUX_DATA_BACKUPDIRS=$TERMUX_TOPDIR/_databackups', 'local TERMUX_DATA_BACKUPDIRS="$TERMUX_TOPDIR/.data/_databackups"'),
    ('if [ -d /data/data ]; then', 'if false && [ -d /data/data ]; then'),
    ('mv "$TERMUX_DATA_CURRENT_BACKUPDIR" /data/data', 'mv "$TERMUX_DATA_CURRENT_BACKUPDIR" "$TERMUX_TOPDIR/.data/data"'),
    ('echo "$TERMUX_ARCH" > $TERMUX_ARCH_FILE', 'mkdir -p "$(dirname "$TERMUX_ARCH_FILE")"\n\techo "$TERMUX_ARCH" > "$TERMUX_ARCH_FILE"'),
])

replace("scripts/build/termux_step_setup_variables.sh", [
    ('TERMUX_BUILT_PACKAGES_DIRECTORY="/data/data/.built-packages"', 'TERMUX_BUILT_PACKAGES_DIRECTORY="$TERMUX_TOPDIR/.built-packages"'),
])

replace("scripts/build/termux_step_setup_cgct_environment.sh", [
    ('local PREFIX_TMP_GLIBC="data/data/com.termux/files/usr/glibc"', f'local PREFIX_TMP_GLIBC="{prefix.removeprefix("/")}/glibc"'),
], optional=True)

replace("packages/apt/build.sh", [
    ('TERMUX_PKG_DEPENDS="coreutils, dpkg, findutils, gpgv, grep, libandroid-glob, libbz2, libc++, libiconv, libgcrypt, libgnutls, liblz4, liblzma, sed, termux-keyring, termux-licenses, xxhash, zlib, zstd"',
     'TERMUX_PKG_DEPENDS="coreutils, dpkg, findutils, grep, libandroid-glob, libbz2, libc++, libiconv, libgcrypt, libgnutls, liblz4, liblzma, sed, termux-keyring, termux-licenses, xxhash, zlib, zstd"'),
    ('echo "# The main termux repository, with cloudflare cache"\n\t\techo "deb https://packages-cf.termux.dev/apt/termux-main/ stable main"\n\t\techo "# The main termux repository, without cloudflare cache"\n\t\techo "# deb https://packages.termux.dev/apt/termux-main/ stable main"',
     f'echo "# Aqua IDE package repository"\n\t\techo "deb [trusted=yes] {apt_repo} stable main"'),
    ('-DWITH_DOC_MANPAGES=ON',
     '-DWITH_DOC_MANPAGES=OFF'),
    ('\n\t# apt-transport-tor\n\tln -sfr $TERMUX_PREFIX/lib/apt/methods/http $TERMUX_PREFIX/lib/apt/methods/tor\n\tln -sfr $TERMUX_PREFIX/lib/apt/methods/http $TERMUX_PREFIX/lib/apt/methods/tor+http\n\tln -sfr $TERMUX_PREFIX/lib/apt/methods/https $TERMUX_PREFIX/lib/apt/methods/tor+https\n\n\tlocal dir=$TERMUX_PREFIX/share/apt-transport-tor\n\tmkdir -p $dir\n\tcat > $dir/README.md <<-EOF\n\tTo use this tor transport, you should install tor package first.\n\tUsage example: apt install tor && apt update -o Acquire::http::Proxy=\"socks5h://127.0.0.1:9050\"\n\tEOF',
     ''),
])

Path("packages/apt/apt-transport-tor.subpackage.sh").unlink(missing_ok=True)
for stale_subpackage in [
    "packages/gnupg/scdaemon.subpackage.sh",
    "packages/libllvm/lldb.subpackage.sh",
    "packages/rubberband/rubberband-ladspa.subpackage.sh",
    "packages/rubberband/rubberband-lv2.subpackage.sh",
    "packages/rubberband/rubberband-vamp.subpackage.sh",
]:
    Path(stale_subpackage).unlink(missing_ok=True)

replace("packages/at-spi2-core/build.sh", [
    ('TERMUX_PKG_BUILD_DEPENDS="g-ir-scanner, libxml2"',
     'TERMUX_PKG_BUILD_DEPENDS="libxml2"'),
    ('TERMUX_PKG_DISABLE_GIR=false',
     'TERMUX_PKG_DISABLE_GIR=true'),
    ('-Dintrospection=enabled',
     '-Dintrospection=disabled'),
    ('\ttermux_setup_gir\n\ttermux_setup_glib_cross_pkg_config_wrapper',
     '\ttermux_setup_glib_cross_pkg_config_wrapper'),
], optional=True)

replace("packages/dpkg/build.sh", [
    ('TERMUX_PKG_DEPENDS="bzip2, coreutils, diffutils, gzip, less, libbz2, liblzma, libmd, tar, xz-utils, zlib, zstd"',
     'TERMUX_PKG_DEPENDS="coreutils, diffutils, gzip, less, libbz2, liblzma, libmd, tar, zlib, zstd"'),
])

replace("packages/dpkg/lib-dpkg-path-remove.c.patch", [
    ("/data/data/com.termux", data),
])

replace("packages/ncurses/build.sh", [
    ('TERMUX_PKG_VERSION=(6.6.20260307+really6.5.20250830\n                    9.31\n                    "$(. "$TERMUX_SCRIPTDIR/x11-packages/kitty/build.sh"; echo "$TERMUX_PKG_VERSION")"\n                    "$(. "$TERMUX_SCRIPTDIR/x11-packages/alacritty/build.sh"; echo "$TERMUX_PKG_VERSION")"\n                    "$(. "$TERMUX_SCRIPTDIR/x11-packages/foot/build.sh"; echo "$TERMUX_PKG_VERSION")")',
     'TERMUX_PKG_VERSION=(6.6.20260307+really6.5.20250830\n                    9.31)'),
    ('TERMUX_PKG_SRCURL=("https://github.com/ThomasDickey/ncurses-snapshots/archive/${_SNAPSHOT_COMMIT}.tar.gz"\n                   "https://dist.schmorp.de/rxvt-unicode/Attic/rxvt-unicode-${TERMUX_PKG_VERSION[1]}.tar.bz2"\n                   "$(. "$TERMUX_SCRIPTDIR/x11-packages/kitty/build.sh"; echo "$TERMUX_PKG_SRCURL")"\n                   "$(. "$TERMUX_SCRIPTDIR/x11-packages/alacritty/build.sh"; echo "$TERMUX_PKG_SRCURL")"\n                   "$(. "$TERMUX_SCRIPTDIR/x11-packages/foot/build.sh"; echo "$TERMUX_PKG_SRCURL")")',
     'TERMUX_PKG_SRCURL=("https://github.com/ThomasDickey/ncurses-snapshots/archive/${_SNAPSHOT_COMMIT}.tar.gz"\n                   "https://dist.schmorp.de/rxvt-unicode/Attic/rxvt-unicode-${TERMUX_PKG_VERSION[1]}.tar.bz2")'),
    ('TERMUX_PKG_SHA256=(28cd102efe6a2610e830cc79cf270da6ff0427b2022900a9a36d2761522f9576\n                   aaa13fcbc149fe0f3f391f933279580f74a96fd312d6ed06b8ff03c2d46672e8\n                   "$(. "$TERMUX_SCRIPTDIR/x11-packages/kitty/build.sh"; echo "$TERMUX_PKG_SHA256")"\n                   "$(. "$TERMUX_SCRIPTDIR/x11-packages/alacritty/build.sh"; echo "$TERMUX_PKG_SHA256")"\n                   "$(. "$TERMUX_SCRIPTDIR/x11-packages/foot/build.sh"; echo "$TERMUX_PKG_SHA256")")',
     'TERMUX_PKG_SHA256=(28cd102efe6a2610e830cc79cf270da6ff0427b2022900a9a36d2761522f9576\n                   aaa13fcbc149fe0f3f391f933279580f74a96fd312d6ed06b8ff03c2d46672e8)'),
    ('\tcp "$TERMUX_PKG_TMPDIR"/full-terminfo/a/{alacritty{,+common,-direct},ansi} "$TI/a/"',
     '\tcp "$TERMUX_PKG_TMPDIR"/full-terminfo/a/ansi "$TI/a/"\n\tcp "$TERMUX_PKG_TMPDIR"/full-terminfo/a/alacritty{,+common,-direct} "$TI/a/" 2>/dev/null || true'),
    ('\tcp "$TERMUX_PKG_TMPDIR"/full-terminfo/f/foot{,+base,-direct} "$TI/f/"',
     '\tcp "$TERMUX_PKG_TMPDIR"/full-terminfo/f/foot{,+base,-direct} "$TI/f/" 2>/dev/null || true'),
    ('\tcp "$TERMUX_PKG_TMPDIR"/full-terminfo/k/kitty{,+common,-direct} "$TI/k/"',
     '\tcp "$TERMUX_PKG_TMPDIR"/full-terminfo/k/kitty{,+common,-direct} "$TI/k/" 2>/dev/null || true'),
    ('\ttic -x -o "$TI" "$TERMUX_PKG_SRCDIR/kitty-${TERMUX_PKG_VERSION[2]}/terminfo/kitty.terminfo"\n\ttic -x -e alacritty,alacritty+common,alacritty-direct -o "$TI" "$TERMUX_PKG_SRCDIR/alacritty-${TERMUX_PKG_VERSION[3]}/extra/alacritty.info"',
     '\tif compgen -G "$TERMUX_PKG_SRCDIR/kitty-*/terminfo/kitty.terminfo" >/dev/null; then\n\t\ttic -x -o "$TI" "$TERMUX_PKG_SRCDIR"/kitty-*/terminfo/kitty.terminfo\n\tfi\n\tif compgen -G "$TERMUX_PKG_SRCDIR/alacritty-*/extra/alacritty.info" >/dev/null; then\n\t\ttic -x -e alacritty,alacritty+common,alacritty-direct -o "$TI" "$TERMUX_PKG_SRCDIR"/alacritty-*/extra/alacritty.info\n\tfi'),
    ('\tsed \'s/@default_terminfo@/foot/g\' "$TERMUX_PKG_SRCDIR/foot/foot.info" | \\\n\ttic -x -e foot,foot-direct -o "$TI" -',
     '\tif [[ -f "$TERMUX_PKG_SRCDIR/foot/foot.info" ]]; then\n\t\tsed \'s/@default_terminfo@/foot/g\' "$TERMUX_PKG_SRCDIR/foot/foot.info" | \\\n\t\ttic -x -e foot,foot-direct -o "$TI" -\n\tfi'),
], optional=True)

replace("packages/libbz2/build.sh", [
    ("(cd $TERMUX_PREFIX/lib && ln -s libbz2.so.${TERMUX_PKG_VERSION} libbz2.so.1.0)",
     "(cd $TERMUX_PREFIX/lib && /bin/ln -s libbz2.so.${TERMUX_PKG_VERSION} libbz2.so.1.0)"),
    ("(cd $TERMUX_PREFIX/lib && ln -s libbz2.so.${TERMUX_PKG_VERSION} libbz2.so)",
     "(cd $TERMUX_PREFIX/lib && /bin/ln -s libbz2.so.${TERMUX_PKG_VERSION} libbz2.so)"),
    ("(cd $TERMUX_PREFIX/bin && ln -s bzip2 bzcat)",
     "(cd $TERMUX_PREFIX/bin && /bin/ln -s bzip2 bzcat)"),
    ("(cd $TERMUX_PREFIX/bin && ln -s bzip2 bunzip2)",
     "(cd $TERMUX_PREFIX/bin && /bin/ln -s bzip2 bunzip2)"),
])

replace("packages/liblzo/build.sh", [
    ('TERMUX_PKG_SRCURL=https://fossies.org/linux/misc/lzo-$TERMUX_PKG_VERSION.tar.xz',
     'TERMUX_PKG_SRCURL=https://www.oberhumer.com/opensource/lzo/download/lzo-$TERMUX_PKG_VERSION.tar.gz'),
    ('TERMUX_PKG_SHA256=37ed4369e45944c53306b0d6a36b66f03e5b6aede8849c9b6388f4b62b20b443',
     'TERMUX_PKG_SHA256=c0f892943208266f9b6543b3ae308fab6284c5c90e627931446fb49b4221a072'),
], optional=True)

replace("packages/libdb/build.sh", [
    ('TERMUX_PKG_SRCURL=https://fossies.org/linux/misc/db-${TERMUX_PKG_VERSION}.tar.gz',
     'TERMUX_PKG_SRCURL=https://download.oracle.com/berkeley-db/db-${TERMUX_PKG_VERSION}.tar.gz'),
], optional=True)

replace("packages/psmisc/build.sh", [
    ('TERMUX_PKG_SRCURL=https://fossies.org/linux/misc/psmisc-$TERMUX_PKG_VERSION.tar.xz',
     'TERMUX_PKG_SRCURL=https://downloads.sourceforge.net/project/psmisc/psmisc/psmisc-$TERMUX_PKG_VERSION.tar.xz'),
], optional=True)

replace("packages/libunbound/build.sh", [
    ('TERMUX_PKG_SRCURL=https://nlnetlabs.nl/downloads/unbound/unbound-${TERMUX_PKG_VERSION}.tar.gz',
     'TERMUX_PKG_SRCURL=https://codeload.github.com/NLnetLabs/unbound/tar.gz/refs/tags/release-${TERMUX_PKG_VERSION}'),
    ('TERMUX_PKG_SHA256=0fe8b6277b0959cfd17562debac0aa5f71e0b02dc4ffa9c60271c583edab586f',
     'TERMUX_PKG_SHA256=ff0bfd926600117f46539640b1b45cad5e26080e38a1a6fb64048723ee9e333c'),
], optional=True)

replace("scripts/build/toolchain/termux_setup_toolchain_29.sh", [
    ("export PATH=$TERMUX_STANDALONE_TOOLCHAIN/bin:$PATH",
     "export PATH=$TERMUX_STANDALONE_TOOLCHAIN/bin:/usr/bin:/bin:$PATH"),
], optional=True)

replace("scripts/build/toolchain/termux_setup_toolchain_23c.sh", [
    ("export PATH=$TERMUX_STANDALONE_TOOLCHAIN/bin:$PATH",
     "export PATH=$TERMUX_STANDALONE_TOOLCHAIN/bin:/usr/bin:/bin:$PATH"),
], optional=True)

replace("scripts/build/setup/termux_setup_gir.sh", [
    ("#!/bin/bash-static",
     "#!/bin/bash"),
], optional=True)

replace("scripts/build/setup/termux_setup_build_python.sh", [
    ('CC="clang-${TERMUX_HOST_LLVM_MAJOR_VERSION} -fuse-ld=lld"',
     'CC="${CC_FOR_BUILD:-gcc}"'),
    ('CXX="clang++-${TERMUX_HOST_LLVM_MAJOR_VERSION} -fuse-ld=lld"',
     'CXX="${CXX_FOR_BUILD:-g++}"'),
    ('PATH="/usr/bin"',
     'PATH="/usr/bin:/bin"'),
    ('env -i \\\n\t\t\t\tmake -j "$(nproc)" install',
     'env -i \\\n\t\t\t\tPATH="/usr/bin:/bin" \\\n\t\t\t\tmake -j "$(nproc)" install'),
], optional=True)

replace("packages/libxml2/build.sh", [
    ("-Ddocs=enabled",
     "-Ddocs=disabled"),
    ("-Dpython=enabled",
     "-Dpython=disabled"),
    ('TERMUX_PKG_BUILD_DEPENDS="doxygen, python, readline"',
     'TERMUX_PKG_BUILD_DEPENDS="readline"'),
    ('TERMUX_PKG_BUILD_DEPENDS="python, readline"',
     'TERMUX_PKG_BUILD_DEPENDS="readline"'),
    ('\tif [[ "$TERMUX_ON_DEVICE_BUILD" == "false" ]]; then\n\t\ttermux_download_ubuntu_packages doxygen libclang-cpp18 libclang1-18 libfmt9 libxapian30\n\tfi',
     '\t:'),
], optional=True)

replace("packages/tar/build.sh", [
    ('termux_step_pre_configure() {\n\tCPPFLAGS+=" -D__USE_FORTIFY_LEVEL=0"',
     'termux_step_pre_configure() {\n\tsed -i \'s|tar_LDADD = $(LIBS) ../lib/libtar.a|tar_LDADD = $(LIBS) $(LIBICONV) ../lib/libtar.a|\' "$TERMUX_PKG_SRCDIR/src/Makefile.in"\n\tfind "$TERMUX_PKG_SRCDIR" -name Makefile.in -exec touch {} +\n\ttouch "$TERMUX_PKG_SRCDIR/configure" "$TERMUX_PKG_SRCDIR/aclocal.m4"\n\tCPPFLAGS+=" -D__USE_FORTIFY_LEVEL=0"'),
], optional=True)

replace("packages/util-linux/build.sh", [
    ('termux_step_pre_configure() {\n\tcase "$TERMUX_ARCH_BITS" in',
     'termux_step_pre_configure() {\n\tfind "$TERMUX_PKG_SRCDIR" -name Makefile.in -exec touch {} +\n\ttouch "$TERMUX_PKG_SRCDIR/aclocal.m4" "$TERMUX_PKG_SRCDIR/configure"\n\tcase "$TERMUX_ARCH_BITS" in'),
    ('\n\tLDFLAGS+=" -landroid-posix-semaphore"\n}',
     '\n\tLDFLAGS+=" -landroid-posix-semaphore -landroid-glob"\n}\n\ntermux_step_post_configure() {\n\tfind "$TERMUX_PKG_SRCDIR" -name Makefile.in -exec touch {} +\n\ttouch "$TERMUX_PKG_BUILDDIR/Makefile"\n}'),
], optional=True)

replace("packages/termux-tools/build.sh", [
    ('termux_step_pre_configure() {\n\tautoreconf -vfi\n}',
     'termux_step_pre_configure() {\n\tsed -i "s/SUBDIRS = \\\\. scripts doc mirrors motds src/SUBDIRS = \\\\. scripts mirrors motds src/" "$TERMUX_PKG_SRCDIR/Makefile.am"\n\tautoreconf -vfi\n}'),
], optional=True)

replace("packages/cmake/build.sh", [
    ('-DSPHINX_MAN=ON',
     '-DSPHINX_MAN=OFF'),
], optional=True)

replace("packages/glib/build.sh", [
    ('TERMUX_PKG_DISABLE_GIR=false',
     'TERMUX_PKG_DISABLE_GIR=true'),
    ('-Dintrospection=enabled',
     '-Dintrospection=disabled'),
    ('-Dman-pages=enabled',
     '-Dman-pages=disabled'),
], optional=True)

replace_between("packages/glib/build.sh",
'''termux_step_pre_configure() {''',
'''	export TERMUX_MESON_ENABLE_SOVERSION=1
}''',
'''termux_step_pre_configure() {
	# Aqua disables GIR/introspection, so skip the nested gobject-introspection
	# build path. The upstream Termux helper expects the ldd package tree, which
	# is intentionally pruned from this runtime source set.
	rm -rf "$TERMUX_HOSTBUILD_MARKER"
	CFLAGS+=" -D__BIONIC__=1"
	export TERMUX_MESON_ENABLE_SOVERSION=1
}''',
optional=True)

for gir_package in [
    "packages/harfbuzz/build.sh",
    "packages/pango/build.sh",
    "packages/gdk-pixbuf/build.sh",
    "x11-packages/gtk3/build.sh",
]:
    replace(gir_package, [
        ('TERMUX_PKG_BUILD_DEPENDS="g-ir-scanner, glib-cross"',
         'TERMUX_PKG_BUILD_DEPENDS="glib-cross"'),
        ('TERMUX_PKG_BUILD_DEPENDS="g-ir-scanner"',
         '# TERMUX_PKG_BUILD_DEPENDS intentionally empty for Aqua lean runtime'),
        ('TERMUX_PKG_BUILD_DEPENDS=""',
         '# TERMUX_PKG_BUILD_DEPENDS intentionally empty for Aqua lean runtime'),
        ('TERMUX_PKG_BUILD_DEPENDS="g-ir-scanner, glib-cross, libwayland-protocols, libwayland-cross-scanner, xorgproto"',
         'TERMUX_PKG_BUILD_DEPENDS="glib-cross, libwayland-protocols, libwayland-cross-scanner, xorgproto"'),
        ('TERMUX_PKG_DISABLE_GIR=false',
         'TERMUX_PKG_DISABLE_GIR=true'),
        ('-Dintrospection=enabled',
         '-Dintrospection=disabled'),
    ], optional=True)

replace("packages/libllvm/build.sh", [
    ('-DLLVM_ENABLE_SPHINX=ON',
     '-DLLVM_ENABLE_SPHINX=OFF'),
    ('-DSPHINX_OUTPUT_MAN=ON',
     '-DSPHINX_OUTPUT_MAN=OFF'),
    ('-DLLDB_ENABLE_PYTHON=ON',
     '-DLLDB_ENABLE_PYTHON=OFF'),
    ('-DLLVM_TARGETS_TO_BUILD=all',
     '-DLLVM_TARGETS_TO_BUILD=AArch64;ARM;X86'),
    ('-DLLVM_TARGETS_TO_BUILD=$llvm_target_arch',
     '-DLLVM_TARGETS_TO_BUILD=AArch64;ARM;X86'),
    ('-DLLVM_EXPERIMENTAL_TARGETS_TO_BUILD=ARC;CSKY;M68k;VE',
     '-DLLVM_EXPERIMENTAL_TARGETS_TO_BUILD='),
    ("-DLLVM_ENABLE_PROJECTS='clang;clang-tools-extra;lldb;mlir'",
     "-DLLVM_ENABLE_PROJECTS='clang;clang-tools-extra;lld'"),
    ('ninja -j "$TERMUX_PKG_MAKE_PROCESSES" clang-tblgen clang-tidy-confusable-chars-gen \\\n\t\tlldb-tblgen llvm-tblgen mlir-tblgen mlir-linalg-ods-yaml-gen',
     'ninja -j "$TERMUX_PKG_MAKE_PROCESSES" clang-tblgen clang-tidy-confusable-chars-gen llvm-tblgen'),
    ('local llvm_projects="clang;clang-tools-extra;compiler-rt;lld;lldb;mlir;openmp;polly"',
     'local llvm_projects="clang;clang-tools-extra;compiler-rt;lld;openmp"'),
    ('\tif [[ "$TERMUX_PKG_CMAKE_BUILD" == "Ninja" ]]; then\n\t\tninja -j "$TERMUX_PKG_MAKE_PROCESSES" docs-{llvm,clang}-man\n\telse\n\t\tmake -j "$TERMUX_PKG_MAKE_PROCESSES" docs-{llvm,clang}-man\n\tfi\n\n\tcp docs/man/* "$TERMUX_PREFIX/share/man/man1"\n\tcp tools/clang/docs/man/{clang,diagtool}.1 "$TERMUX_PREFIX/share/man/man1"',
     '\t# Skip generated LLVM/Clang manpages; Sphinx is disabled for Aqua runtime builds.'),
], optional=True)

replace("packages/python-pip/build.sh", [
    ('TERMUX_PKG_PYTHON_COMMON_BUILD_DEPS="docutils, myst_parser, sphinx_copybutton, sphinx_inline_tabs, sphinxcontrib.towncrier, completion"',
     'TERMUX_PKG_PYTHON_COMMON_BUILD_DEPS="completion"'),
    ('\t( # creating pip documentation\n\t\tcd docs/\n\t\tpython pip_sphinxext.py\n\t\tsphinx-build -b man -d build/doctrees/man man build/man -c html --tag man\n\t)\n\n\tinstall -vDm 644 LICENSE.txt -t "$TERMUX_PREFIX/share/licenses/python-pip/"\n\tinstall -vDm 644 docs/build/man/*.1 -t "$TERMUX_PREFIX/share/man/man1/"\n\tinstall -vDm 644 {NEWS,README}.rst -t "$TERMUX_PREFIX/share/doc/python-pip/"',
     '\tinstall -vDm 644 LICENSE.txt -t "$TERMUX_PREFIX/share/licenses/python-pip/"\n\tinstall -vDm 644 {NEWS,README}.rst -t "$TERMUX_PREFIX/share/doc/python-pip/"'),
], optional=True)

replace("packages/python-tflite-runtime/build.sh", [
    ('-DTFLITE_HOST_TOOLS_DIR=$TERMUX_PKG_HOSTBUILD_DIR',
     '-DTFLITE_HOST_TOOLS_DIR=$TERMUX_PKG_HOSTBUILD_DIR\n-DOVERRIDABLE_FETCH_CONTENT_GIT_REPOSITORY_AND_TAG_TO_URL=ON'),
    ('cmake -DCMAKE_POLICY_VERSION_MINIMUM=3.5 "$TERMUX_PKG_SRCDIR"/tensorflow/lite',
     'cmake -DCMAKE_POLICY_VERSION_MINIMUM=3.5 -DOVERRIDABLE_FETCH_CONTENT_GIT_REPOSITORY_AND_TAG_TO_URL=ON "$TERMUX_PKG_SRCDIR"/tensorflow/lite'),
], optional=True)

replace("packages/libclc/build.sh", [
    ('TERMUX_PKG_BUILD_DEPENDS="clang, libc++, libllvm, libllvm-static, lld, llvm, spirv-llvm-translator"',
     'TERMUX_PKG_BUILD_DEPENDS="clang, libc++, libllvm, lld, llvm, spirv-llvm-translator"'),
], optional=True)

replace("packages/spirv-llvm-translator/build.sh", [
    ('TERMUX_PKG_BUILD_DEPENDS="libllvm-static, spirv-headers, spirv-tools"',
     'TERMUX_PKG_BUILD_DEPENDS="libllvm, spirv-headers, spirv-tools"'),
], optional=True)

replace("packages/python-scipy/build.sh", [
    ('TERMUX_PKG_BUILD_DEPENDS="python-numpy-static, pybind11"',
     'TERMUX_PKG_BUILD_DEPENDS="python-numpy, pybind11"'),
], optional=True)

replace("x11-packages/opencv/build.sh", [
    ('TERMUX_PKG_BUILD_DEPENDS="python-numpy-static"',
     'TERMUX_PKG_BUILD_DEPENDS="python-numpy"'),
], optional=True)

replace("packages/termux-exec/build.sh", [
    ('TERMUX_PKG_BUILD_DEPENDS="termux-core-static"',
     'TERMUX_PKG_BUILD_DEPENDS="termux-core"'),
], optional=True)

replace("packages/git/build.sh", [
    ('\t# Installing man requires asciidoc and xmlto, so git uses separate make targets for man pages\n\tmake -j "$TERMUX_PKG_MAKE_PROCESSES" install-man\n\n\tmake -j "$TERMUX_PKG_MAKE_PROCESSES" -C contrib/subtree $TERMUX_PKG_EXTRA_MAKE_ARGS\n\tmake -C contrib/subtree $TERMUX_PKG_EXTRA_MAKE_ARGS "${TERMUX_PKG_MAKE_INSTALL_TARGET}"\n\tmake -j "$TERMUX_PKG_MAKE_PROCESSES" -C contrib/subtree install-man',
     '\t# Skip manpage targets; they require a host asciidoc/xmlto stack and are not needed in the runtime.\n\tmake -j "$TERMUX_PKG_MAKE_PROCESSES" -C contrib/subtree $TERMUX_PKG_EXTRA_MAKE_ARGS\n\tmake -C contrib/subtree $TERMUX_PKG_EXTRA_MAKE_ARGS "${TERMUX_PKG_MAKE_INSTALL_TARGET}"'),
], optional=True)
PY

echo "Patched Termux package tree for $APP_PACKAGE"
