SUMMARY = "Zstandard - Fast real-time compression algorithm"
DESCRIPTION = "Zstandard is a fast lossless compression algorithm, targeting \
real-time compression scenarios at zlib-level and better compression ratios. \
It's backed by a very fast entropy stage, provided by Huff0 and FSE library."
HOMEPAGE = "http://www.zstd.net/"
SECTION = "console/utils"

LICENSE = "BSD-3-Clause | GPL-2.0-only"
LIC_FILES_CHKSUM = "file://LICENSE;md5=0822a32f7acdbe013606746641746ee8 \
                    file://COPYING;md5=39bba7d2cf0ba1036f2a6e2be52fe3f0 \
                    "

SRC_URI = "git://github.com/facebook/zstd.git;branch=release;protocol=https \
           file://run-ptest \
           "

SRCREV = "f8745da6ff1ad1e7bab384bd1f9d742439278e99"
UPSTREAM_CHECK_GITTAGREGEX = "v(?P<pver>\d+(\.\d+)+)"

CVE_PRODUCT = "zstandard"

PACKAGECONFIG ??= ""
PACKAGECONFIG[lz4] = "HAVE_LZ4=1,HAVE_LZ4=0,lz4"
PACKAGECONFIG[lzma] = "HAVE_LZMA=1,HAVE_LZMA=0,xz"
PACKAGECONFIG[zlib] = "HAVE_ZLIB=1,HAVE_ZLIB=0,zlib"

# See programs/README.md for how to use this
ZSTD_LEGACY_SUPPORT ??= "4"

EXTRA_OEMAKE += "V=1"

do_compile () {
    oe_runmake ${PACKAGECONFIG_CONFARGS} ZSTD_LEGACY_SUPPORT=${ZSTD_LEGACY_SUPPORT}
    oe_runmake ${PACKAGECONFIG_CONFARGS} ZSTD_LEGACY_SUPPORT=${ZSTD_LEGACY_SUPPORT} -C contrib/pzstd
}

do_install () {
    oe_runmake install 'DESTDIR=${D}'
    oe_runmake install 'DESTDIR=${D}' PREFIX=${prefix} -C contrib/pzstd
}

PACKAGE_BEFORE_PN = "libzstd"

FILES:libzstd = "${libdir}/libzstd${SOLIBS}"

BBCLASSEXTEND = "native nativesdk"

inherit ptest

PTEST_BINARIES = "datagen fullbench poolTests fuzzer zstreamtest \
                  invalidDictionaries legacy decodecorpus"

do_compile_ptest() {
    oe_runmake -C ${S}/tests ${PTEST_BINARIES} \
        ZSTD_LEGACY_SUPPORT=${ZSTD_LEGACY_SUPPORT}
}

do_install_ptest() {
    install -d ${D}${PTEST_PATH}/tests ${D}${PTEST_PATH}/programs

    # Test binaries and shell script
    for t in ${PTEST_BINARIES} playTests.sh; do
        install -m 0755 ${S}/tests/$t ${D}${PTEST_PATH}/tests/
    done

    # Test data and cli-tests
    for d in golden-decompression golden-decompression-errors \
             golden-compression golden-dictionaries dict-files cli-tests; do
        cp -r ${S}/tests/$d ${D}${PTEST_PATH}/tests/
    done

    # Source files used as dictionary training corpus
    install -m 0644 ${S}/tests/*.c ${D}${PTEST_PATH}/tests/
    install -m 0644 ${S}/programs/*.c ${S}/programs/*.h ${D}${PTEST_PATH}/programs/
    install -m 0755 ${S}/programs/zstdgrep ${S}/programs/zstdless ${D}${PTEST_PATH}/programs/

    # Remove tests incompatible with ptest environment:
    # levels.sh: --ultra/--max levels require >8GB RAM for allocation
    rm -f ${D}${PTEST_PATH}/tests/cli-tests/compression/levels.sh
    # window-resize.sh: 1GB window size requires >4GB RAM
    rm -f ${D}${PTEST_PATH}/tests/cli-tests/compression/window-resize.sh
    # compress-file-to-dir-without-write-perm.sh: ptest runs as root,
    # which bypasses filesystem permission checks
    rm -f ${D}${PTEST_PATH}/tests/cli-tests/file-stat/compress-file-to-dir-without-write-perm.sh
    # cltools/: zstdgrep requires GNU grep --label option not available
    # in BusyBox grep
    rm -rf ${D}${PTEST_PATH}/tests/cli-tests/cltools
}

RDEPENDS:${PN}-ptest += "bash coreutils python3-core python3-io"
