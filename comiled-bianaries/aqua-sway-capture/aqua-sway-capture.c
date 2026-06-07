#define _GNU_SOURCE
#include <errno.h>
#include <fcntl.h>
#include <poll.h>
#include <stdbool.h>
#include <stddef.h>
#include <stdint.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <sys/mman.h>
#include <sys/socket.h>
#include <sys/stat.h>
#include <sys/types.h>
#include <sys/un.h>
#include <time.h>
#include <unistd.h>
#include <wayland-client.h>
#include "wlr-screencopy-unstable-v1-client-protocol.h"

struct capture_state {
    struct wl_display *display;
    struct wl_registry *registry;
    struct wl_shm *shm;
    struct wl_output *output;
    struct zwlr_screencopy_manager_v1 *manager;
    uint32_t shm_format;
    uint32_t width;
    uint32_t height;
    uint32_t stride;
    uint32_t flags;
    int done;
    int failed;
    void *pixels;
    size_t pixels_size;
};

static int make_tmp_file(size_t size) {
    const char *runtime = getenv("XDG_RUNTIME_DIR");
    if (!runtime || !runtime[0]) runtime = "/data/data/com.andropy.ide/files/usr/tmp";
    char path[512];
    snprintf(path, sizeof(path), "%s/aqua-screencopy-XXXXXX", runtime);
    int fd = mkstemp(path);
    if (fd < 0) return -1;
    unlink(path);
    if (ftruncate(fd, (off_t)size) != 0) {
        close(fd);
        return -1;
    }
    return fd;
}

static int send_aqua_frame(const uint8_t *src, uint32_t width, uint32_t height, uint32_t stride, uint32_t format) {
    const char *socket_name = getenv("ANDROPY_DISPLAY_SOCKET");
    if (!socket_name || !socket_name[0]) socket_name = "andropy_display_com.andropy.ide";

    int fd = socket(AF_UNIX, SOCK_STREAM | SOCK_CLOEXEC, 0);
    if (fd < 0) return -1;
    struct sockaddr_un addr;
    memset(&addr, 0, sizeof(addr));
    addr.sun_family = AF_UNIX;
    size_t name_len = strlen(socket_name);
    if (name_len + 1 >= sizeof(addr.sun_path)) {
        close(fd);
        errno = ENAMETOOLONG;
        return -1;
    }
    addr.sun_path[0] = '\0';
    memcpy(addr.sun_path + 1, socket_name, name_len);
    socklen_t addr_len = (socklen_t)(offsetof(struct sockaddr_un, sun_path) + 1 + name_len);
    if (connect(fd, (struct sockaddr *)&addr, addr_len) != 0) {
        close(fd);
        return -1;
    }

    size_t rgba_size = (size_t)width * height * 4;
    uint8_t *rgba = malloc(rgba_size);
    if (!rgba) {
        close(fd);
        return -1;
    }
    for (uint32_t y = 0; y < height; y++) {
        const uint8_t *row = src + (size_t)y * stride;
        uint8_t *out = rgba + (size_t)y * width * 4;
        for (uint32_t x = 0; x < width; x++) {
            uint8_t b = row[x * 4 + 0];
            uint8_t g = row[x * 4 + 1];
            uint8_t r = row[x * 4 + 2];
            uint8_t a = (format == WL_SHM_FORMAT_ARGB8888) ? row[x * 4 + 3] : 255;
            out[x * 4 + 0] = r;
            out[x * 4 + 1] = g;
            out[x * 4 + 2] = b;
            out[x * 4 + 3] = a;
        }
    }

    char header[160];
    int header_len = snprintf(header, sizeof(header), "FRAME %u %u %zu Aqua Wayland\n", width, height, rgba_size);
    int ok = 0;
    if (header_len > 0 && write(fd, header, (size_t)header_len) == header_len) {
        size_t sent = 0;
        while (sent < rgba_size) {
            ssize_t n = write(fd, rgba + sent, rgba_size - sent);
            if (n <= 0) {
                ok = -1;
                break;
            }
            sent += (size_t)n;
        }
    } else {
        ok = -1;
    }
    free(rgba);
    shutdown(fd, SHUT_WR);
    char discard[64];
    while (read(fd, discard, sizeof(discard)) > 0) {}
    close(fd);
    return ok;
}

static void frame_buffer(void *data, struct zwlr_screencopy_frame_v1 *frame,
                         uint32_t format, uint32_t width, uint32_t height, uint32_t stride) {
    struct capture_state *state = data;
    state->shm_format = format;
    state->width = width;
    state->height = height;
    state->stride = stride;
    if (format != WL_SHM_FORMAT_XRGB8888 && format != WL_SHM_FORMAT_ARGB8888) {
        state->failed = 1;
        return;
    }
    state->pixels_size = (size_t)stride * height;
    int fd = make_tmp_file(state->pixels_size);
    if (fd < 0) {
        state->failed = 1;
        return;
    }
    state->pixels = mmap(NULL, state->pixels_size, PROT_READ | PROT_WRITE, MAP_SHARED, fd, 0);
    if (state->pixels == MAP_FAILED) {
        state->pixels = NULL;
        close(fd);
        state->failed = 1;
        return;
    }
    struct wl_shm_pool *pool = wl_shm_create_pool(state->shm, fd, (int32_t)state->pixels_size);
    struct wl_buffer *buffer = wl_shm_pool_create_buffer(pool, 0, (int32_t)width, (int32_t)height, (int32_t)stride, format);
    wl_shm_pool_destroy(pool);
    close(fd);
    zwlr_screencopy_frame_v1_copy(frame, buffer);
    wl_buffer_destroy(buffer);
}

