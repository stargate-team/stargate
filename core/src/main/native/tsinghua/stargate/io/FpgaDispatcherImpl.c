#include <limits.h>
#include <riffa.h>
#include <stdio.h>
#include <stddef.h>
#include <stdint.h>

#include "pci_util.h"
#include "FpgaDispatcherImpl.h"

#ifdef _LP64
#define jlong_to_ptr(a) ((void*)(a))
#define ptr_to_jlong(a) ((jlong)(a))
#else
#define jlong_to_ptr(a) ((void*)(int)(a))
#define ptr_to_jlong(a) ((jlong)(int)(a))
#endif

JNIEXPORT jint JNICALL Java_tsinghua_stargate_io_FpgaDispatcherImpl_read0
  (JNIEnv *env, jclass obj, jobject fdo, jint core, jlong address, jint len, jlong timeout)
{
    void *buf = (void *)jlong_to_ptr(address);
    jlong fd = get_fd_val(env, fdo);
    return fpga_recv((fpga_t *)fd, core, buf, len >> 2, timeout) << 2;
}

JNIEXPORT jint JNICALL Java_tsinghua_stargate_io_FpgaDispatcherImpl_write0
  (JNIEnv *env, jclass obj, jobject fdo, jint channel, jlong address, jint len, jint
   off, jint last, jlong timeout)
{
    void *buf = (void *)jlong_to_ptr(address);
    jlong fd = get_fd_val(env, fdo);
    return fpga_send((fpga_t *)fd, channel, buf, len >> 2, off, last, timeout) << 2;
}

JNIEXPORT jlong JNICALL Java_tsinghua_stargate_io_FpgaDispatcherImpl_open0
  (JNIEnv *env, jclass obj, jint id)
{
    fpga_t *fd = fpga_open((int)id);
    return (jlong)fd;
}

JNIEXPORT jint JNICALL Java_tsinghua_stargate_io_FpgaDispatcherImpl_list0
  (JNIEnv *env, jclass obj, jobject info)
{
    int r;
  	int i;
  	fpga_info_list list;
  	jclass cls;
  	jstring name;
  	jmethodID setNumFpgas;
  	jmethodID setName;
  	jmethodID setId;
  	jmethodID setNumChannels;
  	jmethodID setVendorId;
  	jmethodID setDeviceId;
  	if ((r = fpga_list(&list)) == 0) {
  		cls = (*env)->FindClass(env, "tsinghua/stargate/io/Fpga$FpgaInfo");
  		setNumFpgas = (*env)->GetMethodID(env, cls, "setNumFpgas", "(I)V");
  		(*env)->CallVoidMethod(env, info, setNumFpgas, list.num_fpgas);
  		for (i = 0; i < list.num_fpgas; i++) {
  			setName = (*env)->GetMethodID(env, cls, "setName", "(ILjava/lang/String;)V");
  			name = (*env)->NewStringUTF(env, list.name[i]);
  			(*env)->CallVoidMethod(env, info, setName, i, name);
  			setNumChannels = (*env)->GetMethodID(env, cls, "setNumChannels", "(II)V");
  			(*env)->CallVoidMethod(env, info, setNumChannels, i, list.num_chnls[i]);
  			setId = (*env)->GetMethodID(env, cls, "setId", "(II)V");
  			(*env)->CallVoidMethod(env, info, setId, i, list.id[i]);
  			setVendorId = (*env)->GetMethodID(env, cls, "setVendorId", "(II)V");
  			(*env)->CallVoidMethod(env, info, setVendorId, i, list.vendor_id[i]);
  			setDeviceId = (*env)->GetMethodID(env, cls, "setDeviceId", "(II)V");
  			(*env)->CallVoidMethod(env, info, setDeviceId, i, list.device_id[i]);
  		}
  	}
  	return r;
}

JNIEXPORT void JNICALL Java_tsinghua_stargate_io_FpgaDispatcherImpl_reset0
  (JNIEnv *env, jclass obj, jobject fdo)
{
    jlong fd = get_fd_val(env, fdo);
    fpga_reset((fpga_t *)fd);
}

JNIEXPORT void JNICALL Java_tsinghua_stargate_io_FpgaDispatcherImpl_close0
  (JNIEnv *env, jclass obj, jobject fdo)
{
    jlong fd = get_fd_val(env, fdo);
    fpga_close((fpga_t *)fd);
}
