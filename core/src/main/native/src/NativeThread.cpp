/*
 * Copyright (c) 2000, 2012, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */

#include <sys/types.h>
#include <string.h>

#include "NativeThread.h"

#ifdef __linux__
  #include <pthread.h>
  #include <sys/signal.h>
  #define INTERRUPT_SIGNAL (__SIGRTMAX - 2)
#elif __solaris__
  #include <thread.h>
  #include <signal.h>
  #define INTERRUPT_SIGNAL (SIGRTMAX - 2)
#elif _ALLBSD_SOURCE
  #include <pthread.h>
  #include <signal.h>
  #define INTERRUPT_SIGNAL SIGIO
#else
  #error "missing platform-specific definition here"
#endif

static void
nullHandler(int sig)
{
}

/*
 * Class:     org_apache_stargate_io_NativeThread
 * Method:    current
 * Signature: ()J
 */
JNIEXPORT jlong JNICALL Java_tsinghua_stargate_io_NativeThread_current
  (JNIEnv *env, jclass cl)
{
#ifdef __solaris__
    return (jlong)thr_self();
#else
    return (jlong)pthread_self();
#endif
}

/*
 * Class:     org_apache_stargate_io_NativeThread
 * Method:    signal
 * Signature: (J)V
 */
JNIEXPORT void JNICALL Java_tsinghua_stargate_io_NativeThread_signal
  (JNIEnv *env, jclass cl, jlong thread)
{
    int ret;
#ifdef __solaris__
    ret = thr_kill((thread_t)thread, INTERRUPT_SIGNAL);
#else
    ret = pthread_kill((pthread_t)thread, INTERRUPT_SIGNAL);
#endif
}

/*
 * Class:     org_apache_stargate_io_NativeThread
 * Method:    init
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_tsinghua_stargate_io_NativeThread_init
  (JNIEnv *env, jclass cl)
{
    sigset_t ss;
    struct sigaction sa, osa;
    sa.sa_handler = nullHandler;
    sa.sa_flags = 0;
    sigemptyset(&sa.sa_mask);
    sigaction(INTERRUPT_SIGNAL, &sa, &osa);
}
