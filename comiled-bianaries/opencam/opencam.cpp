#define PY_SSIZE_T_CLEAN
#include <Python.h>

#include <sys/socket.h>
#include <sys/un.h>
#include <unistd.h>

#include <cerrno>
#include <cstring>
#include <fstream>
#include <sstream>
#include <string>
#include <vector>

static std::string env_socket_name() {
    const char* value = getenv("ANDROPY_OPENCAM_SOCKET");
    if (value && value[0]) return std::string(value);
    const char* pkg = getenv("ANDROID_PACKAGE_NAME");
    if (pkg && pkg[0]) return std::string("andropy_opencam_") + pkg;
    return "andropy_opencam_com.andropy.ide";
}

static std::string opencam_request(const std::string& command) {
    std::string socket_name = env_socket_name();
    if (socket_name.size() + 1 >= sizeof(sockaddr_un::sun_path)) {
        PyErr_SetString(PyExc_RuntimeError, "opencam socket name is too long");
        return "";
    }

    int fd = socket(AF_UNIX, SOCK_STREAM | SOCK_CLOEXEC, 0);
    if (fd < 0) {
        PyErr_SetFromErrno(PyExc_OSError);
        return "";
    }

    sockaddr_un addr{};
    addr.sun_family = AF_UNIX;
    addr.sun_path[0] = '\0';
    memcpy(addr.sun_path + 1, socket_name.data(), socket_name.size());
    socklen_t len = static_cast<socklen_t>(offsetof(sockaddr_un, sun_path) + 1 + socket_name.size());
    if (connect(fd, reinterpret_cast<sockaddr*>(&addr), len) != 0) {
        int saved = errno;
        close(fd);
        errno = saved;
        PyErr_SetFromErrnoWithFilename(PyExc_OSError, socket_name.c_str());
        return "";
    }

    std::string payload = command + "\n";
    const char* data = payload.data();
    size_t remaining = payload.size();
    while (remaining > 0) {
        ssize_t written = write(fd, data, remaining);
        if (written < 0) {
            if (errno == EINTR) continue;
            int saved = errno;
            close(fd);
            errno = saved;
            PyErr_SetFromErrno(PyExc_OSError);
            return "";
        }
        data += written;
        remaining -= static_cast<size_t>(written);
    }

    std::string response;
    char buffer[1024];
    while (true) {
        ssize_t read_count = read(fd, buffer, sizeof(buffer));
        if (read_count < 0) {
            if (errno == EINTR) continue;
            int saved = errno;
            close(fd);
            errno = saved;
            PyErr_SetFromErrno(PyExc_OSError);
            return "";
        }
        if (read_count == 0) break;
        response.append(buffer, static_cast<size_t>(read_count));
    }
    close(fd);
    while (!response.empty() && (response.back() == '\n' || response.back() == '\r')) response.pop_back();
    return response;
}

static PyObject* py_stream(PyObject*, PyObject* args, PyObject* kwargs) {
    int enable = 1;
    static const char* keywords[] = {"enable", nullptr};
    if (!PyArg_ParseTupleAndKeywords(args, kwargs, "|p", const_cast<char**>(keywords), &enable)) return nullptr;
    std::string response = opencam_request(enable ? "STREAM 1" : "STREAM 0");
    if (PyErr_Occurred()) return nullptr;
    return PyUnicode_FromString(response.c_str());
}

static PyObject* py_capture(PyObject*, PyObject* args, PyObject* kwargs) {
    int count = 1;
    const char* path = nullptr;
    if (PyTuple_Size(args) > 0) {
        PyObject* value = PyTuple_GetItem(args, 0);
        count = static_cast<int>(PyLong_AsLong(value));
        if (PyErr_Occurred()) return nullptr;
    }
    if (kwargs) {
        PyObject* value = PyDict_GetItemString(kwargs, "count");
        if (!value) value = PyDict_GetItemString(kwargs, "Count");
        if (value) {
            count = static_cast<int>(PyLong_AsLong(value));
            if (PyErr_Occurred()) return nullptr;
        }
        value = PyDict_GetItemString(kwargs, "path");
        if (!value) value = PyDict_GetItemString(kwargs, "Path");
        if (value) {
            path = PyUnicode_AsUTF8(value);
            if (PyErr_Occurred()) return nullptr;
        }
    }
    if (count < 1) count = 1;
    if (path && path[0]) {
        std::string response = opencam_request("FRAME");
        if (PyErr_Occurred()) return nullptr;
        if (response.rfind("FRAME ", 0) != 0) return PyUnicode_FromString(response.c_str());
        size_t newline = response.find('\n');
        if (newline == std::string::npos) {
            PyErr_SetString(PyExc_RuntimeError, "invalid opencam frame response");
            return nullptr;
        }
        std::istringstream header(response.substr(0, newline));
        std::string marker;
        int width = 0;
        int height = 0;
        std::string format;
        size_t size = 0;
        long long frame_at = 0;
        header >> marker >> width >> height >> format >> size >> frame_at;
        std::string bytes = response.substr(newline + 1);
        if (marker != "FRAME" || width <= 0 || height <= 0 || format != "gray8" || bytes.size() < size) {
            PyErr_SetString(PyExc_RuntimeError, "invalid opencam frame payload");
            return nullptr;
        }
        std::ofstream file(path, std::ios::binary);
        if (!file) {
            PyErr_SetFromErrnoWithFilename(PyExc_OSError, path);
            return nullptr;
        }
        file << "P5\n" << width << " " << height << "\n255\n";
        file.write(bytes.data(), static_cast<std::streamsize>(size));
        file.close();
        if (!file) {
            PyErr_SetString(PyExc_OSError, "failed writing opencam capture");
            return nullptr;
        }
        std::string saved = "OK capture path=" + std::string(path) + " width=" + std::to_string(width)
                + " height=" + std::to_string(height) + " format=pgm";
        return PyUnicode_FromString(saved.c_str());
    }
    std::string response = opencam_request("CAPTURE " + std::to_string(count));
    if (PyErr_Occurred()) return nullptr;
    return PyUnicode_FromString(response.c_str());
}

