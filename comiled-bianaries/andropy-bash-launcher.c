#include <stdlib.h>
#include <string.h>
#include <unistd.h>

int main(int argc, char **argv) {
    const char *cwd = getenv("ANDROPY_START_REAL");
    if (cwd == NULL || cwd[0] == '\0') {
        cwd = getenv("ANDROPY_HOME_REAL");
    }
    const char *bash = getenv("ANDROPY_BASH_PATH");

    if (cwd != NULL && cwd[0] != '\0') {
        chdir(cwd);
    }

    if (bash == NULL || bash[0] == '\0') {
        return 127;
    }

    int first_arg = 1;
    if (argc > 0 && argv[0] != NULL && argv[0][0] == '-') {
        first_arg = 0;
    }

    char **next_argv = calloc((size_t) argc + 2, sizeof(char *));
    if (next_argv == NULL) {
        return 127;
    }

    next_argv[0] = "bash";
    int out = 1;
    for (int i = first_arg; i < argc; i++) {
        next_argv[out++] = argv[i];
    }
    next_argv[out] = NULL;

    execv(bash, next_argv);
    return 127;
}
