"""Aqua Android framebuffer fallback for Python's tkinter package.

This is not the final Tcl/Tk C engine. It is a compatibility backend that lets
the standard library ``tkinter`` module import and display common widget trees on
Android while the real Tcl/Tk source port is built. It renders into Aqua IDE's
native display bridge instead of X11.
"""

from __future__ import annotations

import shlex
import time
from dataclasses import dataclass, field
from typing import Any


TCL_VERSION = "8.6"
TK_VERSION = "8.6"
READABLE = 2
WRITABLE = 4
EXCEPTION = 8


class TclError(Exception):
    pass


def _flatten(value):
    out = []
    for item in value:
        if isinstance(item, (tuple, list)):
            out.extend(_flatten(item))
        elif item is not None:
            out.append(item)
    return tuple(out)


def create(screenName=None, baseName=None, className="Tk", interactive=False,
           wantobjects=True, useTk=True, sync=False, use=None):
    return AquaTkApp(baseName or "tk")


def getbusywaitinterval():
    return 20


def setbusywaitinterval(interval):
    return None


def createfilehandler(*args, **kwargs):
    return None


def deletefilehandler(*args, **kwargs):
    return None


@dataclass
class WidgetState:
    path: str
    kind: str
    options: dict[str, Any] = field(default_factory=dict)
    children: list[str] = field(default_factory=list)
    packed: bool = False
    gridded: bool = False
    placed: bool = False
    canvas_items: list[tuple[str, tuple[Any, ...], dict[str, Any]]] = field(default_factory=list)


