#include <unistd.h>

#include "IOUtils.h"
#include "pci_util.h"

#define CHECK_NULL(x)                           \
    do {                                        \
        if ((x) == NULL) {                      \
            return;                             \
        }                                       \
    } while (0)                                 \

static jfieldID fd_fdID;

JNIEXPORT jlong JNICALL Java_tsinghua_stargate_io_IOUtils_fdVal
  (JNIEnv *env, jclass clazz, jobject fdo)
{
    return (*env)->GetLongField(env, fdo, fd_fdID);
}

JNIEXPORT void JNICALL Java_tsinghua_stargate_io_IOUtils_setfdVal
  (JNIEnv *env, jclass clazz, jobject fdo, jlong val)
{
    (*env)->SetLongField(env, fdo, fd_fdID, val);
}

JNIEXPORT jint JNICALL Java_tsinghua_stargate_io_IOUtils_iovMax
  (JNIEnv *env, jclass clazz)
{
    jlong iov_max = sysconf(_SC_IOV_MAX);
    if (iov_max == -1)
        iov_max = 16;
    return (jint)iov_max;
}

JNIEXPORT void JNICALL Java_tsinghua_stargate_io_IOUtils_initIDs
  (JNIEnv *env, jclass clazz)
{
    CHECK_NULL(clazz = (*env)->FindClass(env, "tsinghua/stargate/io/FpgaDescriptor"));
    CHECK_NULL(fd_fdID = (*env)->GetFieldID(env, clazz, "fd", "J"));
}

/**
 * This function returns the int fd value from FPGA descriptor.
 * Note: CHECK_NULL() is defined in jni_util.h.
 */
jlong get_fd_val(JNIEnv *env, jobject fdo)
{
    return (*env)->GetLongField(env, fdo, fd_fdID);
}