#include <errno.h>
#include <libgen.h>
#include <limits.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <unistd.h>

static int is_coreutils_command(const char *name) {
    static const char *commands[] = {
        "[", "b2sum", "base32", "base64", "basename", "basenc", "cat", "chcon",
        "chgrp", "chmod", "chown", "chroot", "cksum", "comm", "coreutils", "cp",
        "csplit", "cut", "date", "dd", "dir", "dircolors", "dirname", "du",
        "echo", "env", "expand", "expr", "factor", "false", "fmt", "fold",
        "groups", "head", "id", "install", "join", "kill", "link", "ln",
        "logname", "ls", "md5sum", "mkdir", "mkfifo", "mknod", "mktemp", "mv",
        "nice", "nl", "nohup", "nproc", "numfmt", "od", "paste", "pathchk",
        "pr", "printenv", "printf", "ptx", "pwd", "readlink", "realpath", "rm",
        "rmdir", "runcon", "seq", "sha1sum", "sha224sum", "sha256sum",
        "sha384sum", "sha512sum", "shred", "shuf", "sleep", "sort", "split",
        "stat", "stdbuf", "stty", "sum", "sync", "tac", "tail", "tee", "test",
        "timeout", "touch", "tr", "true", "truncate", "tsort", "tty", "uname",
        "unexpand", "uniq", "unlink", "vdir", "wc", "whoami", "yes", NULL
    };
    for (int i = 0; commands[i] != NULL; i++) {
        if (strcmp(name, commands[i]) == 0) return 1;
    }
    return 0;
}

static int native_dir(char *out, size_t out_size) {
    char exe[PATH_MAX];
    ssize_t len = readlink("/proc/self/exe", exe, sizeof(exe) - 1);
    if (len <= 0) return -1;
    exe[len] = '\0';
    char *slash = strrchr(exe, '/');
    if (slash == NULL) return -1;
    *slash = '\0';
    if (strlen(exe) + 1 > out_size) return -1;
    strcpy(out, exe);
    return 0;
}

static int is_pending_command(const char *name) {
    static const char *commands[] = {
        "apt", "pkg", "clang", "clang++", "llvm-config", "gcc", "g++", "make", "dpkg", NULL
    };
    for (int i = 0; commands[i] != NULL; i++) {
        if (strcmp(name, commands[i]) == 0) return 1;
    }
    return 0;
}

static int print_pending_command(const char *name) {
    const char *prefix = getenv("PREFIX");
    if (prefix == NULL || prefix[0] == '\0') prefix = "/data/data/com.andropy.ide/files/usr";

    if (strcmp(name, "apt") == 0 || strcmp(name, "pkg") == 0 || strcmp(name, "dpkg") == 0) {
        fprintf(stderr, "AndroPy package manager is not enabled yet.\n");
        fprintf(stderr, "Termux debs must be rebuilt for %s before apt/pkg/dpkg can install safely.\n", prefix);
        fprintf(stderr, "Build host entrypoint: comiled-bianaries/build-andropy-termux-bootstrap.sh\n");
        return strcmp(name, "dpkg") == 0 ? 127 : 64;
    }

    fprintf(stderr, "%s is queued for the rebuilt AndroPy Termux bootstrap.\n", name);
    fprintf(stderr, "Use comiled-bianaries/build-andropy-termux-bootstrap.sh to build it for %s.\n", prefix);
    return 127;
}

int main(int argc, char **argv) {
    const char *name = basename(argv[0]);
    const char *payload = NULL;

    if (strcmp(name, "nano") == 0) {
        payload = "libandropy_nano.so";
    } else if (strcmp(name, "clear") == 0) {
        payload = "libandropy_clear.so";
    } else if (strcmp(name, "tset") == 0 || strcmp(name, "reset") == 0) {
        payload = "libandropy_tset.so";
    } else if (is_coreutils_command(name)) {
        payload = "libandropy_coreutils.so";
    }

    if (payload == NULL) {
        if (is_pending_command(name)) return print_pending_command(name);
        fprintf(stderr, "andropy-tool-launcher: unknown command: %s\n", name);
        return 127;
    }

    char dir[PATH_MAX];
    char target[PATH_MAX];
    if (native_dir(dir, sizeof(dir)) != 0) {
        perror("andropy-tool-launcher: native dir");
        return 127;
    }
    if (snprintf(target, sizeof(target), "%s/%s", dir, payload) >= (int)sizeof(target)) {
        fprintf(stderr, "andropy-tool-launcher: target path too long\n");
        return 127;
    }

    char **child_argv = calloc((size_t)argc + 1, sizeof(char *));
    if (child_argv == NULL) {
        perror("andropy-tool-launcher: calloc");
        return 127;
    }
    child_argv[0] = (char *)name;
    for (int i = 1; i < argc; i++) child_argv[i] = argv[i];

    execv(target, child_argv);
    fprintf(stderr, "andropy-tool-launcher: exec %s failed: %s\n", target, strerror(errno));
    return 127;
}
