"""Android-backed display output for Aqua IDE Python code.

This module sends RGBA frames to the app over an Android local socket. It is
small on purpose: PIL and matplotlib can draw into normal Python image buffers,
then Aqua uploads that buffer into a native Android EGL/OpenGL texture.
"""

from __future__ import annotations

import io
import os
import socket
from typing import Any


DEFAULT_SOCKET = "andropy_display_com.andropy.ide"
_pil_patched = False
_matplotlib_patched = False


def socket_name() -> str:
    return os.environ.get("ANDROPY_DISPLAY_SOCKET") or DEFAULT_SOCKET


def _connect() -> socket.socket:
    sock = socket.socket(socket.AF_UNIX, socket.SOCK_STREAM)
    sock.connect("\0" + socket_name())
    return sock


def _read_response(sock: socket.socket) -> str:
    chunks: list[bytes] = []
    while True:
        part = sock.recv(4096)
        if not part:
            break
        chunks.append(part)
    return b"".join(chunks).decode("utf-8", "replace").strip()


def ping() -> str:
    with _connect() as sock:
        sock.sendall(b"PING\n")
        return _read_response(sock)


def _rgba_from_image(image: Any) -> tuple[int, int, bytes]:
    if hasattr(image, "convert") and hasattr(image, "size"):
        rgba = image.convert("RGBA")
        width, height = rgba.size
        return int(width), int(height), rgba.tobytes()
    if isinstance(image, (bytes, bytearray, memoryview)):
        raise TypeError("raw byte frames need width= and height=")
    raise TypeError("expected a PIL-like image or raw RGBA bytes")


def show(image: Any, *, width: int | None = None, height: int | None = None, title: str = "Aqua display") -> str:
    if isinstance(image, (bytes, bytearray, memoryview)):
        if width is None or height is None:
            raise TypeError("raw RGBA frames need width= and height=")
        frame = bytes(image)
        width = int(width)
        height = int(height)
    else:
        width, height, frame = _rgba_from_image(image)

    expected = width * height * 4
    if len(frame) != expected:
        raise ValueError(f"RGBA frame has {len(frame)} bytes, expected {expected}")

    clean_title = " ".join(str(title or "Aqua display").split())
    header = f"FRAME {width} {height} {len(frame)} {clean_title}\n".encode("utf-8")
    with _connect() as sock:
        sock.sendall(header)
        sock.sendall(frame)
        return _read_response(sock)


def close() -> str:
    with _connect() as sock:
        sock.sendall(b"CLOSE\n")
        return _read_response(sock)


class _Display:
    def buffer(self, image: Any, *, width: int | None = None, height: int | None = None, title: str = "Aqua display") -> str:
        return show(image, width=width, height=height, title=title)


display = _Display()
framebuffer = display


def install_pil() -> bool:
    global _pil_patched
    if _pil_patched:
        return True
    try:
        from PIL import Image
    except Exception:
        return False

    original_show = Image.Image.show

    def aqua_show(self, *args, **kwargs):
        title = kwargs.pop("title", None) or getattr(self, "filename", None) or "PIL image"
        try:
            return show(self, title=title)
        except Exception:
            return original_show(self, *args, **kwargs)

    Image.Image.show = aqua_show
    _pil_patched = True
    return True


def show_matplotlib(fig: Any | None = None, *, title: str = "matplotlib") -> str:
    try:
        from PIL import Image
        if fig is None:
            import matplotlib.pyplot as plt
            fig = plt.gcf()
    except Exception as error:
        raise RuntimeError("matplotlib/PIL are not available") from error

    data = io.BytesIO()
    fig.savefig(data, format="png", bbox_inches="tight")
    data.seek(0)
    with Image.open(data) as image:
        return show(image, title=title)


def install_matplotlib() -> bool:
    global _matplotlib_patched
    if _matplotlib_patched:
        return True
    try:
        import matplotlib.pyplot as plt
    except Exception:
        return False

    def aqua_pyplot_show(*args, **kwargs):
        title = kwargs.pop("title", "matplotlib")
        return show_matplotlib(title=title)

    plt.show = aqua_pyplot_show
    _matplotlib_patched = True
    return True


def install() -> dict[str, bool]:
    return {
        "pil": install_pil(),
        "matplotlib": install_matplotlib(),
    }
