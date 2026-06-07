# Aqua Tkinter Wayland Lane

This lane is intentionally separate from `tkinter-egl`.

Reality check: upstream Tk/Tkinter does not currently ship a native Wayland
platform backend in the normal Tk tree. The real Wayland route is:

1. Aqua starts the native Android Vulkan host.
2. Aqua runs a real Wayland compositor stack, targeted here as sway/wlroots.
3. Tkinter runs the supported Unix Tk/X11 backend against Xwayland inside that
   compositor.

That means this lane is "Tkinter on real Wayland compositor via Xwayland", not
the old Aqua JSON/EGL shim and not a fake framebuffer.

Build:

```bash
AQUA_CV_ARCHES="x86_64 aarch64" bash comiled-bianaries/tkinter-wayland/build-tkinter-wayland.sh
```
