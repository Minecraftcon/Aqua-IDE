#define _GNU_SOURCE
#include <errno.h>
#include <fcntl.h>
#include <stdbool.h>
#include <stdint.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <sys/mman.h>
#include <unistd.h>
#include <wayland-client.h>
#include "wlr-layer-shell-unstable-v1-client-protocol.h"

#define WALL_W 1080
#define WALL_H 2400

struct wall_state {
    struct wl_display *display;
    struct wl_registry *registry;
    struct wl_compositor *compositor;
    struct wl_shm *shm;
    struct wl_output *output;
    struct zwlr_layer_shell_v1 *layer_shell;
    struct wl_surface *surface;
    struct zwlr_layer_surface_v1 *layer_surface;
    bool configured;
};

static int make_tmp_file(size_t size) {
    const char *runtime = getenv("XDG_RUNTIME_DIR");
    if (!runtime || !runtime[0]) runtime = "/data/data/com.andropy.ide/files/usr/tmp";
    char path[512];
    snprintf(path, sizeof(path), "%s/aqua-wallpaper-XXXXXX", runtime);
    int fd = mkstemp(path);
    if (fd < 0) return -1;
    unlink(path);
    if (ftruncate(fd, (off_t)size) != 0) {
        close(fd);
        return -1;
    }
    return fd;
}

static void fill_gradient(uint8_t *dst) {
    for (int y = 0; y < WALL_H; y++) {
        for (int x = 0; x < WALL_W; x++) {
            uint8_t *p = dst + ((size_t)y * WALL_W + x) * 4;
            p[0] = (uint8_t)(180 + y * 40 / WALL_H); /* B */
            p[1] = (uint8_t)(150 + x * 70 / WALL_W); /* G */
            p[2] = (uint8_t)(80 + x * 40 / WALL_W);  /* R */
            p[3] = 255;
        }
    }
}

static void load_wallpaper(uint8_t *dst) {
    const char *path = getenv("AQUA_SWAY_WALLPAPER_RGBA");
    if (!path || !path[0]) path = "/data/data/com.andropy.ide/files/usr/share/aqua/wallpapers/default-1080x2400.rgba";
    FILE *file = fopen(path, "rb");
    if (!file) {
        fill_gradient(dst);
        return;
    }
    size_t expected = (size_t)WALL_W * WALL_H * 4;
    size_t got = fread(dst, 1, expected, file);
    fclose(file);
    if (got != expected) fill_gradient(dst);
    for (size_t i = 0; i < expected; i += 4) {
        uint8_t r = dst[i + 0], g = dst[i + 1], b = dst[i + 2], a = dst[i + 3];
        dst[i + 0] = b;
        dst[i + 1] = g;
        dst[i + 2] = r;
        dst[i + 3] = a;
    }
}

static struct wl_buffer *create_wall_buffer(struct wall_state *state) {
    size_t size = (size_t)WALL_W * WALL_H * 4;
    int fd = make_tmp_file(size);
    if (fd < 0) return NULL;
    uint8_t *pixels = mmap(NULL, size, PROT_READ | PROT_WRITE, MAP_SHARED, fd, 0);
    if (pixels == MAP_FAILED) {
        close(fd);
        return NULL;
    }
    load_wallpaper(pixels);
    struct wl_shm_pool *pool = wl_shm_create_pool(state->shm, fd, (int32_t)size);
    struct wl_buffer *buffer = wl_shm_pool_create_buffer(pool, 0, WALL_W, WALL_H, WALL_W * 4, WL_SHM_FORMAT_ARGB8888);
    wl_shm_pool_destroy(pool);
    munmap(pixels, size);
    close(fd);
    return buffer;
}

static void layer_configure(void *data, struct zwlr_layer_surface_v1 *surface, uint32_t serial, uint32_t width, uint32_t height) {
    (void)width; (void)height;
    struct wall_state *state = data;
    zwlr_layer_surface_v1_ack_configure(surface, serial);
    state->configured = true;
}

static void layer_closed(void *data, struct zwlr_layer_surface_v1 *surface) {
    (void)surface;
    ((struct wall_state *)data)->configured = false;
}

static const struct zwlr_layer_surface_v1_listener layer_listener = {
    layer_configure,
    layer_closed,
};

static void registry_global(void *data, struct wl_registry *registry, uint32_t name, const char *interface, uint32_t version) {
    struct wall_state *state = data;
    if (strcmp(interface, wl_compositor_interface.name) == 0) {
        state->compositor = wl_registry_bind(registry, name, &wl_compositor_interface, version < 4 ? version : 4);
    } else if (strcmp(interface, wl_shm_interface.name) == 0) {
        state->shm = wl_registry_bind(registry, name, &wl_shm_interface, 1);
    } else if (strcmp(interface, wl_output_interface.name) == 0 && !state->output) {
        state->output = wl_registry_bind(registry, name, &wl_output_interface, version < 4 ? version : 4);
    } else if (strcmp(interface, zwlr_layer_shell_v1_interface.name) == 0) {
        state->layer_shell = wl_registry_bind(registry, name, &zwlr_layer_shell_v1_interface, version < 4 ? version : 4);
    }
}

static void registry_remove(void *data, struct wl_registry *registry, uint32_t name) {
    (void)data; (void)registry; (void)name;
}

static const struct wl_registry_listener registry_listener = {
    registry_global,
    registry_remove,
};

int main(void) {
    struct wall_state state;
    memset(&state, 0, sizeof(state));
    state.display = wl_display_connect(NULL);
    if (!state.display) {
        fprintf(stderr, "aqua-sway-wallpaper: cannot connect to Wayland display\n");
        return 2;
    }
    state.registry = wl_display_get_registry(state.display);
    wl_registry_add_listener(state.registry, &registry_listener, &state);
    wl_display_roundtrip(state.display);
    wl_display_roundtrip(state.display);
    if (!state.compositor || !state.shm || !state.output || !state.layer_shell) {
        fprintf(stderr, "aqua-sway-wallpaper: missing compositor/shm/output/layer-shell\n");
        return 3;
    }
    state.surface = wl_compositor_create_surface(state.compositor);
    state.layer_surface = zwlr_layer_shell_v1_get_layer_surface(
            state.layer_shell, state.surface, state.output,
            ZWLR_LAYER_SHELL_V1_LAYER_BACKGROUND, "aqua-wallpaper");
    zwlr_layer_surface_v1_set_size(state.layer_surface, WALL_W, WALL_H);
    zwlr_layer_surface_v1_set_anchor(state.layer_surface,
            ZWLR_LAYER_SURFACE_V1_ANCHOR_TOP | ZWLR_LAYER_SURFACE_V1_ANCHOR_RIGHT |
            ZWLR_LAYER_SURFACE_V1_ANCHOR_BOTTOM | ZWLR_LAYER_SURFACE_V1_ANCHOR_LEFT);
    zwlr_layer_surface_v1_set_exclusive_zone(state.layer_surface, -1);
    zwlr_layer_surface_v1_add_listener(state.layer_surface, &layer_listener, &state);
    wl_surface_commit(state.surface);
    wl_display_roundtrip(state.display);
    struct wl_buffer *buffer = create_wall_buffer(&state);
    if (!buffer) return 4;
    wl_surface_attach(state.surface, buffer, 0, 0);
    wl_surface_damage_buffer(state.surface, 0, 0, WALL_W, WALL_H);
    wl_surface_commit(state.surface);
    while (wl_display_dispatch(state.display) >= 0) {}
    return 0;
}
