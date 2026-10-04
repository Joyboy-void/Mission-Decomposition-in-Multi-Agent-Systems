cmake_minimum_required(VERSION 3.20)

project(
    ltlf_spot_bridge
    LANGUAGES CXX
)


# C++ configuration

set(CMAKE_CXX_STANDARD 20)
set(CMAKE_CXX_STANDARD_REQUIRED ON)
set(CMAKE_CXX_EXTENSIONS OFF)


# Spot installation
#
# Preferred:
#
#   SPOT_HOME=/path/to/project/tools/spot/install
#
# CMake override:
#
#   -DSPOT_ROOT=<path>

if(
    DEFINED ENV{SPOT_HOME}
    AND NOT "$ENV{SPOT_HOME}" STREQUAL ""
)

    file(
        TO_CMAKE_PATH
        "$ENV{SPOT_HOME}"
        SPOT_ROOT_DEFAULT
    )

else()

    set(SPOT_ROOT_DEFAULT "")

endif()


set(
    SPOT_ROOT
    "${SPOT_ROOT_DEFAULT}"
    CACHE PATH
    "Spot installation prefix"
)


if(SPOT_ROOT STREQUAL "")

    message(
        FATAL_ERROR
        "Spot installation not found. "
        "Set SPOT_HOME or pass -DSPOT_ROOT=<path>."
    )

endif()


message(
    STATUS
    "Using Spot installation: ${SPOT_ROOT}"
)


# JNI

find_package(JNI REQUIRED)

message(
    STATUS
    "JNI include directories: ${JNI_INCLUDE_DIRS}"
)


# Spot headers

find_path(
    SPOT_INCLUDE_DIR
    NAMES spot/tl/parse.hh
    HINTS
        "${SPOT_ROOT}/include"
    REQUIRED
)

message(
    STATUS
    "Spot include directory: ${SPOT_INCLUDE_DIR}"
)


# Spot library

find_library(
    SPOT_LIBRARY
    NAMES
        spot
        libspot
    HINTS
        "${SPOT_ROOT}/lib"
    REQUIRED
)

message(
    STATUS
    "Spot library: ${SPOT_LIBRARY}"
)


# BuDDyX

find_library(
    BDDX_LIBRARY
    NAMES
        bddx
        libbddx
    HINTS
        "${SPOT_ROOT}/lib"
    REQUIRED
)

message(
    STATUS
    "BDDX library: ${BDDX_LIBRARY}"
)


# Generated JNI headers
#
# Maven generates them under:
#
#   target/generated-sources/headers
#
# Maven may override this with:
#
#   -DJNI_GENERATED_HEADERS=<path>

if(NOT DEFINED JNI_GENERATED_HEADERS)

    set(
        JNI_GENERATED_HEADERS
        "${CMAKE_SOURCE_DIR}/target/generated-sources/headers"
        CACHE PATH
        "Directory containing generated JNI headers"
    )

endif()


message(
    STATUS
    "JNI generated headers: ${JNI_GENERATED_HEADERS}"
)


# Native JNI bridge

add_library(
    ltlf_spot_bridge
    SHARED
    src/main/cpp/SpotNativeBridge.cpp
)


# Include directories

target_include_directories(
    ltlf_spot_bridge
    PRIVATE

    ${JNI_INCLUDE_DIRS}

    ${SPOT_INCLUDE_DIR}

    ${JNI_GENERATED_HEADERS}
)


# Link libraries

target_link_libraries(
    ltlf_spot_bridge
    PRIVATE

    ${SPOT_LIBRARY}

    ${BDDX_LIBRARY}
)


# Output name

if(WIN32)

    set_target_properties(
        ltlf_spot_bridge
        PROPERTIES
        PREFIX ""
        OUTPUT_NAME "ltlf_spot_bridge"
    )

else()

    set_target_properties(
        ltlf_spot_bridge
        PROPERTIES
        OUTPUT_NAME "ltlf_spot_bridge"
    )

endif()


# Output directory

set_target_properties(
    ltlf_spot_bridge
    PROPERTIES
    RUNTIME_OUTPUT_DIRECTORY
        "${CMAKE_BINARY_DIR}/native"
    LIBRARY_OUTPUT_DIRECTORY
        "${CMAKE_BINARY_DIR}/native"
    ARCHIVE_OUTPUT_DIRECTORY
        "${CMAKE_BINARY_DIR}/native"
)


# Runtime search path for Unix-like systems

if(UNIX)

    set_target_properties(
        ltlf_spot_bridge
        PROPERTIES
        BUILD_RPATH "${SPOT_ROOT}/lib"
        INSTALL_RPATH "${SPOT_ROOT}/lib"
    )

endif()