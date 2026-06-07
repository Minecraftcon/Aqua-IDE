TERMUX_PKG_HOMEPAGE=https://gitlab.gnome.org/World/Phosh/phoc
TERMUX_PKG_DESCRIPTION="Mobile-oriented Wayland compositor used by Phosh"
TERMUX_PKG_LICENSE="GPL-3.0-or-later"
TERMUX_PKG_MAINTAINER="Aqua IDE"
TERMUX_PKG_VERSION=0.55.1
TERMUX_PKG_SRCURL=https://gitlab.gnome.org/World/Phosh/phoc/-/archive/v${TERMUX_PKG_VERSION}/phoc-v${TERMUX_PKG_VERSION}.tar.gz
TERMUX_PKG_SHA256=97cffe56477614f65ba462f0c6075f758795382cb21f2faa5674d0533d007a7a
TERMUX_PKG_DEPENDS="glib, gsettings-desktop-schemas, gnome-desktop, libc++, libdrm, libglvnd, libinput, libpixman, libudev, libwayland, libwayland-protocols, libxkbcommon, xkeyboard-config"
TERMUX_PKG_BUILD_DEPENDS="libwayland-cross-scanner, libwayland-protocols, scdoc"
TERMUX_PKG_EXTRA_CONFIGURE_ARGS="
-Dembed-wlroots=enabled
-Dxwayland=disabled
-Dtests=false
-Dgtk_doc=false
-Dman=false
-Ddtrace=disabled
-Dsysprof=disabled
-Dwerror=false
"

termux_step_pre_configure() {
	CFLAGS+=" -D__USE_BSD=1"
	CXXFLAGS+=" -D__USE_BSD=1"
}
