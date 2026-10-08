#!/usr/bin/env sh

set -eu

# Machine Decomposition - Linux / macOS setup

#

# Responsibilities:

#   1. Detect a usable JDK 21 or newer.
#   2. Prefer a native Unix/Linux/macOS JDK over Windows tools
#      inherited through WSL.
#   3. Reuse a globally installed native Maven when available.
#      If no native/global Maven exists, install project-local
#      Apache Maven.
#   4. Verify the native C/C++ and GNU build tools required by
#      the Spot source build.
#   5. Reuse a native Graphviz installation or build Graphviz 16.1.0
#      locally under tools/graphviz/install.
#   6. Install Spot 2.16 into tools/spot/install.
#   7. Persist SPOT_HOME, JAVA_HOME, Graphviz, and project-local Maven
#      environment when applicable.
#
# The project targets Java 21.
# A JDK newer than 21 may be used to build the project, while
# Maven compiles with --release 21 as configured in the POMs.
#
# This script is intended for Linux, WSL, and macOS.
# It does not configure Windows-only tools.

# Project paths

PROJECT_ROOT="$(

    CDPATH= cd -- "$(dirname -- "$0")/.." && pwd

)"

# Graphviz

GRAPHVIZ_VERSION="16.1.0"

GRAPHVIZ_DIR="$PROJECT_ROOT/tools/graphviz"

GRAPHVIZ_DOWNLOAD_DIR="$GRAPHVIZ_DIR/downloads"

GRAPHVIZ_SOURCE_DIR="$GRAPHVIZ_DIR/src"

GRAPHVIZ_SOURCE="$GRAPHVIZ_SOURCE_DIR/graphviz-$GRAPHVIZ_VERSION"

GRAPHVIZ_HOME="$GRAPHVIZ_DIR/install"

GRAPHVIZ_DOT="$GRAPHVIZ_HOME/bin/dot"

GRAPHVIZ_ARCHIVE="$GRAPHVIZ_DOWNLOAD_DIR/graphviz-$GRAPHVIZ_VERSION.tar.gz"

GRAPHVIZ_URL="https://gitlab.com/api/v4/projects/4207231/packages/generic/graphviz-releases/${GRAPHVIZ_VERSION}/graphviz-${GRAPHVIZ_VERSION}.tar.gz"

# Spot

SPOT_VERSION="2.16"

SPOT_URL="https://www.lre.epita.fr/dload/spot/spot-2.16.tar.gz"

SPOT_DIR="$PROJECT_ROOT/tools/spot"

SPOT_DOWNLOAD_DIR="$SPOT_DIR/downloads"

SPOT_SOURCE_DIR="$SPOT_DIR/src"

SPOT_ARCHIVE="$SPOT_DOWNLOAD_DIR/spot-${SPOT_VERSION}.tar.gz"

SPOT_SOURCE="$SPOT_SOURCE_DIR/spot-${SPOT_VERSION}"

SPOT_HOME="$SPOT_DIR/install"

# Maven

MAVEN_VERSION="3.10.0"

MAVEN_DIR="$PROJECT_ROOT/tools/maven"

MAVEN_DOWNLOAD_DIR="$MAVEN_DIR/downloads"

MAVEN_HOME_ROOT="$MAVEN_DIR/install"

MAVEN_INSTALL_DIR="$MAVEN_HOME_ROOT/apache-maven-${MAVEN_VERSION}"

MAVEN_BIN="$MAVEN_INSTALL_DIR/bin"

MAVEN_EXECUTABLE="$MAVEN_BIN/mvn"

MAVEN_ARCHIVE="$MAVEN_DOWNLOAD_DIR/apache-maven-${MAVEN_VERSION}-bin.tar.gz"

MAVEN_URL="https://dlcdn.apache.org/maven/maven-3/${MAVEN_VERSION}/binaries/apache-maven-${MAVEN_VERSION}-bin.tar.gz"

# Java

JAVA_MIN_VERSION="21"

# Build configuration

MAKE_JOBS="${MAKE_JOBS:-2}"

# Command line mode

CHECK_ONLY=0

SHOW_HELP=0

case "${1:-}" in

    --check)

        CHECK_ONLY=1

        ;;

    --help|-h)

        SHOW_HELP=1

        ;;

    "")

        ;;

    *)

        printf '%s\n' "Unknown argument: $1" >&2

        printf '%s\n' "Usage: ./scripts/setup.sh [--check|--help]" >&2

        exit 2

        ;;

esac

if [ "$SHOW_HELP" -eq 1 ]; then

    cat <<EOF

Machine Decomposition setup

Usage:

  ./scripts/setup.sh

  ./scripts/setup.sh --check

Environment overrides:

  MAKE_JOBS=2       Number of parallel make jobs for Spot.

                    Example: MAKE_JOBS=4 ./scripts/setup.sh

EOF

    exit 0

fi

# Utilities

say() {

    printf '%s\n' "$*"

}

fail() {

    say "Error: $*" >&2

    exit 1

}

command_exists() {

    command -v "$1" >/dev/null 2>&1

}

