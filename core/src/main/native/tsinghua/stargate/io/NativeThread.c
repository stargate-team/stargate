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