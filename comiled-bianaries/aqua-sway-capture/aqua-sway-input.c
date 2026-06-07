#define _GNU_SOURCE
#include <ctype.h>
#include <errno.h>
#include <stdbool.h>
#include <stdint.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <sys/socket.h>
#include <sys/un.h>
#include <time.h>
#include <unistd.h>
#include <wayland-client.h>
#include "wlr-virtual-pointer-unstable-v1-client-protocol.h"

#ifndef BTN_LEFT
#define BTN_LEFT 0x110
#endif
#ifndef WL_POINTER_BUTTON_STATE_RELEASED
#define WL_POINTER_BUTTON_STATE_RELEASED 0
#define WL_POINTER_BUTTON_STATE_PRESSED 1
#endif

struct input_state {
    struct wl_display *display;
    struct wl_registry *registry;
    struct wl_output *output;
    struct wl_seat *seat;
    struct zwlr_virtual_pointer_manager_v1 *manager;
    struct zwlr_virtual_pointer_v1 *pointer;
    bool pressed;
    int output_width;
    int output_height;
    int last_x;
    int last_y;
};

static uint32_t now_ms(void) {
    struct timespec ts;
    clock_gettime(CLOCK_MONOTONIC, &ts);
    return (uint32_t)(ts.tv_sec * 1000u + ts.tv_nsec / 1000000u);
}

static int connect_aqua_socket(void) {
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
    return fd;
}

static char *poll_events(void) {
    int fd = connect_aqua_socket();
    if (fd < 0) return NULL;
    const char *request = "POLLEVENTS\n";
    if (write(fd, request, strlen(request)) < 0) {
        close(fd);
        return NULL;
    }
    shutdown(fd, SHUT_WR);
    size_t cap = 4096, len = 0;
    char *buf = malloc(cap);
    if (!buf) {
        close(fd);
        return NULL;
    }
    while (true) {
        if (len + 1024 >= cap) {
            cap *= 2;
            char *next = realloc(buf, cap);
            if (!next) {
                free(buf);
                close(fd);
                return NULL;
            }
            buf = next;
        }
        ssize_t n = read(fd, buf + len, cap - len - 1);
        if (n <= 0) break;
        len += (size_t)n;
    }
    close(fd);
    buf[len] = '\0';
    return buf;
}

static bool json_string_value(const char *object, const char *key, char *out, size_t out_size) {
    char needle[64];
    snprintf(needle, sizeof(needle), "\"%s\"", key);
    const char *p = strstr(object, needle);
    if (!p) return false;
    p = strchr(p, ':');
    if (!p) return false;
    p++;
    while (*p && isspace((unsigned char)*p)) p++;
    if (*p != '"') return false;
    p++;
    size_t i = 0;
    while (*p && *p != '"' && i + 1 < out_size) out[i++] = *p++;
    out[i] = '\0';
    return i > 0;
}

static bool json_int_value(const char *object, const char *key, int *out) {
    char needle[64];
    snprintf(needle, sizeof(needle), "\"%s\"", key);
    const char *p = strstr(object, needle);
    if (!p) return false;
    p = strchr(p, ':');
    if (!p) return false;
    p++;
    while (*p && isspace((unsigned char)*p)) p++;
    *out = atoi(p);
    return true;
}

static int clamp_int(int value, int min, int max) {
    if (value < min) return min;
    if (value > max) return max;
    return value;
}

static void send_pointer_motion(struct input_state *state, uint32_t t, int x, int y) {
    int width = state->output_width > 0 ? state->output_width : 1080;
    int height = state->output_height > 0 ? state->output_height : 2400;
    x = clamp_int(x, 0, width - 1);
    y = clamp_int(y, 0, height - 1);
    state->last_x = x;
    state->last_y = y;
    zwlr_virtual_pointer_v1_motion_absolute(state->pointer, t, (uint32_t)x, (uint32_t)y,
            (uint32_t)width, (uint32_t)height);
}

