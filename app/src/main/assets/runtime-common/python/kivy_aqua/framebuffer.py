"""Aqua framebuffer helpers for Python UI packages."""

from __future__ import annotations

from typing import Any

import aquadisplay


def show_rgba(frame: bytes | bytearray | memoryview, width: int, height: int, *, title: str = "Kivy") -> str:
    return aquadisplay.show(frame, width=width, height=height, title=title)


def show_scene(scene: dict[str, Any], *, title: str = "Kivy") -> str:
    return aquadisplay.show_scene(scene, title=title)


def poll_events() -> list[dict[str, Any]]:
    return aquadisplay.poll_events()


def close() -> str:
    return aquadisplay.close()
