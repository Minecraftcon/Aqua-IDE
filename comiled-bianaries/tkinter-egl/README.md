# Aqua Tkinter EGL Android Port

Goal: make Python `tkinter` work in Aqua IDE without X11 by rendering Tk output
through the app's native Android display bridge.

Current state:

- The temporary `_tkinter.py` fallback has been removed from the app runtime.
- Standard `import tkinter` now uses the native `_tkinter` extension when the
  selected runtime ABI provides one.
- x86_64 currently has a native `_tkinter` build that can create roots and
  render basic Canvas shapes.
- Runtime ABIs without a native `_tkinter` extension will fail honestly until
  their real Android Tk backend is built.

Native source-port path:

1. `fetch-tkinter-sources.sh` shallow/sparse-clones Tcl, Tk, and CPython
   `_tkinter` sources.
2. `patch-aqua-tk-android.py` injects the Aqua Android RGBA bridge into Tk.
3. `build-aqua-tkinter.sh` builds Tcl first and refuses to build X11 Tk unless
   `AQUA_BUILD_NATIVE_TK=1` is explicitly set.
4. Finish the real Tk platform backend by wiring Tk's drawing/event/display
   layer to `tkAquaAndroidBridge.c`, then build CPython `Modules/_tkinter.c`
   against those Android Tcl/Tk libraries.

Important: this folder is the experimental EGL/RGBA Android Tk port. The
separate `comiled-bianaries/tkinter-wayland` lane is for real Wayland compositor
testing through sway/Xwayland.