class AquaTkApp:
    def __init__(self, base_name: str):
        self.base_name = base_name
        self.vars: dict[str, Any] = {
            "tk_version": TK_VERSION,
            "tcl_version": TCL_VERSION,
        }
        self.commands: dict[str, Any] = {}
        self.widgets: dict[str, WidgetState] = {
            ".": WidgetState(".", "root", {"background": "#24272e", "title": base_name})
        }
        self._wantobjects = True
        self._quit = False
        self._last_render = 0.0

    def wantobjects(self):
        return self._wantobjects

    def loadtk(self):
        return None

    def settrace(self, trace):
        self.trace = trace

    def createcommand(self, name, func):
        self.commands[name] = func

    def deletecommand(self, name):
        self.commands.pop(name, None)

    def getvar(self, name):
        return self.vars.get(str(name), "")

    def globalgetvar(self, name):
        return self.getvar(name)

    def setvar(self, name, value):
        self.vars[str(name)] = value

    def globalsetvar(self, name, value):
        self.setvar(name, value)

    def unsetvar(self, name):
        self.vars.pop(str(name), None)

    def globalunsetvar(self, name):
        self.unsetvar(name)

    def getboolean(self, value):
        if isinstance(value, bool):
            return value
        return str(value).lower() in {"1", "true", "yes", "on"}

    def getint(self, value):
        return int(float(value))

    def getdouble(self, value):
        return float(value)

    def splitlist(self, value):
        if isinstance(value, (tuple, list)):
            return tuple(value)
        if value is None:
            return ()
        try:
            return tuple(shlex.split(str(value)))
        except ValueError:
            return tuple(str(value).split())

    def eval(self, script):
        result = ""
        for raw in str(script).splitlines():
            line = raw.strip()
            if line:
                result = self.call(*self.splitlist(line))
        return result

    def evalfile(self, path):
        with open(path, "r", encoding="utf-8") as handle:
            return self.eval(handle.read())

    def exprstring(self, value):
        return str(eval(str(value), {"__builtins__": {}}, {}))

    def exprlong(self, value):
        return int(float(self.exprstring(value)))

    def exprdouble(self, value):
        return float(self.exprstring(value))

    def exprboolean(self, value):
        return bool(self.exprlong(value))

    def mainloop(self, threshold=0):
        self.render(force=True)
        while not self._quit:
            time.sleep(0.05)

    def quit(self):
        self._quit = True

    def call(self, *args):
        args = _flatten(args)
        if not args:
            return ""
        cmd = str(args[0])
        rest = args[1:]

        if cmd in {"frame", "label", "button", "canvas", "entry", "text", "listbox", "scrollbar",
                   "checkbutton", "radiobutton", "scale", "spinbox", "toplevel", "labelframe"}:
            return self._create_widget(cmd, rest)
        if cmd == "destroy":
            return self._destroy(rest)
        if cmd == "update":
            self.render(force=True)
            return ""
        if cmd == "after":
            return self._after(rest)
        if cmd == "mainloop":
            return self.mainloop()
        if cmd == "wm":
            return self._wm(rest)
        if cmd == "winfo":
            return self._winfo(rest)
        if cmd == "bind":
            return ""
        if cmd == "focus":
            return ""
        if cmd == "option":
            return ""
        if cmd == "tk":
            return self._tk(rest)
        if cmd in self.widgets:
            return self._widget_call(cmd, rest)
        if cmd in self.commands:
            return self.commands[cmd](*rest)
        return ""

    def _create_widget(self, kind: str, rest: tuple[Any, ...]):
        if not rest:
            raise TclError(f"missing widget path for {kind}")
        path = str(rest[0])
        options = self._parse_options(rest[1:])
        parent = "." if path == "." else path.rsplit(".", 1)[0] or "."
        state = WidgetState(path, kind, options)
        self.widgets[path] = state
        self.widgets.setdefault(parent, WidgetState(parent, "frame")).children.append(path)
        self.render()
        return path

    def _widget_call(self, path: str, rest: tuple[Any, ...]):
        widget = self.widgets[path]
        if not rest:
            return path
        sub = str(rest[0])
        tail = rest[1:]
        if sub in {"configure", "config"}:
            if not tail:
                return ""
            widget.options.update(self._parse_options(tail))
            self.render()
            return ""
        if sub == "cget":
            key = str(tail[0]).lstrip("-") if tail else ""
            return widget.options.get(key, "")
        if sub in {"pack", "grid", "place"}:
            setattr(widget, sub + "ed" if sub != "pack" else "packed", True)
            self.render()
            return ""
        if sub == "insert":
            widget.options["text"] = str(widget.options.get("text", "")) + " ".join(map(str, tail[1:] or tail))
            self.render()
            return ""
        if sub == "delete":
            widget.options["text"] = ""
            self.render()
            return ""
        if widget.kind == "canvas" and (sub.startswith("create_") or sub == "create"):
            if sub == "create":
                if not tail:
                    return ""
                item_kind = str(tail[0])
                tail = tail[1:]
            else:
                item_kind = sub.removeprefix("create_")
            coords: list[Any] = []
            index = 0
            while index < len(tail) and not str(tail[index]).startswith("-"):
                coords.append(tail[index])
                index += 1
            opts = self._parse_options(tail[index:])
            widget.canvas_items.append((item_kind, tuple(coords), opts))
            self.render()
            return str(len(widget.canvas_items))
        if widget.kind == "canvas" and sub in {"delete", "move", "coords", "itemconfigure"}:
            self.render()
            return ""
        return ""

    def _parse_options(self, values: tuple[Any, ...]):
        options = {}
        index = 0
        while index < len(values):
            key = str(values[index])
            if key.startswith("-") and index + 1 < len(values):
                options[key[1:]] = values[index + 1]
                index += 2
            else:
                index += 1
        return options

    def _destroy(self, rest):
        for path in rest or (".",):
            path = str(path)
            for child in list(self.widgets.get(path, WidgetState(path, "")).children):
                self._destroy((child,))
            self.widgets.pop(path, None)
        self.render()
        return ""

    def _after(self, rest):
        if not rest:
            return ""
        try:
            ms = int(rest[0])
        except Exception:
            ms = 0
        if len(rest) > 1:
            time.sleep(max(0, ms) / 1000.0)
            callback = str(rest[1])
            if callback in self.commands:
                self.commands[callback](*rest[2:])
        return "after#0"

    def _wm(self, rest):
        if len(rest) >= 3 and rest[0] == "title":
            self.widgets["."].options["title"] = rest[2]
            self.render()
        return ""

    def _winfo(self, rest):
        if len(rest) >= 2 and rest[0] in {"exists", "ismapped"}:
            return "1" if str(rest[1]) in self.widgets else "0"
        if rest and rest[0] in {"screenwidth", "width"}:
            return "720"
        if rest and rest[0] in {"screenheight", "height"}:
            return "480"
        return "1"

    def _tk(self, rest):
        if rest and rest[0] == "scaling":
            return "1.0"
        return ""

    def render(self, force=False):
        now = time.monotonic()
        if not force and now - self._last_render < 0.05:
            return
        self._last_render = now
        width, height = 720, 480
        frame = bytearray([26, 28, 34, 255] * width * height)
        y = 28
        title = str(self.widgets["."].options.get("title") or self.base_name or "Tk")
        self._rect(frame, width, 0, 0, width, 52, (34, 38, 46, 255))
        self._text_bar(frame, width, 24, 20, title, (238, 242, 248, 255))
        for path in self._visible_paths():
            widget = self.widgets[path]
            if widget.kind == "frame":
                self._rect(frame, width, 28, y, width - 56, y + 16, (43, 47, 55, 255))
                y += 20
            elif widget.kind in {"label", "button", "entry", "text", "listbox", "checkbutton", "radiobutton"}:
                text = str(widget.options.get("text") or widget.options.get("value") or widget.kind)
                bg = (58, 63, 74, 255) if widget.kind != "button" else (76, 82, 95, 255)
                self._rect(frame, width, 28, y, width - 56, y + 44, bg)
                self._text_bar(frame, width, 46, y + 16, text, (238, 242, 248, 255))
                y += 54
            elif widget.kind == "canvas":
                canvas_height = int(float(widget.options.get("height", 160) or 160))
                self._rect(frame, width, 28, y, width - 56, y + canvas_height, (15, 17, 22, 255))
                self._render_canvas(frame, width, 28, y, widget)
                y += canvas_height + 12
            else:
                y += 16
        try:
            import aquadisplay
            aquadisplay.show(bytes(frame), width=width, height=height, title=f"tkinter - {title}")
        except Exception:
            pass

    def _visible_paths(self):
        return [p for p in sorted(self.widgets) if p != "." and (
            self.widgets[p].packed or self.widgets[p].gridded or self.widgets[p].placed)]

    def _render_canvas(self, frame, width, ox, oy, widget):
        for kind, coords, opts in widget.canvas_items:
            fill = self._color(opts.get("fill") or opts.get("outline") or "#d8dee9")
            nums = [int(float(x)) for x in coords if str(x).replace(".", "", 1).lstrip("-").isdigit()]
            if kind in {"rectangle", "oval"} and len(nums) >= 4:
                self._rect(frame, width, ox + nums[0], oy + nums[1], ox + nums[2], oy + nums[3], fill)
            elif kind == "line" and len(nums) >= 4:
                self._line(frame, width, ox + nums[0], oy + nums[1], ox + nums[2], oy + nums[3], fill)
            elif kind == "text" and len(nums) >= 2:
                self._text_bar(frame, width, ox + nums[0], oy + nums[1], str(opts.get("text", "")), fill)

    def _color(self, value):
        names = {
            "black": (0, 0, 0, 255), "white": (255, 255, 255, 255),
            "red": (220, 72, 72, 255), "green": (64, 180, 98, 255),
            "blue": (82, 145, 255, 255), "yellow": (245, 210, 86, 255),
        }
        value = str(value)
        if value.lower() in names:
            return names[value.lower()]
        if value.startswith("#") and len(value) in {4, 7}:
            if len(value) == 4:
                r, g, b = [int(c * 2, 16) for c in value[1:]]
            else:
                r, g, b = int(value[1:3], 16), int(value[3:5], 16), int(value[5:7], 16)
            return (r, g, b, 255)
        return (216, 222, 233, 255)

    def _rect(self, frame, width, x1, y1, x2, y2, color):
        height = len(frame) // (width * 4)
        x1, x2 = sorted((max(0, min(width, int(x1))), max(0, min(width, int(x2)))))
        y1, y2 = sorted((max(0, min(height, int(y1))), max(0, min(height, int(y2)))))
        row = bytes(color) * max(0, x2 - x1)
        for y in range(y1, y2):
            start = (y * width + x1) * 4
            frame[start:start + len(row)] = row

    def _line(self, frame, width, x1, y1, x2, y2, color):
        steps = max(abs(x2 - x1), abs(y2 - y1), 1)
        height = len(frame) // (width * 4)
        for i in range(steps + 1):
            x = int(x1 + (x2 - x1) * i / steps)
            y = int(y1 + (y2 - y1) * i / steps)
            if 0 <= x < width and 0 <= y < height:
                idx = (y * width + x) * 4
                frame[idx:idx + 4] = bytes(color)

    def _text_bar(self, frame, width, x, y, text, color):
        # Lightweight placeholder text renderer: visible bars per character.
        cursor = int(x)
        for ch in str(text)[:48]:
            if ch != " ":
                self._rect(frame, width, cursor, y, cursor + 5, y + 9, color)
            cursor += 8
