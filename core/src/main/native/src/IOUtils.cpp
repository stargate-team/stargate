/*
 * Copyright 2017 The Tsinghua University
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

#include <unistd.h>

#include "IOUtils.h"

#define CHECK_NULL(x)                           \
    do {                                        \
        if ((x) == NULL) {                      \
            return;                             \
        }                                       \
    } while (0)                                 \

static jfieldID fd_fdID;

/*
 * Class:     org_apache_stargate_util_IOUtils
 * Method:    fdVal
 * Signature: (Lorg/apache/stargate/io/FpgaDescriptor;)I
 */
JNIEXPORT jlong JNICALL Java_tsinghua_stargate_io_IOUtils_fdVal
  (JNIEnv *env, jclass clazz, jobject fdo)
{
    return env->GetLongField(fdo, fd_fdID);
}

/*
 * Class:     org_apache_stargate_util_IOUtils
 * Method:    setfdVal
 * Signature: (Lorg/apache/stargate/io/FpgaDescriptor;I)V
 */
JNIEXPORT void JNICALL Java_tsinghua_stargate_io_IOUtils_setfdVal
  (JNIEnv *env, jclass clazz, jobject fdo, jlong val)
{
    env->SetLongField(fdo, fd_fdID, val);
}

/*
 * Class:     org_apache_stargate_util_IOUtils
 * Method:    iovMax
 * Signature: ()I
 */
JNIEXPORT jint JNICALL Java_tsinghua_stargate_io_IOUtils_iovMax
  (JNIEnv *env, jclass clazz)
{
    jlong iov_max = sysconf(_SC_IOV_MAX);
    if (iov_max == -1)
        iov_max = 16;
    return (jint)iov_max;
}

/*
 * Class:     org_apache_stargate_util_IOUtils
 * Method:    initIDs
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_tsinghua_stargate_io_IOUtils_initIDs
  (JNIEnv *env, jclass clazz)
{
    CHECK_NULL(clazz = env->FindClass("tsinghua/stargate/io/FpgaDescriptor"));
    CHECK_NULL(fd_fdID = env->GetFieldID(clazz, "fd", "J"));
}

/**
 * This function returns the int fd value from FPGA descriptor.
 * Note: CHECK_NULL() is defined in jni_util.h.
 */
jlong get_fd_val(JNIEnv *env, jobject fdo)
{
    return env->GetLongField(fdo, fd_fdID);
}
