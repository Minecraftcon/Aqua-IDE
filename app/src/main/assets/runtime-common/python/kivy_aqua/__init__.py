"""Kivy integration hooks for Aqua IDE.

The real framebuffer lives in the Android app. This package only sets the
runtime defaults and exposes helpers used by the patched Kivy package.
"""

from __future__ import annotations

import os


def install() -> bool:
    os.environ.setdefault("KIVY_NO_CONSOLELOG", "1")
    os.environ.setdefault("KIVY_NO_FILELOG", "1")
    os.environ.setdefault("KIVY_WINDOW", "aqua")
    os.environ.setdefault("KIVY_IMAGE", "pil")
    os.environ.setdefault("KIVY_AUDIO", "null")
    os.environ.setdefault("KIVY_CAMERA", "android")
    os.environ.setdefault("KIVY_CLIPBOARD", "android")
    return True


install()
