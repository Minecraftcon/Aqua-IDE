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

static int AquaAndroidExchange(const unsigned char *header, size_t header_len,
        const unsigned char *body, size_t body_len, char *response, size_t response_len) {
    const char *socket_name = AquaAndroidSocketName();
    if (header == NULL || header_len == 0) {
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

    const unsigned char *parts[2] = {header, body};
    size_t sizes[2] = {header_len, body != NULL ? body_len : 0};
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

    if (response != NULL && response_len > 0) {
        size_t used = 0;
        while (used + 1 < response_len) {
            ssize_t count = read(fd, response + used, response_len - used - 1);
            if (count < 0) {
                if (errno == EINTR) continue;
                break;
            }
            if (count == 0) break;
            used += (size_t)count;
            if (memchr(response, '\n', used) != NULL) break;
        }
        response[used] = '\0';
    } else {
        char discard[64];
        (void) read(fd, discard, sizeof(discard));
    }
    close(fd);
    return 0;
}

static int AquaAndroidSend(const unsigned char *header, size_t header_len, const unsigned char *body, size_t body_len) {
    return AquaAndroidExchange(header, header_len, body, body_len, NULL, 0);
}

int AquaAndroidPresentRGBA(int width, int height, const unsigned char *rgba, size_t rgba_len, const char *title) {
    if (width <= 0 || height <= 0 || rgba == NULL || rgba_len != (size_t) width * (size_t) height * 4) {
        return -1;
    }

    char header[256];
    int header_len = snprintf(header, sizeof(header), "FRAME %d %d %zu %s\n",
                              width, height, rgba_len, title && title[0] ? title : "Tk");
    if (header_len <= 0 || header_len >= (int) sizeof(header)) {
        errno = EINVAL;
        return -1;
    }

    return AquaAndroidSend((const unsigned char *) header, (size_t) header_len, rgba, rgba_len);
}

int AquaAndroidPresentScene(const char *json, size_t json_len) {
    if (json == NULL || json_len == 0) {
        return -1;
    }

    char header[128];
    int header_len = snprintf(header, sizeof(header), "SCENE %zu\n", json_len);
    if (header_len <= 0 || header_len >= (int) sizeof(header)) {
        errno = EINVAL;
        return -1;
    }

    return AquaAndroidSend((const unsigned char *) header, (size_t) header_len,
                           (const unsigned char *) json, json_len);
}

int AquaAndroidPollEvents(char *response, size_t response_len) {
    static const unsigned char header[] = "POLLEVENTS\n";
    if (response == NULL || response_len == 0) {
        return -1;
    }
    return AquaAndroidExchange(header, sizeof(header) - 1, NULL, 0, response, response_len);
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

/*
 * Preferred Android path for Tk widgets: send a scene operation JSON document
 * so Android renders text and vector primitives with its native Canvas instead
 * of forcing Tk/Python to rasterize fonts into a software RGBA framebuffer.
 */
int AquaAndroidPresentScene(const char *json, size_t json_len);

/*
 * Poll Android-side input events queued by the display view. The response is a
 * single line shaped like: OK [{"type":"touch","action":"down","x":1,"y":2}]
 */
int AquaAndroidPollEvents(char *response, size_t response_len);

#endif
'''.lstrip()


FONT_C = r'''
#include "tkInt.h"
#include "tkFont.h"

#include <string.h>

void AquaAndroidSceneText(Drawable drawable, GC gc, const char *source, int numBytes, int x, int y);

static int
AquaAndroidCharWidth(Tk_Font tkfont)
{
    TkFont *fontPtr = (TkFont *)tkfont;
    if (fontPtr != NULL && fontPtr->fm.maxWidth > 0) {
	return fontPtr->fm.maxWidth;
    }
    return 9;
}

static TkFont *
AquaAndroidInitFont(TkFont *fontPtr, Tk_Window tkwin,
	const TkFontAttributes *faPtr, const char *nativeName)
{
    if (fontPtr == NULL) {
	fontPtr = (TkFont *)ckalloc(sizeof(TkFont));
    }
    memset(fontPtr, 0, sizeof(TkFont));

    if (tkwin != NULL) {
	fontPtr->screen = Tk_Screen(tkwin);
    }
    if (faPtr != NULL) {
	fontPtr->fa = *faPtr;
    }
    if (fontPtr->fa.family == NULL) {
	fontPtr->fa.family = Tk_GetUid(nativeName != NULL ? nativeName : "sans");
    }
    if (fontPtr->fa.size == 0.0) {
	fontPtr->fa.size = -16.0;
    }

    fontPtr->fid = 1;
    fontPtr->fm.ascent = 14;
    fontPtr->fm.descent = 4;
    fontPtr->fm.maxWidth = 9;
    fontPtr->fm.fixed = 0;
    fontPtr->tabWidth = fontPtr->fm.maxWidth * 8;
    fontPtr->underlinePos = 1;
    fontPtr->underlineHeight = 1;
    return fontPtr;
}

void
TkpFontPkgInit(TkMainInfo *mainPtr)
{
    (void)mainPtr;
}

TkFont *
TkpGetNativeFont(Tk_Window tkwin, const char *name)
{
    return AquaAndroidInitFont(NULL, tkwin, NULL, name);
}

TkFont *
TkpGetFontFromAttributes(TkFont *tkFontPtr, Tk_Window tkwin,
	const TkFontAttributes *faPtr)
{
    return AquaAndroidInitFont(tkFontPtr, tkwin, faPtr, NULL);
}

void
TkpDeleteFont(TkFont *tkFontPtr)
{
    /*
     * Generic Tk owns the TkFont allocation and releases it after this
     * platform hook returns. Freeing tkFontPtr here double-frees animated
     * canvas text items when scripts call canvas.delete("all").
     */
    (void)tkFontPtr;
}

void
TkpGetFontFamilies(Tcl_Interp *interp, Tk_Window tkwin)
{
    Tcl_Obj *resultPtr = Tcl_NewObj();
    (void)tkwin;
    Tcl_ListObjAppendElement(interp, resultPtr, Tcl_NewStringObj("sans", -1));
    Tcl_ListObjAppendElement(interp, resultPtr, Tcl_NewStringObj("monospace", -1));
    Tcl_SetObjResult(interp, resultPtr);
}

void
TkpGetSubFonts(Tcl_Interp *interp, Tk_Font tkfont)
{
    (void)tkfont;
    Tcl_SetObjResult(interp, Tcl_NewObj());
}

void
TkpGetFontAttrsForChar(Tk_Window tkwin, Tk_Font tkfont, int c,
	TkFontAttributes *faPtr)
{
    TkFont *fontPtr = (TkFont *)tkfont;
    (void)tkwin;
    (void)c;
    if (fontPtr != NULL && faPtr != NULL) {
	*faPtr = fontPtr->fa;
    }
}

int
TkpMeasureCharsInContext(Tk_Font tkfont, const char *source, int numBytes,
	int rangeStart, int rangeLength, int maxLength, int flags,
	int *lengthPtr)
{
    int width = AquaAndroidCharWidth(tkfont);
    int count = rangeLength;
    (void)source;
    (void)numBytes;
    (void)rangeStart;
    (void)flags;

    if (count < 0) {
	count = 0;
    }
    if (maxLength >= 0 && width > 0 && count * width > maxLength) {
	count = maxLength / width;
	if (count <= 0 && (flags & TK_AT_LEAST_ONE)) {
	    count = 1;
	}
    }
    if (lengthPtr != NULL) {
	*lengthPtr = count * width;
    }
    return count;
}

void
Tk_DrawChars(Display *display, Drawable drawable, GC gc, Tk_Font tkfont,
	const char *source, int numBytes, int x, int y)
{
    (void)display;
    (void)tkfont;
    AquaAndroidSceneText(drawable, gc, source, numBytes, x, y);
}

void
TkpDrawCharsInContext(Display *display, Drawable drawable, GC gc,
	Tk_Font tkfont, const char *source, int numBytes, int rangeStart,
	int rangeLength, int x, int y)
{
    Tk_DrawChars(display, drawable, gc, tkfont, source + rangeStart,
	    rangeLength, x, y);
    (void)numBytes;
}

void
TkpDrawAngledCharsInContext(Display *display, Drawable drawable, GC gc,
	Tk_Font tkfont, const char *source, int numBytes, int rangeStart,
	int rangeLength, double x, double y, double angle)
{
    TkpDrawCharsInContext(display, drawable, gc, tkfont, source, numBytes,
	    rangeStart, rangeLength, (int)x, (int)y);
    (void)angle;
}
'''.lstrip()


README = r'''
# Aqua Android Tk backend

This source tree is patched for Aqua IDE's Android display bridge.

Current backend contract:

- Tk must render to an RGBA memory buffer.
- Tk can still emit a scene operation JSON document through
  `AquaAndroidPresentScene()` for fallback/debug, but the production path is
  the RGBA framebuffer.
- RGBA buffers are sent to Android through the abstract Unix socket named by
  `ANDROPY_DISPLAY_SOCKET`.
- The app uploads that buffer into the Aqua display EGL/OpenGL view.
- X11/Wayland/simulated framebuffer output is not used.
- A small native WM layer tracks Tk toplevels, raises focused windows, and
  draws lightweight title chrome over secondary windows.

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
    write_if_changed(tk / "generic" / "tkAquaAndroidFont.c", FONT_C)
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
