SUMMARY = "A portable foreign function interface library"
HOMEPAGE = "http://sourceware.org/libffi/"
DESCRIPTION = "The `libffi' library provides a portable, high level programming interface to various calling \
conventions.  This allows a programmer to call any function specified by a call interface description at run \
time. FFI stands for Foreign Function Interface.  A foreign function interface is the popular name for the \
interface that allows code written in one language to call code written in another language.  The `libffi' \
library really only provides the lowest, machine dependent layer of a fully featured foreign function interface.  \
A layer must exist above `libffi' that handles type conversions for values passed between the two languages."

LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://LICENSE;md5=ce4763670c5b7756000561f9af1ab178"

SRC_URI = "${GITHUB_BASE_URI}/download/v${PV}/${BPN}-${PV}.tar.gz \
           file://not-win32.patch \
           file://run-ptest \
           "
SRC_URI[sha256sum] = "f3a3082a23b37c293a4fcd1053147b371f2ff91fa7ea1b2a52e335676bac82dc"

EXTRA_OECONF = "--disable-builddir"
EXTRA_OECONF:class-native += "--with-gcc-arch=generic"
EXTRA_OEMAKE:class-target = "LIBTOOLFLAGS='--tag=CC'"

inherit autotools texinfo multilib_header github-releases ptest

do_install:append() {
	oe_multilib_header ffi.h ffitarget.h
}

# Doesn't compile in MIPS16e mode due to use of hand-written
# assembly
MIPS_INSTRUCTION_SET = "mips"

BBCLASSEXTEND = "native nativesdk"

do_compile_ptest() {
    mkdir -p ${B}/ptest-bins
    cd ${S}/testsuite

    for suite in libffi.call libffi.closures libffi.complex libffi.go libffi.threads; do
        [ -d $suite ] || continue
        extra=""
        [ "$suite" = "libffi.threads" ] && extra="-lpthread"

        for src in $suite/*.c $suite/*.cc; do
            [ -f "$src" ] || continue
            grep -q "dg-do run" "$src" || continue
            name=$(basename "${src%.*}")
            [ "$name" = "complex_int" ] && continue
            case "$src" in
                *.cc) compiler="${CXX}";;
                *)    compiler="${CC}";;
            esac
            $compiler ${CFLAGS} ${LDFLAGS} \
                -I${B}/include -I${B} -I${S}/testsuite/libffi.call \
                -o ${B}/ptest-bins/${suite}__${name} "$src" \
                -L${B}/.libs -lffi $extra 2>/dev/null || true
        done
    done

    for t in test-call test-callback; do
        ${CC} ${CFLAGS} ${LDFLAGS} -I${B}/include -I${B} \
            -o ${B}/ptest-bins/libffi.bhaible__${t} \
            libffi.bhaible/${t}.c -L${B}/.libs -lffi || true
    done
}

do_install_ptest() {
    install -d ${D}${PTEST_PATH}/tests
    for t in ${B}/ptest-bins/*; do
        [ -f "$t" ] && install -m 0755 "$t" ${D}${PTEST_PATH}/tests/
    done
}
