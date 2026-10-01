/*
 * Copyright (C) 2005-2011 by Wind River Systems, Inc.
 *
 * SPDX-License-Identifier: MIT
 * 
 */

/*
 * Pick the -32/-64 variant through __MHWORDSIZE.
 * - glibc: __WORDSIZE from bits/wordsize.h, as before
 * - musl: no bits/wordsize.h, so use the compiler's pointer size. musl
 *   defines __WORDSIZE itself in sys/reg.h and sys/user.h, hence the
 *   separate name
 * - bpf: pointers are always 8 bytes, so use the variant the sysroot has
 * - __MHWORDSIZE can be preset for preprocessors without __has_include
 */
#ifndef __MHWORDSIZE
# if defined(__has_include)
#  if __has_include(<bits/alltypes.h>)
#   if defined(__bpf__) && __has_include(<ENTER_HEADER_FILENAME_HERE-32.h>) && !__has_include(<ENTER_HEADER_FILENAME_HERE-64.h>)
#    define __MHWORDSIZE 32
#   elif defined(__bpf__) && __has_include(<ENTER_HEADER_FILENAME_HERE-64.h>) && !__has_include(<ENTER_HEADER_FILENAME_HERE-32.h>)
#    define __MHWORDSIZE 64
#   else
#    define __MHWORDSIZE (__SIZEOF_POINTER__ * 8)
#   endif
#  endif
# endif
# ifndef __MHWORDSIZE
#  include <bits/wordsize.h>
#  define __MHWORDSIZE __WORDSIZE
# endif
#endif

#if __MHWORDSIZE == 32

#ifdef _MIPS_SIM

#if _MIPS_SIM == _ABIO32
#include <ENTER_HEADER_FILENAME_HERE-32.h>
#elif _MIPS_SIM == _ABIN32
#include <ENTER_HEADER_FILENAME_HERE-n32.h>
#else
#error "Unknown _MIPS_SIM"
#endif

#else /* _MIPS_SIM is not defined */
#include <ENTER_HEADER_FILENAME_HERE-32.h>
#endif

#elif __MHWORDSIZE == 64
#include <ENTER_HEADER_FILENAME_HERE-64.h>
#else
#error "Unknown __WORDSIZE detected"
#endif /* matches #if __MHWORDSIZE == 32 */
  
