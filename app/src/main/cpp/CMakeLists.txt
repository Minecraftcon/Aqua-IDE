cmake_minimum_required(VERSION 3.22.1)

project(aqua_native LANGUAGES CXX)

add_library(aqua_native SHARED
        aqua_wayland_vulkan.cpp)

target_compile_features(aqua_native PRIVATE cxx_std_17)

target_link_libraries(aqua_native
        android
        log
        vulkan)