is_mounted_windows_path() {

    path="$1"

    case "$path" in

        /mnt/*)

            return 0

            ;;

        *.exe|*.EXE|*.cmd|*.CMD|*.bat|*.BAT)

            return 0

            ;;

    esac

    return 1

}

prepend_path_once() {

    new_entry="$1"

    [ -n "$new_entry" ] || return 0

    [ -d "$new_entry" ] || return 0

    case ":${PATH:-}:" in

        *":$new_entry:"*)

            ;;

        *)

            PATH="$new_entry:${PATH:-}"

            export PATH

            ;;

    esac

}

# Java version helpers

java_major_from_executable() {

    java_exe="$1"

    [ -x "$java_exe" ] || return 1

    version="$(

        "$java_exe" -version 2>&1 |

            awk -F'"' '/version/ {print $2; exit}' || true

    )"

    [ -n "$version" ] || return 1

    case "$version" in

        1.*)

            version="${version#1.}"

            ;;

    esac

    major="${version%%.*}"

    major="${major%%-*}"

    major="${major%%+*}"

    case "$major" in

        ''|*[!0-9]*)

            return 1

            ;;

    esac

    printf '%s\n' "$major"

}

java_home_from_executable() {

    java_exe="$1"

    [ -x "$java_exe" ] || return 1

    canonical="$java_exe"

    if command_exists readlink; then

        resolved="$(readlink -f "$java_exe" 2>/dev/null || true)"

        if [ -n "$resolved" ] && [ -x "$resolved" ]; then

            canonical="$resolved"

        fi

    fi

    if command_exists realpath; then

        resolved="$(realpath "$canonical" 2>/dev/null || true)"

        if [ -n "$resolved" ] && [ -x "$resolved" ]; then

            canonical="$resolved"

        fi

    fi

    bin_dir="$(CDPATH= cd -- "$(dirname -- "$canonical")" 2>/dev/null && pwd)"

    home_dir="$(CDPATH= cd -- "$bin_dir/.." 2>/dev/null && pwd)"

    if [ -x "$home_dir/bin/java" ] &&

       [ -x "$home_dir/bin/javac" ]; then

        printf '%s\n' "$home_dir"

        return 0

    fi

    return 1

}

check_java_home_candidate() {

    candidate="$1"

    [ -n "$candidate" ] || return 1

    [ -x "$candidate/bin/java" ] || return 1

    [ -x "$candidate/bin/javac" ] || return 1

    is_mounted_windows_path "$candidate" && return 1

    major="$(java_major_from_executable "$candidate/bin/java" 2>/dev/null || true)"

    [ -n "$major" ] || return 1

    [ "$major" -ge "$JAVA_MIN_VERSION" ] || return 1

    printf '%s\n' "$candidate"

}

find_linux_java_home() {

    # 1. Explicit JAVA_HOME, when it is a real Unix JDK.

    if [ -n "${JAVA_HOME:-}" ]; then

        home="$(check_java_home_candidate "$JAVA_HOME" 2>/dev/null || true)"

        if [ -n "$home" ]; then

            printf '%s\n' "$home"

            return 0

        fi

    fi

    # 2. Search PATH entries, but ignore WSL-mounted Windows tools.

    old_ifs="${IFS}"

    IFS=':'

    for path_entry in ${PATH:-}; do

        [ -n "$path_entry" ] || continue

        case "$path_entry" in

            /mnt/*)

                continue

                ;;

        esac

        java_candidate="$path_entry/java"

        if [ -x "$java_candidate" ]; then

            home="$(java_home_from_executable "$java_candidate" 2>/dev/null || true)"

            if [ -n "$home" ]; then

                if check_java_home_candidate "$home" >/dev/null 2>&1; then

                    IFS="$old_ifs"

                    printf '%s\n' "$home"

                    return 0

                fi

            fi

        fi

    done

    IFS="$old_ifs"

    # 3. Common Linux JDK locations.

    for candidate in \
        /usr/lib/jvm/* \
        /usr/java/* \
        /opt/java/* \
        "$HOME"/.sdkman/candidates/java/* \
        "$HOME"/.local/jdks/*

    do

        [ -d "$candidate" ] || continue

        home="$(check_java_home_candidate "$candidate" 2>/dev/null || true)"

        if [ -n "$home" ]; then

            printf '%s\n' "$home"

            return 0

        fi

    done

    return 1

}

find_macos_java_home() {

    # Prefer JAVA_HOME when it points to a usable JDK.

    if [ -n "${JAVA_HOME:-}" ]; then

        home="$(check_java_home_candidate "$JAVA_HOME" 2>/dev/null || true)"

        if [ -n "$home" ]; then

            printf '%s\n' "$home"

            return 0

        fi

    fi

    # macOS provides /usr/libexec/java_home.

    if [ -x /usr/libexec/java_home ]; then

        candidate="$(

            /usr/libexec/java_home -v "${JAVA_MIN_VERSION}+" 2>/dev/null || true

        )"

        if [ -n "$candidate" ]; then

            home="$(check_java_home_candidate "$candidate" 2>/dev/null || true)"

            if [ -n "$home" ]; then

                printf '%s\n' "$home"

                return 0

            fi

        fi

    fi

    # Search native PATH entries.

    old_ifs="${IFS}"

    IFS=':'

    for path_entry in ${PATH:-}; do

        [ -n "$path_entry" ] || continue

        case "$path_entry" in

            /mnt/*)

                continue

                ;;

        esac

        java_candidate="$path_entry/java"

        if [ -x "$java_candidate" ]; then

            home="$(java_home_from_executable "$java_candidate" 2>/dev/null || true)"

            if [ -n "$home" ] &&

               check_java_home_candidate "$home" >/dev/null 2>&1

            then

                IFS="$old_ifs"

                printf '%s\n' "$home"

                return 0

            fi

        fi

    done

    IFS="$old_ifs"

    # Homebrew / common macOS JDK locations.

    for candidate in \
        /Library/Java/JavaVirtualMachines/*/Contents/Home \
        /System/Library/Java/JavaVirtualMachines/*/Contents/Home \
        "$HOME"/.sdkman/candidates/java/* \
        "$HOME"/.local/jdks/*

    do

        [ -d "$candidate" ] || continue

        home="$(check_java_home_candidate "$candidate" 2>/dev/null || true)"

        if [ -n "$home" ]; then

            printf '%s\n' "$home"

            return 0

        fi

    done

    return 1

}

find_java_home() {

    os="$(uname -s)"

    case "$os" in

        Darwin)

            find_macos_java_home

            ;;

        *)

            find_linux_java_home

            ;;

    esac

}

java_ok() {

    home="$(find_java_home 2>/dev/null || true)"

    [ -n "$home" ]

}

select_java() {

    home="$(find_java_home 2>/dev/null || true)"

    if [ -z "$home" ]; then

        return 1

    fi

    JAVA_HOME="$home"

    export JAVA_HOME

    prepend_path_once "$JAVA_HOME/bin"

    major="$(java_major_from_executable "$JAVA_HOME/bin/java" 2>/dev/null || true)"

    [ -n "$major" ] || return 1

    [ "$major" -ge "$JAVA_MIN_VERSION" ] || return 1

    return 0

}

# Maven candidate helpers

is_valid_maven_executable() {

    candidate="$1"

    [ -f "$candidate" ] || return 1

    [ -x "$candidate" ] || return 1

    is_mounted_windows_path "$candidate" && return 1

    case "$candidate" in

        /mnt/*)

            return 1

            ;;

    esac

    return 0

}

find_maven_in_path() {

    old_ifs="${IFS}"

    IFS=':'

    for path_entry in ${PATH:-}; do

        [ -n "$path_entry" ] || continue

        case "$path_entry" in

            /mnt/*)

                continue

                ;;

        esac

        candidate="$path_entry/mvn"

        # Do not mistake our project-local Maven for a global Maven.

        if [ "$candidate" = "$MAVEN_EXECUTABLE" ]; then

            continue

        fi

        if is_valid_maven_executable "$candidate"; then

            IFS="$old_ifs"

            printf '%s\n' "$candidate"

            return 0

        fi

    done

    IFS="$old_ifs"

    return 1

}

find_global_maven_executable() {

    # Common native Unix Maven installations.

    for candidate in \
        /usr/bin/mvn \
        /usr/local/bin/mvn \
        /opt/maven/bin/mvn \
        "$HOME"/bin/mvn \
        "$HOME"/.local/bin/mvn \
        "$HOME"/.sdkman/candidates/maven/current/bin/mvn

    do

        if is_valid_maven_executable "$candidate"; then

            printf '%s\n' "$candidate"

            return 0

        fi

    done

    # Finally inspect PATH while explicitly ignoring WSL Windows paths.

    candidate="$(find_maven_in_path 2>/dev/null || true)"

    if [ -n "$candidate" ]; then

        printf '%s\n' "$candidate"

        return 0

    fi

    return 1

}

maven_installed() {

    # Keep the intended project behavior:

    #   1. Reuse an existing native/global Maven.

    #   2. Otherwise use project-local Maven when it exists.

    #

    # A Windows Maven inherited through WSL is not considered global.

    candidate="$(find_global_maven_executable 2>/dev/null || true)"

    if [ -n "$candidate" ]; then

        return 0

    fi

    [ -x "$MAVEN_EXECUTABLE" ]

}

select_maven() {

    # IMPORTANT:

    # Prefer a native/global Maven whenever one is available.

    # Fall back to project-local Maven only when no native/global

    # Maven can be found.

    candidate="$(find_global_maven_executable 2>/dev/null || true)"

    if [ -n "$candidate" ]; then

        SELECTED_MAVEN="$candidate"

        export SELECTED_MAVEN

        maven_bin_dir="$(CDPATH= cd -- "$(dirname -- "$candidate")" && pwd)"

        prepend_path_once "$maven_bin_dir"

        return 0

    fi

    if [ -x "$MAVEN_EXECUTABLE" ]; then

        SELECTED_MAVEN="$MAVEN_EXECUTABLE"

        export SELECTED_MAVEN

        prepend_path_once "$MAVEN_BIN"

        return 0

    fi

    return 1

}

maven_is_project_local() {

    [ "${SELECTED_MAVEN:-}" = "$MAVEN_EXECUTABLE" ]

}

# Maven installation

install_maven() {

    mkdir -p \
        "$MAVEN_DOWNLOAD_DIR" \
        "$MAVEN_HOME_ROOT"

    # Download

    if [ ! -f "$MAVEN_ARCHIVE" ]; then

        say ""

        say "Downloading Apache Maven ${MAVEN_VERSION}..."

        curl -fL \
            "$MAVEN_URL" \
            -o "$MAVEN_ARCHIVE"

    else

        say ""

        say "Maven archive already exists."

        say "Reusing: $MAVEN_ARCHIVE"

    fi

    # Extract

    if [ ! -x "$MAVEN_EXECUTABLE" ]; then

        say ""

        say "Extracting Apache Maven ${MAVEN_VERSION}..."

        tar -xzf \
            "$MAVEN_ARCHIVE" \
            -C "$MAVEN_HOME_ROOT"

    else

        say ""

        say "Apache Maven ${MAVEN_VERSION} is already installed."

    fi

    # Verify

    if [ ! -x "$MAVEN_EXECUTABLE" ]; then

        fail "Maven installation failed. Expected executable: $MAVEN_EXECUTABLE"

    fi

    prepend_path_once "$MAVEN_BIN"

    SAY_MAVEN_HOME="$MAVEN_INSTALL_DIR"

    export SAY_MAVEN_HOME

    say ""

    say "Apache Maven ${MAVEN_VERSION} installed."

    say "Maven home: $MAVEN_INSTALL_DIR"

}

install_maven_interactively() {

    printf \
        'Missing dependency: Maven. Download Apache Maven %s into the project? [Y/n] ' \
        "$MAVEN_VERSION"

    read -r answer

    answer="${answer:-Y}"

    case "$answer" in

        Y|y)

            install_maven

            ;;

        *)

            fail "Setup cancelled."

            ;;

    esac

}

# Java installation

install_java() {

    os="$(uname -s)"

    say ""

    say "No Java ${JAVA_MIN_VERSION}+ JDK was found."

    say "The project requires a Java ${JAVA_MIN_VERSION}+ JDK."

    say ""

    case "$os" in

        Darwin)

            if ! command_exists brew; then

                fail "Homebrew is required for automatic Java installation on macOS."

            fi

            install_one \
                "Java 21" \
                brew install openjdk@21

            java21_prefix="$(

                brew --prefix openjdk@21 2>/dev/null || true

            )"

            if [ -n "$java21_prefix" ] &&

               [ -x "$java21_prefix/bin/java" ] &&

               [ -x "$java21_prefix/bin/javac" ]

            then

                JAVA_HOME="$java21_prefix"

                export JAVA_HOME

                prepend_path_once "$JAVA_HOME/bin"

            fi

            ;;

        *)

            if command_exists apt-get; then

                install_one \
                    "Java 21" \
                    sh -c \
                    'sudo apt-get update && sudo apt-get install -y openjdk-21-jdk'

            elif command_exists dnf; then

                install_one \
                    "Java 21" \
                    sudo dnf install -y java-21-openjdk-devel

            elif command_exists pacman; then

                install_one \
                    "Java 21" \
                    sudo pacman -S --needed jdk21-openjdk

            else

                fail \
                    "No supported package manager found. Install Java 21 or newer manually."

            fi

            ;;

    esac

    # Refresh the Java selection after package installation.

    if ! select_java; then

        fail "Java ${JAVA_MIN_VERSION}+ is still not available after installation."

    fi

}

# Package-manager installation helper

install_one() {

    label="$1"

    shift

    printf \
        'Missing dependency: %s. Install it using the detected package manager? [Y/n] ' \
        "$label"

    read -r answer

    answer="${answer:-Y}"

    case "$answer" in

        Y|y)

            "$@"

            ;;

        *)

            fail "Setup cancelled."

            ;;

    esac

}

# Dependency check

check_tools() {

    missing=""

    # Java

    java_ok || missing="$missing java>=${JAVA_MIN_VERSION}"

    # Maven

    maven_installed || missing="$missing maven"

    # CMake

    command_exists cmake || missing="$missing cmake"

    # Ninja

    command_exists ninja || missing="$missing ninja"

    # Native C++ compiler

    command_exists c++ || missing="$missing c++"

    # pkg-config

    command_exists pkg-config || missing="$missing pkg-config"

    # make

    command_exists make || missing="$missing make"

    # curl

    command_exists curl || missing="$missing curl"

    # tar

    command_exists tar || missing="$missing tar"

    # Autotools

    command_exists autoconf || missing="$missing autoconf"

    command_exists automake || missing="$missing automake"

    command_exists libtool || missing="$missing libtool"

    # Graphviz PNG rendering requires the GD development package
    # when Graphviz is built from source.
    case "$(uname -s)" in
        Linux*)
            if command_exists apt-get && command_exists dpkg; then
                dpkg -s libgd-dev >/dev/null 2>&1 || missing="$missing libgd-dev"
            elif command_exists dnf && command_exists rpm; then
                rpm -q gd-devel >/dev/null 2>&1 || missing="$missing gd-devel"
            elif command_exists pacman; then
                pacman -Q gd >/dev/null 2>&1 || missing="$missing gd"
            fi
            ;;
        Darwin*)
            if command_exists brew; then
                brew list --formula gd >/dev/null 2>&1 || missing="$missing gd"
            fi
            ;;
    esac

    if [ -n "$missing" ]; then

        say ""

        say "Missing dependencies:$missing"

        return 1

    fi

    return 0

}

# Linux dependency installation

install_linux_dependencies() {

    # CMake

    if ! command_exists cmake; then

        install_one \
            "CMake" \
            sh -c \
            'sudo apt-get update && sudo apt-get install -y cmake'

    fi

    # Ninja

    if ! command_exists ninja; then

        install_one \
            "Ninja" \
            sh -c \
            'sudo apt-get update && sudo apt-get install -y ninja-build'

    fi

    # Compiler

    if ! command_exists c++; then

        install_one \
            "C++ compiler" \
            sh -c \
            'sudo apt-get update && sudo apt-get install -y build-essential'

    fi

    # make

    if ! command_exists make; then

        install_one \
            "make" \
            sh -c \
            'sudo apt-get update && sudo apt-get install -y make'

    fi

    # pkg-config

    if ! command_exists pkg-config; then

        install_one \
            "pkg-config" \
            sh -c \
            'sudo apt-get update && sudo apt-get install -y pkg-config'

    fi

    # curl

    if ! command_exists curl; then

        install_one \
            "curl" \
            sh -c \
            'sudo apt-get update && sudo apt-get install -y curl'

    fi

    # tar

    if ! command_exists tar; then

        install_one \
            "tar" \
            sh -c \
            'sudo apt-get update && sudo apt-get install -y tar'

    fi

    # autoconf

    if ! command_exists autoconf; then

        install_one \
            "autoconf" \
            sh -c \
            'sudo apt-get update && sudo apt-get install -y autoconf'

    fi

    # automake

    if ! command_exists automake; then

        install_one \
            "automake" \
            sh -c \
            'sudo apt-get update && sudo apt-get install -y automake'

    fi

    # libtool

    if ! command_exists libtool; then
        install_one \
            "libtool" \
            sh -c \
            'sudo apt-get update && sudo apt-get install -y libtool libtool-bin'
    fi

    # GD development headers/libraries.
    # Graphviz uses GD to provide PNG rendering support.
    if ! dpkg -s libgd-dev >/dev/null 2>&1; then

        install_one \
            "libgd-dev (required for Graphviz PNG support)" \
            sh -c \
            'sudo apt-get update && sudo apt-get install -y libgd-dev'

    fi

}

# Fedora / RHEL dependency installation

install_fedora_dependencies() {

    # CMake

    if ! command_exists cmake; then

        install_one \
            "CMake" \
            sudo dnf install -y cmake

    fi

    # Ninja

    if ! command_exists ninja; then

        install_one \
            "Ninja" \
            sudo dnf install -y ninja-build

    fi

    # Compiler

    if ! command_exists c++; then

        install_one \
            "C++ compiler" \
            sudo dnf install -y gcc-c++

    fi

    # make

    if ! command_exists make; then

        install_one \
            "make" \
            sudo dnf install -y make

    fi

    # pkg-config

    if ! command_exists pkg-config; then

        install_one \
            "pkg-config" \
            sudo dnf install -y pkgconf-pkg-config

    fi

    # curl

    if ! command_exists curl; then

        install_one \
            "curl" \
            sudo dnf install -y curl

    fi

    # tar

    if ! command_exists tar; then

        install_one \
            "tar" \
            sudo dnf install -y tar

    fi

    # autoconf

    if ! command_exists autoconf; then

        install_one \
            "autoconf" \
            sudo dnf install -y autoconf

    fi

    # automake

    if ! command_exists automake; then

        install_one \
            "automake" \
            sudo dnf install -y automake

    fi

    # libtool

    if ! command_exists libtool; then

        install_one \
            "libtool" \
            sudo dnf install -y libtool

    fi

    # GD development package for Graphviz PNG support.
    if ! command_exists rpm || ! rpm -q gd-devel >/dev/null 2>&1; then

        install_one \
            "gd-devel (required for Graphviz PNG support)" \
            sudo dnf install -y gd-devel

    fi

}

# Arch dependency installation

install_arch_dependencies() {

    # CMake

    if ! command_exists cmake; then

        install_one \
            "CMake" \
            sudo pacman -S --needed cmake

    fi

    # Ninja

    if ! command_exists ninja; then

        install_one \
            "Ninja" \
            sudo pacman -S --needed ninja

    fi

    # Compiler

    if ! command_exists c++; then

        install_one \
            "C++ compiler" \
            sudo pacman -S --needed gcc

    fi

    # make

    if ! command_exists make; then

        install_one \
            "make" \
            sudo pacman -S --needed make

    fi

    # pkg-config

    if ! command_exists pkg-config; then

        install_one \
            "pkg-config" \
            sudo pacman -S --needed pkgconf

    fi

    # curl

    if ! command_exists curl; then

        install_one \
            "curl" \
            sudo pacman -S --needed curl

    fi

    # tar

    if ! command_exists tar; then

        install_one \
            "tar" \
            sudo pacman -S --needed tar

    fi

    # autoconf

    if ! command_exists autoconf; then

        install_one \
            "autoconf" \
            sudo pacman -S --needed autoconf

    fi

    # automake

    if ! command_exists automake; then

        install_one \
            "automake" \
            sudo pacman -S --needed automake

    fi

    # libtool

    if ! command_exists libtool; then

        install_one \
            "libtool" \
            sudo pacman -S --needed libtool

    fi

    # GD library for Graphviz PNG support.
    if ! pacman -Q gd >/dev/null 2>&1; then

        install_one \
            "gd (required for Graphviz PNG support)" \
            sudo pacman -S --needed gd

    fi

}

# macOS dependency installation

install_macos_dependencies() {

    if ! command_exists brew; then

        fail "Homebrew is required for automatic dependency installation on macOS."

    fi

    # CMake

    if ! command_exists cmake; then

        install_one \
            "CMake" \
            brew install cmake

    fi

    # Ninja

    if ! command_exists ninja; then

        install_one \
            "Ninja" \
            brew install ninja

    fi

    # C++ compiler

    if ! command_exists c++; then

        install_one \
            "C++ compiler" \
            brew install gcc

    fi

    # pkg-config

    if ! command_exists pkg-config; then

        install_one \
            "pkg-config" \
            brew install pkg-config

    fi

    # make

    if ! command_exists make; then

        install_one \
            "make" \
            brew install make

    fi

    # curl

    if ! command_exists curl; then

        install_one \
            "curl" \
            brew install curl

    fi

    # tar

    if ! command_exists tar; then

        install_one \
            "tar" \
            brew install gnu-tar

    fi

    # autoconf

    if ! command_exists autoconf; then

        install_one \
            "autoconf" \
            brew install autoconf

    fi

    # automake

    if ! command_exists automake; then

        install_one \
            "automake" \
            brew install automake

    fi

    # libtool

    if ! command_exists libtool; then

        install_one \
            "libtool" \
            brew install libtool

    fi

    # GD library for Graphviz PNG support.
    if ! brew list --formula gd >/dev/null 2>&1; then

        install_one \
            "gd (required for Graphviz PNG support)" \
            brew install gd

    fi

}

# Install all dependencies

install_tools() {

    os="$(uname -s)"

    # Java

    if ! java_ok; then

        install_java

    fi

    # Platform-specific build tools

    case "$os" in

        Darwin)

            install_macos_dependencies

            ;;

        Linux)

            if command_exists apt-get; then

                install_linux_dependencies

            elif command_exists dnf; then

                install_fedora_dependencies

            elif command_exists pacman; then

                install_arch_dependencies

            else

                fail \
                    "No supported Linux package manager found. Install CMake, Ninja, a C++20 compiler, pkg-config, make, curl, tar, autoconf, automake and libtool manually."

            fi

            ;;

        *)

            fail \
                "Unsupported operating system: $os. This script supports Linux, WSL and macOS."

            ;;

    esac

    # Maven

    # WSL Windows Maven executables are deliberately ignored.

    if ! maven_installed; then

        install_maven_interactively

    fi

}

# Environment persistence

persist_environment() {

    env_file="$HOME/.machine-decomposition-env"

    selected_java_home="$(find_java_home 2>/dev/null || true)"

    if [ -z "$selected_java_home" ]; then

        fail "Could not select a usable Java ${JAVA_MIN_VERSION}+ JDK."

    fi

    selected_java_major="$(

        java_major_from_executable \
            "$selected_java_home/bin/java" \
            2>/dev/null || true

    )"

    [ -n "$selected_java_major" ] ||

        fail "Could not determine the selected Java version."

    if [ "$selected_java_major" -lt "$JAVA_MIN_VERSION" ]; then

        fail "Selected Java version $selected_java_major is below the required version $JAVA_MIN_VERSION."

    fi

    # Determine the Maven choice.

    if ! select_maven; then

        fail "Could not select a usable native Maven installation."

    fi

    maven_choice="$SELECTED_MAVEN"

    # Write persistent project environment.

    {

        printf '# machine-decomposition environment\n'

        printf 'export JAVA_HOME="%s"\n' "$selected_java_home"

        printf 'export SPOT_HOME="%s"\n' "$SPOT_HOME"

        if [ -x "$GRAPHVIZ_DOT" ]; then

            printf 'export GRAPHVIZ_HOME="%s"\n' "$GRAPHVIZ_HOME"

        fi

        if maven_is_project_local; then

            printf 'export MAVEN_HOME="%s"\n' "$MAVEN_INSTALL_DIR"

        fi

        # Put native selected tool locations before inherited

        # Windows PATH entries in WSL.

        printf 'case ":${PATH:-}:" in *":$JAVA_HOME/bin:"*) ;; *) export PATH="$JAVA_HOME/bin:$PATH" ;; esac\n'

        printf 'case ":${PATH:-}:" in *":$SPOT_HOME/bin:"*) ;; *) export PATH="$SPOT_HOME/bin:$PATH" ;; esac\n'

        if maven_is_project_local; then

            printf 'case ":${PATH:-}:" in *":$MAVEN_HOME/bin:"*) ;; *) export PATH="$MAVEN_HOME/bin:$PATH" ;; esac\n'

        else

            maven_bin_dir="$(

                CDPATH= cd -- "$(dirname -- "$maven_choice")" && pwd

            )"

            printf 'case ":${PATH:-}:" in *":%s:"*) ;; *) export PATH="%s:$PATH" ;; esac\n' \
                "$maven_bin_dir" \
                "$maven_bin_dir"

        fi

    } > "$env_file"

    # Source from ~/.profile.

    profile="$HOME/.profile"

    marker="# machine-decomposition environment"

    touch "$profile"

    if ! grep -Fq "$marker" "$profile"; then

        {

            printf '\n%s\n' "$marker"

            printf '%s\n' \
                '[ -f "$HOME/.machine-decomposition-env" ] && . "$HOME/.machine-decomposition-env"'

        } >> "$profile"

    fi

    # Current shell.

    export JAVA_HOME="$selected_java_home"

    prepend_path_once "$JAVA_HOME/bin"

    export SPOT_HOME="$SPOT_HOME"

    prepend_path_once "$SPOT_HOME/bin"

    if [ -x "$GRAPHVIZ_DOT" ]; then

        export GRAPHVIZ_HOME="$GRAPHVIZ_HOME"

        prepend_path_once "$GRAPHVIZ_HOME/bin"

    fi

    if maven_is_project_local; then

        export MAVEN_HOME="$MAVEN_INSTALL_DIR"

        prepend_path_once "$MAVEN_HOME/bin"

    else

        maven_bin_dir="$(

            CDPATH= cd -- "$(dirname -- "$maven_choice")" && pwd

        )"

        prepend_path_once "$maven_bin_dir"

    fi

}

# Graphviz installation checks

graphviz_dot_from_path() {

    if ! command_exists dot; then

        return 1

    fi

    candidate="$(command -v dot)"

    case "$candidate" in

        /mnt/*|*.exe|*.EXE)

            return 1

            ;;

    esac

    if [ -x "$candidate" ]; then

        printf '%s\n' "$candidate"

        return 0

    fi

    return 1

}

graphviz_installed() {

    if graphviz_dot_from_path >/dev/null 2>&1; then

        return 0

    fi

    [ -x "$GRAPHVIZ_DOT" ]

}

graphviz_png_supported() {

    [ -x "$GRAPHVIZ_DOT" ] || return 1

    printf '%s\n' 'digraph G { A -> B; }' |
        "$GRAPHVIZ_DOT" -Tpng >/dev/null 2>&1

}

graphviz_svg_supported() {

    [ -x "$GRAPHVIZ_DOT" ] || return 1

    printf '%s\n' 'digraph G { A -> B; }' |
        "$GRAPHVIZ_DOT" -Tsvg >/dev/null 2>&1

}

install_graphviz_from_source() {

    mkdir -p "$GRAPHVIZ_DOWNLOAD_DIR" "$GRAPHVIZ_SOURCE_DIR"

    # --------------------------------------------------------
    # Download the official Graphviz release source package.
    #
    # IMPORTANT:
    # Do not use GitLab's /-/archive/<tag>/ archive here.
    # That is a repository snapshot and does not contain the
    # generated ./configure script required by this build.
    # --------------------------------------------------------

    archive_is_valid=0

    if [ -f "$GRAPHVIZ_ARCHIVE" ]; then
        if tar -tzf "$GRAPHVIZ_ARCHIVE" 2>/dev/null |
            grep -q '/configure$'
        then
            archive_is_valid=1
        fi
    fi

    if [ "$archive_is_valid" -eq 0 ]; then

        if [ -f "$GRAPHVIZ_ARCHIVE" ]; then
            say ""
            say "Existing Graphviz archive is not a release source package."
            say "Replacing: $GRAPHVIZ_ARCHIVE"
            rm -f "$GRAPHVIZ_ARCHIVE"
        fi

        say ""
        say "Downloading Graphviz ${GRAPHVIZ_VERSION}..."

        curl -fL \
            "$GRAPHVIZ_URL" \
            -o "$GRAPHVIZ_ARCHIVE"

    else

        say ""
        say "Graphviz release archive already exists."
        say "Reusing: $GRAPHVIZ_ARCHIVE"

    fi

    # --------------------------------------------------------
    # Ensure the source tree came from the correct release
    # package. This also repairs a source tree extracted from
    # the old repository snapshot.
    # --------------------------------------------------------

    source_is_valid=0

    if [ -d "$GRAPHVIZ_SOURCE" ] &&
       [ -f "$GRAPHVIZ_SOURCE/configure" ]
    then
        source_is_valid=1
    fi

    if [ "$source_is_valid" -eq 0 ]; then

        if [ -d "$GRAPHVIZ_SOURCE" ]; then
            say ""
            say "Existing Graphviz source tree is incomplete."
            say "Removing: $GRAPHVIZ_SOURCE"
            rm -rf "$GRAPHVIZ_SOURCE"
        fi

        say ""
        say "Extracting Graphviz ${GRAPHVIZ_VERSION}..."

        tar -xzf \
            "$GRAPHVIZ_ARCHIVE" \
            -C "$GRAPHVIZ_SOURCE_DIR"

    else

        say ""
        say "Graphviz source already exists."
        say "Reusing: $GRAPHVIZ_SOURCE"

    fi

    if [ ! -f "$GRAPHVIZ_SOURCE/configure" ]; then
        fail \
            "Graphviz source package does not contain ./configure: $GRAPHVIZ_SOURCE"
    fi

    if [ -x "$GRAPHVIZ_DOT" ]; then

        if graphviz_png_supported && graphviz_svg_supported; then
            say "Graphviz ${GRAPHVIZ_VERSION} is already installed locally."
            say "  PNG support : OK"
            say "  SVG support : OK"
            return 0
        fi

        say ""
        say "Existing local Graphviz installation is missing PNG/SVG support."
        say "Rebuilding Graphviz ${GRAPHVIZ_VERSION} with the required rendering dependencies..."

        rm -rf "$GRAPHVIZ_HOME"

        # Clean the previous source build so configure detects the newly
        # installed GD development package and enables PNG rendering.
        cd "$GRAPHVIZ_SOURCE"
        make distclean >/dev/null 2>&1 || true
    fi

    # --------------------------------------------------------
    # Configure
    # --------------------------------------------------------

    say ""
    say "Configuring Graphviz ${GRAPHVIZ_VERSION}..."

    cd "$GRAPHVIZ_SOURCE"

    ./configure \
        --prefix="$GRAPHVIZ_HOME" \
        --disable-python

    # --------------------------------------------------------
    # Build
    # --------------------------------------------------------

    say ""
    say "Building Graphviz ${GRAPHVIZ_VERSION} with ${MAKE_JOBS} jobs..."

    make -j"$MAKE_JOBS"

    # --------------------------------------------------------
    # Install
    # --------------------------------------------------------

    say ""
    say "Installing Graphviz ${GRAPHVIZ_VERSION} locally..."

    make install

    # --------------------------------------------------------
    # Verify
    # --------------------------------------------------------

    if [ ! -x "$GRAPHVIZ_DOT" ]; then
        fail \
            "Graphviz build completed, but dot was not found at $GRAPHVIZ_DOT"
    fi

    if ! graphviz_png_supported; then
        fail \
            "Graphviz build completed, but PNG rendering support is missing. Ensure libgd-dev/gd-devel/gd is installed before building Graphviz."
    fi

    if ! graphviz_svg_supported; then
        fail \
            "Graphviz build completed, but SVG rendering support is missing."
    fi

    say "Graphviz ${GRAPHVIZ_VERSION} installed successfully."
    say "  PNG support : OK"
    say "  SVG support : OK"
}

ensure_graphviz() {

    if graphviz_dot_from_path >/dev/null 2>&1; then

        selected_graphviz_dot="$(graphviz_dot_from_path)"

        if printf '%s\n' 'digraph G { A -> B; }' |
           "$selected_graphviz_dot" -Tpng >/dev/null 2>&1 &&
           printf '%s\n' 'digraph G { A -> B; }' |
           "$selected_graphviz_dot" -Tsvg >/dev/null 2>&1
        then
            say "Graphviz : global/native"
            say "  $selected_graphviz_dot"
            say "  PNG support : OK"
            say "  SVG support : OK"
            return 0
        fi

        say "Global/native Graphviz was found, but PNG/SVG support is incomplete."
        say "Building a project-local Graphviz installation with the required dependencies."
    else
        say "Graphviz is not installed globally."
    fi

    install_graphviz_from_source

    prepend_path_once "$GRAPHVIZ_HOME/bin"

}

# Spot installation checks

spot_installed() {

    header="$SPOT_HOME/include/spot/tl/parse.hh"

    pc_file="$SPOT_HOME/lib/pkgconfig/libspot.pc"

    [ -f "$header" ] &&

    [ -f "$pc_file" ]

}

spot_installation_binaries_present() {

    [ -f "$SPOT_HOME/bin/ltlfilt" ] ||

    [ -f "$SPOT_HOME/bin/autfilt" ] ||

    [ -f "$SPOT_HOME/bin/spot-x" ]

}

# Spot source integrity / extraction

ensure_spot_source() {

    mkdir -p \
        "$SPOT_DOWNLOAD_DIR" \
        "$SPOT_SOURCE_DIR"

    # Download

    if [ ! -f "$SPOT_ARCHIVE" ]; then

        say ""

        say "Downloading Spot ${SPOT_VERSION}..."

        curl -fL \
            "$SPOT_URL" \
            -o "$SPOT_ARCHIVE"

    else

        say ""

        say "Spot archive already exists."

        say "Reusing: $SPOT_ARCHIVE"

    fi

    # Extract

    if [ ! -d "$SPOT_SOURCE" ]; then

        say ""

        say "Extracting Spot ${SPOT_VERSION}..."

        tar -xzf \
            "$SPOT_ARCHIVE" \
            -C "$SPOT_SOURCE_DIR"

    else

        say ""

        say "Spot source already exists."

        say "Reusing: $SPOT_SOURCE"

    fi

    if [ ! -d "$SPOT_SOURCE" ]; then

        fail "Spot source directory was not found after extraction: $SPOT_SOURCE"

    fi

}

# Spot build configuration

configure_spot() {

    say ""

    say "Configuring Spot ${SPOT_VERSION}..."

    cd "$SPOT_SOURCE"

    ./configure \
        --prefix="$SPOT_HOME" \
        --disable-python

}

# Spot build

build_spot() {

    ensure_spot_source

    # Remove only an incomplete install.

    #

    # Never delete the downloaded archive or source tree.

    if [ -d "$SPOT_HOME" ] && ! spot_installed; then

        say ""

        say "Removing previous incomplete Spot installation..."

        rm -rf "$SPOT_HOME"

    fi

    mkdir -p "$SPOT_HOME"

    # Configure

    configure_spot

    # Build

    say ""

    say "Building Spot ${SPOT_VERSION} with ${MAKE_JOBS} jobs..."

    make -j"$MAKE_JOBS"

    # Install

    say ""

    say "Installing Spot ${SPOT_VERSION}..."

    make install

    # Verify

    if ! spot_installed; then

        fail \
            "Spot build completed, but the expected installation files were not found under $SPOT_HOME"

    fi

    say ""

    say "Spot ${SPOT_VERSION} installed successfully."

}

# Check Java and Maven consistency

check_selected_java_and_maven() {

    if ! select_java; then

        return 1

    fi

    if ! select_maven; then

        return 1

    fi

    # Maven must be executable.

    [ -x "$SELECTED_MAVEN" ] || return 1

    # Ask Maven for its version. This also catches cases where

    # a globally installed Maven cannot start with the selected JDK.

    "$SELECTED_MAVEN" -version >/dev/null 2>&1 || return 1

    return 0

}

# Check-only mode

if [ "$CHECK_ONLY" -eq 1 ]; then

    say "========================================"

    say "Machine Decomposition Environment Check"

    say "========================================"

    say ""

    # Java

    java_home_check="$(find_java_home 2>/dev/null || true)"

    if [ -n "$java_home_check" ]; then

        java_version_check="$(

            java_major_from_executable \
                "$java_home_check/bin/java" \
                2>/dev/null || true

        )"

        say "Java >= 21  : OK (version ${java_version_check})"

        say "JAVA_HOME   : $java_home_check"

    else

        say "Java >= 21  : MISSING"

        if command_exists java; then

            system_java_version="$(

                java_major_from_executable \
                    "$(command -v java)" \
                    2>/dev/null || true

            )"

            if [ -n "$system_java_version" ]; then

                say "System Java : $system_java_version"

            fi

        fi

    fi

    say ""

    # Maven

    local_maven_ok=0

    global_maven_ok=0

    global_maven_path=""

    if [ -x "$MAVEN_EXECUTABLE" ]; then

        local_maven_ok=1

    fi

    global_maven_path="$(find_global_maven_executable 2>/dev/null || true)"

    if [ -n "$global_maven_path" ]; then

        global_maven_ok=1

    fi

    if [ "$global_maven_ok" -eq 1 ]; then

        say "Maven       : OK (global/native)"

        say "  $global_maven_path"

    elif [ "$local_maven_ok" -eq 1 ]; then

        say "Maven       : OK (project-local)"

        say "  $MAVEN_EXECUTABLE"

    else

        say "Maven       : MISSING"

    fi

    say ""

    # Native build tools

    if command_exists cmake; then

        say "CMake       : OK"

    else

        say "CMake       : MISSING"

    fi

    if command_exists ninja; then

        say "Ninja       : OK"

    else

        say "Ninja       : MISSING"

    fi

    if command_exists c++; then

        say "C++         : OK"

    else

        say "C++         : MISSING"

    fi

    if command_exists pkg-config; then

        say "pkg-config  : OK"

    else

        say "pkg-config  : MISSING"

    fi

    if command_exists make; then

        say "make        : OK"

    else

        say "make        : MISSING"

    fi

    if command_exists curl; then

        say "curl        : OK"

    else

        say "curl        : MISSING"

    fi

    if command_exists tar; then

        say "tar         : OK"

    else

        say "tar         : MISSING"

    fi

    if command_exists autoconf; then

        say "autoconf    : OK"

    else

        say "autoconf    : MISSING"

    fi

    if command_exists automake; then

        say "automake    : OK"

    else

        say "automake    : MISSING"

    fi

    if command_exists libtool; then

        say "libtool     : OK"

    else

        say "libtool     : MISSING"

    fi

    say ""

    # Graphviz

    if graphviz_installed; then

        graphviz_dot_check="$(graphviz_dot_from_path 2>/dev/null || true)"

        if [ -z "$graphviz_dot_check" ] && [ -x "$GRAPHVIZ_DOT" ]; then

            graphviz_dot_check="$GRAPHVIZ_DOT"

        fi

        say "Graphviz       : OK"

        say "  $graphviz_dot_check"

        if printf '%s\n' 'digraph G { A -> B; }' |
           "$graphviz_dot_check" -Tpng >/dev/null 2>&1
        then
            say "  PNG support  : OK"
        else
            say "  PNG support  : MISSING"
        fi

        if printf '%s\n' 'digraph G { A -> B; }' |
           "$graphviz_dot_check" -Tsvg >/dev/null 2>&1
        then
            say "  SVG support  : OK"
        else
            say "  SVG support  : MISSING"
        fi

    else

        say "Graphviz       : MISSING"

    fi

    say ""

    # Spot

    if spot_installed; then

        say "Spot ${SPOT_VERSION} : OK"

    else

        say "Spot ${SPOT_VERSION} : MISSING"

    fi

    say ""

    say "Expected project-local Maven:"

    say "  $MAVEN_EXECUTABLE"

    say "Expected SPOT_HOME:"

    say "  $SPOT_HOME"

    say ""

    exit 0

fi

# Normal setup

# First dependency check.

if ! check_tools; then

    say ""

    say "Starting dependency installation..."

    install_tools

fi

# Refresh Java/Maven choices after installation.

if ! select_java; then

    fail "Could not select a usable Java ${JAVA_MIN_VERSION}+ JDK."

fi

say ""

say "Using Java ${JAVA_MIN_VERSION}+ JDK:"

say "  $JAVA_HOME"

java_version="$(

    java_major_from_executable \
        "$JAVA_HOME/bin/java" \
        2>/dev/null || true

)"

say "Java version: $java_version"

# Maven selection.

if ! select_maven; then

    fail "Could not select a usable native Maven installation."

fi

say ""

if maven_is_project_local; then

    say "Using project-local Maven:"

    say "  $SELECTED_MAVEN"

else

    say "Using global/native Maven:"

    say "  $SELECTED_MAVEN"

fi

# Verify dependencies again.

if ! check_tools; then

    fail "Required tools are still missing."

fi

# Verify Java + Maven together.

if ! check_selected_java_and_maven; then

    fail "Java/Maven environment could not be validated."

fi

# Graphviz

ensure_graphviz

# Build / reuse Spot

if spot_installed; then

    say ""

    say "Spot ${SPOT_VERSION} is already installed."

    say "Reusing: $SPOT_HOME"

else

    build_spot

fi

# Persist environment

persist_environment

# Final output

say ""

say "========================================"

say "Setup complete"

say "========================================"

say ""

say "Java:"

say "  version $(java_major_from_executable "$JAVA_HOME/bin/java")"

say "  JAVA_HOME=$JAVA_HOME"

say "  $(command -v java)"

say ""

say "Maven:"

if maven_is_project_local; then

    say "  project-local"

else

    say "  global/native"

fi

say "  $SELECTED_MAVEN"

say ""

say "SPOT_HOME:"

say "  $SPOT_HOME"

if [ -n "${GRAPHVIZ_HOME:-}" ]; then

    say "GRAPHVIZ_HOME:"

    say "  $GRAPHVIZ_HOME"

fi

say ""

say "Environment file:"

say "  $HOME/.machine-decomposition-env"

say ""

say "Open a new terminal, then run:"

say ""

say "  mvn clean test"

say ""

say '  ./scripts/run-cli.sh "F G p"'

say ""