static PyObject* py_info(PyObject*, PyObject*) {
    std::string response = opencam_request("INFO");
    if (PyErr_Occurred()) return nullptr;
    return PyUnicode_FromString(response.c_str());
}

static PyObject* py_frame(PyObject*, PyObject*) {
    std::string response = opencam_request("FRAME");
    if (PyErr_Occurred()) return nullptr;
    if (response.rfind("FRAME ", 0) != 0) return Py_BuildValue("{s:s}", "error", response.c_str());
    size_t newline = response.find('\n');
    if (newline == std::string::npos) {
        PyErr_SetString(PyExc_RuntimeError, "invalid opencam frame response");
        return nullptr;
    }
    std::istringstream header(response.substr(0, newline));
    std::string marker;
    int width = 0;
    int height = 0;
    std::string format;
    size_t size = 0;
    long long frame_at = 0;
    header >> marker >> width >> height >> format >> size >> frame_at;
    std::string bytes = response.substr(newline + 1);
    if (marker != "FRAME" || width <= 0 || height <= 0 || format.empty() || bytes.size() < size) {
        PyErr_SetString(PyExc_RuntimeError, "invalid opencam frame payload");
        return nullptr;
    }
    return Py_BuildValue("{s:i,s:i,s:s,s:N,s:L}",
                         "width", width,
                         "height", height,
                         "format", format.c_str(),
                         "data", PyBytes_FromStringAndSize(bytes.data(), static_cast<Py_ssize_t>(size)),
                         "timestamp_ms", frame_at);
}

static PyObject* py_read(PyObject*, PyObject*) {
    PyObject* frame = py_frame(nullptr, nullptr);
    if (!frame) return nullptr;
    return Py_BuildValue("NO", Py_True, frame);
}

static PyObject* py_display(PyObject*, PyObject*) {
    std::string response = opencam_request("DISPLAY");
    if (PyErr_Occurred()) return nullptr;
    return PyUnicode_FromString(response.c_str());
}

static PyMethodDef display_methods[] = {
        {"buffer", reinterpret_cast<PyCFunction>(py_display), METH_NOARGS, "Launch the Aqua camera framebuffer display."},
        {nullptr, nullptr, 0, nullptr}
};

static PyModuleDef display_module = {
        PyModuleDef_HEAD_INIT,
        "opencam.display",
        "Aqua opencam display helpers.",
        -1,
        display_methods
};

static PyObject* create_display_module() {
    return PyModule_Create(&display_module);
}

static PyMethodDef opencam_methods[] = {
        {"stream", reinterpret_cast<PyCFunction>(py_stream), METH_VARARGS | METH_KEYWORDS, "Start or stop the app camera stream."},
        {"capture", reinterpret_cast<PyCFunction>(py_capture), METH_VARARGS | METH_KEYWORDS, "Capture metadata or save the latest camera frame as a grayscale PGM."},
        {"info", reinterpret_cast<PyCFunction>(py_info), METH_NOARGS, "Return current camera bridge status."},
        {"frame", reinterpret_cast<PyCFunction>(py_frame), METH_NOARGS, "Return the latest gray8 camera frame as a dict."},
        {"read", reinterpret_cast<PyCFunction>(py_read), METH_NOARGS, "OpenCV-like read returning (True, frame_dict)."},
        {"display", reinterpret_cast<PyCFunction>(py_display), METH_NOARGS, "Launch the Aqua camera framebuffer display."},
        {nullptr, nullptr, 0, nullptr}
};

static PyModuleDef opencam_module = {
        PyModuleDef_HEAD_INIT,
        "opencam",
        "Tiny Aqua Android camera bridge module.",
        -1,
        opencam_methods
};

PyMODINIT_FUNC PyInit_opencam(void) {
    PyObject* module = PyModule_Create(&opencam_module);
    if (!module) return nullptr;

    PyObject* display = create_display_module();
    if (!display) {
        Py_DECREF(module);
        return nullptr;
    }
    if (PyModule_AddObject(module, "display", display) != 0) {
        Py_DECREF(display);
        Py_DECREF(module);
        return nullptr;
    }
    PyObject* modules = PyImport_GetModuleDict();
    if (modules) PyDict_SetItemString(modules, "opencam.display", display);
    return module;
}
