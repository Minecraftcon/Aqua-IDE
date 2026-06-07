"""Aqua IDE Python startup hooks.

Keep this file tiny: it runs for every Python process launched by the IDE.
"""

from __future__ import annotations

import importlib.machinery
import importlib.util
import os
import sys
import sysconfig
import builtins


def _install_native_tkinter_if_present() -> None:
    if "_tkinter" in sys.modules:
        return

    candidates: list[str] = []
    destshared = sysconfig.get_config_var("DESTSHARED")
    if destshared:
        candidates.append(destshared)
    prefix = sys.prefix
    version = f"python{sys.version_info.major}.{sys.version_info.minor}"
    candidates.extend([
        os.path.join(prefix, "lib", version, "lib-dynload"),
        os.path.join(prefix, "lib", "lib-dynload"),
    ])

    for directory in dict.fromkeys(candidates):
        if not directory or not os.path.isdir(directory):
            continue
        for suffix in importlib.machinery.EXTENSION_SUFFIXES:
            path = os.path.join(directory, "_tkinter" + suffix)
            if not os.path.exists(path):
                continue
            spec = importlib.util.spec_from_file_location("_tkinter", path)
            if spec is None or spec.loader is None:
                continue
            module = importlib.util.module_from_spec(spec)
            spec.loader.exec_module(module)
            sys.modules["_tkinter"] = module
            return


try:
    _install_native_tkinter_if_present()
except Exception:
    pass


def _install_android_tk_fullscreen_defaults() -> None:
    width = int(os.environ.get("ANDROPY_DISPLAY_WIDTH") or "0")
    height = int(os.environ.get("ANDROPY_DISPLAY_HEIGHT") or "0")
    scale = float(os.environ.get("ANDROPY_TK_SCALE") or "3.0")
    if width <= 0 or height <= 0:
        return

    def patch_tkinter(tk) -> None:
        if not hasattr(tk, "Tk"):
            return
        if getattr(tk.Tk, "_aqua_android_fullscreen", False):
            return
        original_init = tk.Tk.__init__
        original_mainloop = tk.Tk.mainloop
        original_state = tk.Tk.state

        def apply_scaling(root) -> None:
            try:
                root.tk.call("tk", "scaling", scale)
            except Exception:
                pass

        def apply_fullscreen(root) -> None:
            try:
                root.geometry(f"{width}x{height}+0+0")
                root.minsize(width, height)
                root.grid_rowconfigure(0, weight=1)
                root.grid_columnconfigure(0, weight=1)
            except Exception:
                pass

        def aqua_init(self, *args, **kwargs):
            original_init(self, *args, **kwargs)
            apply_scaling(self)
            apply_fullscreen(self)
            try:
                self.after_idle(lambda: (apply_scaling(self), apply_fullscreen(self)))
            except Exception:
                pass

        def aqua_mainloop(self, *args, **kwargs):
            apply_scaling(self)
            apply_fullscreen(self)
            try:
                self.after_idle(lambda: (apply_scaling(self), apply_fullscreen(self)))
            except Exception:
                pass
            return original_mainloop(self, *args, **kwargs)

        def aqua_state(self, newstate=None):
            if newstate in ("zoomed", "iconic"):
                apply_fullscreen(self)
                return "normal"
            return original_state(self, newstate)

        tk.Tk.__init__ = aqua_init
        tk.Tk.mainloop = aqua_mainloop
        tk.Tk.state = aqua_state
        tk.Tk._aqua_android_fullscreen = True

    original_import = builtins.__import__

    def aqua_import(name, globals=None, locals=None, fromlist=(), level=0):
        module = original_import(name, globals, locals, fromlist, level)
        if name == "tkinter" or name.startswith("tkinter."):
            tk = sys.modules.get("tkinter")
            if tk is not None:
                patch_tkinter(tk)
        return module

    builtins.__import__ = aqua_import

    existing = sys.modules.get("tkinter")
    if existing is not None:
        patch_tkinter(existing)


try:
    _install_android_tk_fullscreen_defaults()
except Exception:
    pass

try:
    import kivy_aqua

    kivy_aqua.install()
except Exception:
    pass
