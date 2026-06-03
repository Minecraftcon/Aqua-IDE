#!/usr/bin/env python3
"""Patch shallow Tcl/Tk/CPython sources with Aqua's Android display backend."""

from __future__ import annotations

import sys
from pathlib import Path


BRIDGE_C = r'''
#include "tkAquaAndroidBridge.h"

#include <errno.h>
#include <stddef.h>
#include <stdint.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <sys/socket.h>
#include <sys/un.h>
#include <unistd.h>

static const char *AquaAndroidSocketName(void) {
    const char *socket_name = getenv("ANDROPY_DISPLAY_SOCKET");
    return socket_name && socket_name[0] ? socket_name : "andropy_display_com.andropy.ide";
}

int AquaAndroidPresentRGBA(int width, int height, const unsigned char *rgba, size_t rgba_len, const char *title) {
    const char *socket_name = AquaAndroidSocketName();
    if (width <= 0 || height <= 0 || rgba == NULL || rgba_len != (size_t) width * (size_t) height * 4) {
        return -1;
    }

    int fd = socket(AF_UNIX, SOCK_STREAM | SOCK_CLOEXEC, 0);
    if (fd < 0) return -1;

    struct sockaddr_un addr;
    memset(&addr, 0, sizeof(addr));
    addr.sun_family = AF_UNIX;
    addr.sun_path[0] = '\0';
    size_t name_len = strlen(socket_name);
    if (name_len + 1 >= sizeof(addr.sun_path)) {
        close(fd);
        errno = ENAMETOOLONG;
        return -1;
    }
    memcpy(addr.sun_path + 1, socket_name, name_len);

    socklen_t addr_len = (socklen_t) (offsetof(struct sockaddr_un, sun_path) + 1 + name_len);
    if (connect(fd, (struct sockaddr *) &addr, addr_len) != 0) {
        int saved = errno;
        close(fd);
        errno = saved;
        return -1;
    }

    char header[256];
    int header_len = snprintf(header, sizeof(header), "FRAME %d %d %zu %s\n",
                              width, height, rgba_len, title && title[0] ? title : "Tk");
    if (header_len <= 0 || header_len >= (int) sizeof(header)) {
        close(fd);
        errno = EINVAL;
        return -1;
    }

    const unsigned char *parts[2] = {(const unsigned char *) header, rgba};
    size_t sizes[2] = {(size_t) header_len, rgba_len};
    for (int part = 0; part < 2; part++) {
        const unsigned char *cursor = parts[part];
        size_t remaining = sizes[part];
        while (remaining > 0) {
            ssize_t written = write(fd, cursor, remaining);
            if (written < 0) {
                if (errno == EINTR) continue;
                int saved = errno;
                close(fd);
                errno = saved;
                return -1;
            }
            cursor += written;
            remaining -= (size_t) written;
        }
    }

    char response[64];
    (void) read(fd, response, sizeof(response));
    close(fd);
    return 0;
}
'''.lstrip()


BRIDGE_H = r'''
#ifndef TK_AQUA_ANDROID_BRIDGE_H
#define TK_AQUA_ANDROID_BRIDGE_H

#include <stddef.h>

/*
 * Aqua Android backend seam for Tk.
 *
 * Real Tk integration should call AquaAndroidPresentRGBA() after Tk has
 * rasterized a toplevel/window into an RGBA backing store. The Android app owns
 * the actual visible surface; Tk must not open X11, Wayland, or a simulated
 * framebuffer.
 */
int AquaAndroidPresentRGBA(int width, int height, const unsigned char *rgba, size_t rgba_len, const char *title);

#endif
'''.lstrip()


README = r'''
# Aqua Android Tk backend

This source tree is patched for Aqua IDE's Android display bridge.

Current backend contract:

- Tk must render to an RGBA memory buffer.
- The buffer is sent to Android through the abstract Unix socket named by
  `ANDROPY_DISPLAY_SOCKET`.
- The app paints that buffer into a native Android `ImageView`.
- X11/Wayland/simulated framebuffer output is not used.

Porting target:

1. Build Tcl for Android/Bionic normally.
2. Build Tk with an Aqua Android platform implementation rather than `unix/X11`.
3. Build CPython `Modules/_tkinter.c` against the Android Tcl/Tk libraries.
4. Replace the temporary Python `_tkinter.py` fallback in the app assets with
   the compiled `_tkinter.cpython-313-<arch>-linux-android.so`.

Files injected by Aqua:

- `tk/generic/tkAquaAndroidBridge.c`
- `tk/generic/tkAquaAndroidBridge.h`

These files are deliberately tiny and platform-neutral. The real work is in
hooking Tk's window/display operations to an RGBA backing store and calling the
bridge whenever Tk flushes a window.
'''.lstrip()


def write_if_changed(path: Path, text: str) -> None:
    path.parent.mkdir(parents=True, exist_ok=True)
    if path.exists() and path.read_text(errors="ignore") == text:
        return
    path.write_text(text)


def main() -> int:
    if len(sys.argv) != 2:
        print("usage: patch-aqua-tk-android.py <aqua-tkinter-source-root>", file=sys.stderr)
        return 2
    root = Path(sys.argv[1]).resolve()
    tk = root / "tk"
    cpython = root / "cpython"
    if not tk.is_dir():
        print(f"missing Tk source: {tk}", file=sys.stderr)
        return 1

    write_if_changed(tk / "generic" / "tkAquaAndroidBridge.c", BRIDGE_C)
    write_if_changed(tk / "generic" / "tkAquaAndroidBridge.h", BRIDGE_H)
    write_if_changed(root / "README.aqua-android-tk.md", README)

    if cpython.is_dir():
        note = cpython / "Modules" / "README.aqua-android-_tkinter.md"
        write_if_changed(note, (
            "Build Modules/_tkinter.c against Aqua's Android Tcl/Tk libraries.\n"
            "Do not link against X11. The Tk display backend must use "
            "tkAquaAndroidBridge.c.\n"
        ))

    print(f"patched Aqua Android Tk bridge under {root}")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