static void send_pointer_button(struct input_state *state, uint32_t t, bool pressed) {
    if (state->pressed == pressed) return;
    state->pressed = pressed;
    zwlr_virtual_pointer_v1_button(state->pointer, t, BTN_LEFT,
            pressed ? WL_POINTER_BUTTON_STATE_PRESSED : WL_POINTER_BUTTON_STATE_RELEASED);
}

static void process_touch_events(struct input_state *state, const char *payload) {
    const char *p = payload;
    while ((p = strstr(p, "\"type\":\"touch\"")) != NULL) {
        const char *start = p;
        const char *end = strchr(p, '}');
        if (!end) break;
        size_t len = (size_t)(end - start + 1);
        char object[512];
        if (len >= sizeof(object)) len = sizeof(object) - 1;
        memcpy(object, start, len);
        object[len] = '\0';
        char action[32];
        int x = 0, y = 0;
        if (json_string_value(object, "action", action, sizeof(action))
                && json_int_value(object, "x", &x)
                && json_int_value(object, "y", &y)) {
            uint32_t t = now_ms();
            if (strcmp(action, "down") == 0) {
                send_pointer_motion(state, t, x, y);
                send_pointer_button(state, t, true);
            } else if (strcmp(action, "move") == 0) {
                send_pointer_motion(state, t, x, y);
            } else if (strcmp(action, "up") == 0 || strcmp(action, "cancel") == 0) {
                send_pointer_motion(state, t, x, y);
                send_pointer_button(state, t, false);
            }
            zwlr_virtual_pointer_v1_frame(state->pointer);
            wl_display_flush(state->display);
        }
        p = end + 1;
    }
}

static void registry_global(void *data, struct wl_registry *registry, uint32_t name, const char *interface, uint32_t version) {
    struct input_state *state = data;
    if (strcmp(interface, wl_output_interface.name) == 0 && !state->output) {
        state->output = wl_registry_bind(registry, name, &wl_output_interface, version < 4 ? version : 4);
    } else if (strcmp(interface, wl_seat_interface.name) == 0 && !state->seat) {
        state->seat = wl_registry_bind(registry, name, &wl_seat_interface, version < 5 ? version : 5);
    } else if (strcmp(interface, zwlr_virtual_pointer_manager_v1_interface.name) == 0) {
        state->manager = wl_registry_bind(registry, name, &zwlr_virtual_pointer_manager_v1_interface, version < 2 ? version : 2);
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
    struct input_state state;
    memset(&state, 0, sizeof(state));
    state.output_width = getenv("AQUA_SWAY_WIDTH") ? atoi(getenv("AQUA_SWAY_WIDTH")) : 1080;
    state.output_height = getenv("AQUA_SWAY_HEIGHT") ? atoi(getenv("AQUA_SWAY_HEIGHT")) : 2400;
    if (state.output_width <= 0) state.output_width = 1080;
    if (state.output_height <= 0) state.output_height = 2400;
    state.display = wl_display_connect(NULL);
    if (!state.display) {
        fprintf(stderr, "aqua-sway-input: cannot connect to Wayland display\n");
        return 2;
    }
    state.registry = wl_display_get_registry(state.display);
    wl_registry_add_listener(state.registry, &registry_listener, &state);
    wl_display_roundtrip(state.display);
    wl_display_roundtrip(state.display);
    if (!state.manager) {
        fprintf(stderr, "aqua-sway-input: compositor has no virtual pointer manager\n");
        return 3;
    }
    if (state.output && zwlr_virtual_pointer_manager_v1_get_version(state.manager) >= 2) {
        state.pointer = zwlr_virtual_pointer_manager_v1_create_virtual_pointer_with_output(state.manager, state.seat, state.output);
    } else {
        state.pointer = zwlr_virtual_pointer_manager_v1_create_virtual_pointer(state.manager, state.seat);
    }
    if (!state.pointer) return 4;
    wl_display_flush(state.display);
    while (true) {
        while (wl_display_prepare_read(state.display) != 0) {
            wl_display_dispatch_pending(state.display);
        }
        wl_display_flush(state.display);
        char *events = poll_events();
        wl_display_cancel_read(state.display);
        if (events) {
            process_touch_events(&state, events);
            free(events);
        }
        usleep(6000);
    }
    return 0;
}
