"""Aqua native Wayland/Vulkan display host helpers.

This opens the Android-side native Vulkan surface that will host the Aqua
Wayland compositor path. It intentionally shares the existing display socket so
scripts can request the accelerated display without JNI bindings.
"""

from __future__ import annotations

import os
import socket


DEFAULT_SOCKET = "andropy_display_com.andropy.ide"


def display_socket_name() -> str:
    return os.environ.get("ANDROPY_DISPLAY_SOCKET") or DEFAULT_SOCKET


def wayland_socket_name() -> str:
    return os.environ.get("ANDROPY_WAYLAND_SOCKET") or os.environ.get("WAYLAND_DISPLAY") or "aqua-wayland-0"


def _connect() -> socket.socket:
    sock = socket.socket(socket.AF_UNIX, socket.SOCK_STREAM)
    sock.connect("\0" + display_socket_name())
    return sock


def _read_response(sock: socket.socket) -> str:
    chunks: list[bytes] = []
    while True:
        part = sock.recv(4096)
        if not part:
            break
        chunks.append(part)
    return b"".join(chunks).decode("utf-8", "replace").strip()


def open(title: str = "Aqua Wayland") -> str:
    clean_title = " ".join(str(title or "Aqua Wayland").split())
    with _connect() as sock:
        sock.sendall(f"WAYLAND {clean_title}\n".encode("utf-8"))
        return _read_response(sock)


def close() -> str:
    with _connect() as sock:
        sock.sendall(b"CLOSE\n")
        return _read_response(sock)


def environment() -> dict[str, str]:
    return {
        "WAYLAND_DISPLAY": wayland_socket_name(),
        "XDG_RUNTIME_DIR": os.environ.get("XDG_RUNTIME_DIR", ""),
        "ANDROPY_WAYLAND_SOCKET": wayland_socket_name(),
    }
