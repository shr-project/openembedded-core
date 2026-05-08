SUMMARY = "Userspace RCU (read-copy-update) library"
DESCRIPTION = "A userspace RCU (read-copy-update) library. This data \
synchronization library provides read-side access which scales linearly \
with the number of cores. "
HOMEPAGE = "http://lttng.org/urcu"
BUGTRACKER = "http://lttng.org/project/issues"

LICENSE = "LGPL-2.1-or-later & MIT"
LIC_FILES_CHKSUM = "file://LICENSE.md;md5=c2a92498b6e88e276f986877995425b8 \
                    file://include/urcu/urcu.h;beginline=1;endline=18;md5=d35fe8cc2613ca70e0a624ed8bf6fef9 \
                    file://include/urcu/uatomic/x86.h;beginline=1;endline=6;md5=358d69272ba7b5f85e29e342430d440c \
                    "

SRC_URI = "http://lttng.org/files/urcu/userspace-rcu-${PV}.tar.bz2 \
           file://run-ptest \
           "

SRC_URI[sha256sum] = "850b192096eb11ebf2c70e8f97bc7da7479ee41da1bebeb44e3986908bac414f"

S = "${UNPACKDIR}/userspace-rcu-${PV}"
inherit autotools multilib_header ptest

CPPFLAGS:append:riscv64  = " -pthread -D_REENTRANT"

RDEPENDS:${PN}-ptest += "bash coreutils"

do_install:append() {
    oe_multilib_header urcu/config.h
}

do_compile_ptest() {
    oe_runmake -C ${B}/tests/unit check TESTS=
    oe_runmake -C ${B}/tests/regression check TESTS=
}

do_install_ptest() {
    install -d ${D}${PTEST_PATH}/tests/unit

    find ${B}/tests/unit -maxdepth 1 -type f -executable \
        ! -name "*.la" | while read -r t; do
        ${B}/libtool --mode=install install -m 0755 "$t" ${D}${PTEST_PATH}/tests/unit/
    done

    install -m 0755 ${S}/tests/unit/test_get_cpu_mask_from_sysfs ${D}${PTEST_PATH}/tests/unit/
    install -m 0755 ${S}/tests/unit/test_get_cpu_mask_from_sysfs_cxx ${D}${PTEST_PATH}/tests/unit/
    install -m 0755 ${S}/tests/unit/test_get_max_cpuid_from_sysfs ${D}${PTEST_PATH}/tests/unit/
    install -m 0755 ${S}/tests/unit/test_get_max_cpuid_from_sysfs_cxx ${D}${PTEST_PATH}/tests/unit/

    install -d ${D}${PTEST_PATH}/tests/regression

    find ${B}/tests/regression -maxdepth 1 -type f -executable \
        ! -name "*.la" | while read -r t; do
        ${B}/libtool --mode=install install -m 0755 "$t" ${D}${PTEST_PATH}/tests/regression/
    done

    install -m 0755 ${S}/tests/regression/*.tap ${D}${PTEST_PATH}/tests/regression/

    install -d ${D}${PTEST_PATH}/tests/utils
    install -m 0644 ${S}/tests/utils/tap.sh ${D}${PTEST_PATH}/tests/utils/
    install -m 0644 ${S}/tests/utils/utils.sh ${D}${PTEST_PATH}/tests/utils/
}
