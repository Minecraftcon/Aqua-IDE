# Aqua Tkinter Android Port

Goal: make Python `tkinter` work in Aqua IDE without X11 by rendering Tk output
through the app's native Android display bridge.

Current state:

- App runtime includes a temporary `_tkinter.py` fallback.
- Standard `import tkinter` works instead of crashing on missing `_tkinter`.
- Basic `Tk`, `Label`, `Button`, and `Canvas` calls render to Android through
  `aquadisplay`.
- The fallback is intentionally not the final full Tcl/Tk C engine.

Native source-port path:

1. `fetch-tkinter-sources.sh` shallow/sparse-clones Tcl, Tk, and CPython
   `_tkinter` sources.
2. `patch-aqua-tk-android.py` injects the Aqua Android RGBA bridge into Tk.
3. `build-aqua-tkinter.sh` builds Tcl first and refuses to build X11 Tk unless
   `AQUA_BUILD_NATIVE_TK=1` is explicitly set.
4. Finish the real Tk platform backend by wiring Tk's drawing/event/display
   layer to `tkAquaAndroidBridge.c`, then build CPython `Modules/_tkinter.c`
   against those Android Tcl/Tk libraries.

Important: do not ship X11 Tk as "Android tkinter". Aqua's target display path
is Android app framebuffer output through `ANDROPY_DISPLAY_SOCKET`.
