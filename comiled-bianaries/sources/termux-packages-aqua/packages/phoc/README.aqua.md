# Aqua Phoc trial lane

Phoc is the mobile Wayland compositor used by Phosh. Aqua keeps it opt-in while
we prove the Android/headless path because it is larger and more mobile-GNOME
oriented than the current sway test lane.

Build attempt:

```sh
AQUA_MOBILE_WAYLAND_PACKAGES=phoc \
AQUA_CV_ARCHES=x86_64 \
bash comiled-bianaries/build-aqua-cv-packages.sh
```

Current dependency status in the Aqua package tree:

- Present: `glib`, `libdrm`, `libglvnd`, `libpixman`, `libwayland`,
  `libwayland-protocols`, `libxkbcommon`, `xkeyboard-config`.
- Missing/staged next: `gsettings-desktop-schemas`, `gnome-desktop`,
  `libinput`, `libudev`.

The app launcher `aqua-phoc` is already wired. Until the `phoc` binary exists in
the runtime, it opens the Aqua Wayland display bridge and falls back to
`aqua-sway`.