static void frame_flags(void *data, struct zwlr_screencopy_frame_v1 *frame, uint32_t flags) {
    (void)frame;
    ((struct capture_state *)data)->flags = flags;
}

static void frame_ready(void *data, struct zwlr_screencopy_frame_v1 *frame,
                        uint32_t tv_sec_hi, uint32_t tv_sec_lo, uint32_t tv_nsec) {
    (void)frame;
    (void)tv_sec_hi;
    (void)tv_sec_lo;
    (void)tv_nsec;
    struct capture_state *state = data;
    if (state->pixels && state->width && state->height) {
        send_aqua_frame((const uint8_t *)state->pixels, state->width, state->height, state->stride, state->shm_format);
    }
    state->done = 1;
}

static void frame_failed(void *data, struct zwlr_screencopy_frame_v1 *frame) {
    (void)frame;
    struct capture_state *state = data;
    state->failed = 1;
    state->done = 1;
}

static void frame_damage(void *data, struct zwlr_screencopy_frame_v1 *frame, uint32_t x, uint32_t y, uint32_t width, uint32_t height) {
    (void)data; (void)frame; (void)x; (void)y; (void)width; (void)height;
}

static void frame_linux_dmabuf(void *data, struct zwlr_screencopy_frame_v1 *frame, uint32_t format, uint32_t width, uint32_t height) {
    (void)data; (void)frame; (void)format; (void)width; (void)height;
}

static void frame_buffer_done(void *data, struct zwlr_screencopy_frame_v1 *frame) {
    (void)data; (void)frame;
}

static const struct zwlr_screencopy_frame_v1_listener frame_listener = {
    frame_buffer,
    frame_flags,
    frame_ready,
    frame_failed,
    frame_damage,
    frame_linux_dmabuf,
    frame_buffer_done,
};

static void registry_global(void *data, struct wl_registry *registry, uint32_t name, const char *interface, uint32_t version) {
    struct capture_state *state = data;
    if (strcmp(interface, wl_shm_interface.name) == 0) {
        state->shm = wl_registry_bind(registry, name, &wl_shm_interface, version < 1 ? version : 1);
    } else if (strcmp(interface, wl_output_interface.name) == 0 && !state->output) {
        state->output = wl_registry_bind(registry, name, &wl_output_interface, version < 4 ? version : 4);
    } else if (strcmp(interface, zwlr_screencopy_manager_v1_interface.name) == 0) {
        uint32_t bind_version = version < 3 ? version : 3;
        state->manager = wl_registry_bind(registry, name, &zwlr_screencopy_manager_v1_interface, bind_version);
    }
}

static void registry_remove(void *data, struct wl_registry *registry, uint32_t name) {
    (void)data; (void)registry; (void)name;
}

static const struct wl_registry_listener registry_listener = {
    registry_global,
    registry_remove,
};

static int capture_once(struct capture_state *state) {
    state->done = 0;
    state->failed = 0;
    state->pixels = NULL;
    state->pixels_size = 0;
    struct zwlr_screencopy_frame_v1 *frame = zwlr_screencopy_manager_v1_capture_output(state->manager, 0, state->output);
    zwlr_screencopy_frame_v1_add_listener(frame, &frame_listener, state);
    wl_display_flush(state->display);
    while (!state->done && wl_display_dispatch(state->display) >= 0) {}
    zwlr_screencopy_frame_v1_destroy(frame);
    if (state->pixels) {
        munmap(state->pixels, state->pixels_size);
    }
    wl_display_roundtrip(state->display);
    return state->failed ? -1 : 0;
}

int main(int argc, char **argv) {
    int fps = 60;
    if (argc > 1) {
        fps = atoi(argv[1]);
        if (fps < 1) fps = 1;
        if (fps > 60) fps = 60;
    }
    struct capture_state state;
    memset(&state, 0, sizeof(state));
    state.display = wl_display_connect(NULL);
    if (!state.display) {
        fprintf(stderr, "aqua-sway-capture: cannot connect to Wayland display\n");
        return 2;
    }
    state.registry = wl_display_get_registry(state.display);
    wl_registry_add_listener(state.registry, &registry_listener, &state);
    wl_display_roundtrip(state.display);
    wl_display_roundtrip(state.display);
    if (!state.shm || !state.output || !state.manager) {
        fprintf(stderr, "aqua-sway-capture: missing shm/output/screencopy (shm=%p output=%p manager=%p)\n",
                (void *)state.shm, (void *)state.output, (void *)state.manager);
        return 3;
    }
    struct timespec delay = {0, 1000000000L / fps};
    while (true) {
        capture_once(&state);
        nanosleep(&delay, NULL);
    }
    return 0;
}
