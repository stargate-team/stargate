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

#include "FpgaDispatcherImpl.h"
#include "common.h"
#include "kmeanslayer.h"
#include "string.h"
#include "pci_util.h"

#ifdef _LP64
#define jlong_to_ptr(a) ((void*)(a))
#define ptr_to_jlong(a) ((jlong)(a))
#else
#define jlong_to_ptr(a) ((void*)(int)(a))
#define ptr_to_jlong(a) ((jlong)(int)(a))
#endif


std::string jstring2str(JNIEnv* env, jstring jstr)
{
	char*   rtn   =   NULL;
	jclass   clsstring   =   env->FindClass("java/lang/String");
	jstring   strencode   =   env->NewStringUTF("GB2312");
	jmethodID   mid   =   env->GetMethodID(clsstring,   "getBytes",   "(Ljava/lang/String;)[B");
	jbyteArray   barr=   (jbyteArray)env->CallObjectMethod(jstr,mid,strencode);
	jsize   alen   =   env->GetArrayLength(barr);
	jbyte*   ba   =   env->GetByteArrayElements(barr,JNI_FALSE);
    if(alen   >   0)
    {
        rtn   =   (char*)malloc(alen+1);
        memcpy(rtn,ba,alen);
        rtn[alen]=0;
    }
    env->ReleaseByteArrayElements(barr,ba,0);
    std::string stemp(rtn);
    free(rtn);
    return stemp;
}

JNIEXPORT jint JNICALL Java_tsinghua_stargate_io_FpgaDispatcherImpl_listDevice
(JNIEnv *env, jclass obj, jobject info)
{

	int r;
	int i;
	int size=-1;
	vector<FpgaInfoList> list;
	jclass cls;
	jstring name;
	jmethodID  setName;
	jmethodID  frequency;
	jmethodID  setNumChannels;
	jmethodID  setId;
	DeviceList(list);
	size=list.size();

	if(size<0)
	   return 1;
	for(i=0;i<size;i++){

		//name
		cls =env->FindClass("tsinghua/stargate/io/Fpga$FpgaInfo");
		setName =env->GetMethodID(cls,"setName","(ILjava/lang/String;)V");
		name = env->NewStringUTF(list.at(i).devName.c_str());
		env->CallVoidMethod(info, setName, i, name);
		//channel
		setNumChannels = env->GetMethodID(cls, "setNumChannels", "(II)V");
		env->CallVoidMethod(info, setNumChannels, i, list.at(i).numChnls);
		//id
		setId = env->GetMethodID(cls, "setId", "(II)V");
		env->CallVoidMethod(info, setId, i, list.at(i).id);
	}
	return 0;
}

JNIEXPORT jlong JNICALL Java_tsinghua_stargate_io_FpgaDispatcherImpl_openDevice
  (JNIEnv *env, jclass obj, jint id,jobject fdo)
{
	jclass cls;
	jmethodID jmethodType;

	ConfigInfo *config = DeviceOpen((int)id);
	cls=env->FindClass("tsinghua/stargate/io/ModuleConfig");
	jmethodType =env->GetMethodID(cls,"getType","()Ljava/lang/String;");

	jstring jstrType=static_cast<jstring>(env->CallObjectMethod(fdo,jmethodType));

	std::string strType=jstring2str(env,jstrType);

	if(strType.compare("kmeans")==0){

		jmethodID jmethod;
		cls=env->FindClass("tsinghua/stargate/io/KMeansConfig");
		jmethod =env->GetMethodID(cls,"getMinNclusters","()I");
		config->configType="kmeans";
		config->kmeansInfo.minNclusters=(jint)(env->CallIntMethod(fdo,jmethod));

		jmethod =env->GetMethodID(cls,"getMaxNclusters","()I");
		config->kmeansInfo.maxNclusters=(jint)(env->CallIntMethod(fdo,jmethod));

		jmethod =env->GetMethodID(cls,"getThreshold","()F");
		config->kmeansInfo.threshold=(jfloat)(env->CallFloatMethod(fdo,jmethod));

		jmethod =env->GetMethodID(cls,"getWriteOverTime","()I");
		config->writeOverTime=(jint)(env->CallIntMethod(fdo,jmethod));

		jmethod =env->GetMethodID(cls,"getReadOverTime","()I");
		config->readOverTime=(jint)(env->CallIntMethod(fdo,jmethod));

		jmethod =env->GetMethodID(cls,"getInFileName","()Ljava/lang/String;");
		jstring jstrInFilename=static_cast<jstring>(env->CallObjectMethod(fdo,jmethod));
		if(jstrInFilename)
			config->inputFileName=jstring2str(env,jstrInFilename);

		jmethod =env->GetMethodID(cls,"getOutFileName","()Ljava/lang/String;");
		jstring jstrOutFilename=static_cast<jstring>(env->CallObjectMethod(fdo,jmethod));
		if(jstrOutFilename)
			config->outputFileName=jstring2str(env,jstrOutFilename);

		jmethod =env->GetMethodID(cls,"getLine","()I");
		config->kmeansInfo.fileLine=(jint)(env->CallIntMethod(fdo,jmethod));

		jmethod =env->GetMethodID(cls,"getRow","()I");
		config->kmeansInfo.fileRow=(jint)(env->CallIntMethod(fdo,jmethod));

		jmethod =env->GetMethodID(cls,"getAccelerateBitPath","()Ljava/lang/String;");
		jstring jstrbitFilename=static_cast<jstring>(env->CallObjectMethod(fdo,jmethod));
		config->kmeansInfo.accelerateBitFile=jstring2str(env,jstrbitFilename);


		jmethod =env->GetMethodID(cls,"getInputType","()I");
		config->inputDyte=(jint)(env->CallIntMethod(fdo,jmethod));

		jmethod =env->GetMethodID(cls,"getOutputType","()I");
		config->outputDyte=(jint)(env->CallIntMethod(fdo,jmethod));

	}else if(strType.compare("riffa")==0){
		jmethodID jmethod;
		config->configType="riffa";

		cls=env->FindClass("tsinghua/stargate/io/RiffaConfig");
		jmethod =env->GetMethodID(cls,"getLast","()I");
		config->riffaInfo.last=(jint)(env->CallIntMethod(fdo,jmethod));

		jmethod =env->GetMethodID(cls,"getDestoff","()I");
		config->riffaInfo.destOff=(jint)(env->CallIntMethod(fdo,jmethod));


		jmethod =env->GetMethodID(cls,"getWriteOverTime","()I");
		config->writeOverTime=(jint)(env->CallIntMethod(fdo,jmethod));

		jmethod =env->GetMethodID(cls,"getReadOverTime","()I");
		config->readOverTime=(jint)(env->CallIntMethod(fdo,jmethod));


		jmethod =env->GetMethodID(cls,"getWriteSize","()I");
		config->inputDataLen=(jint)(env->CallIntMethod(fdo,jmethod));

		jmethod =env->GetMethodID(cls,"getReadSize","()I");
		config->outputDataLen=(jint)(env->CallIntMethod(fdo,jmethod));
	}
	DeviceConfig(id);
    return (jlong)config;
}

JNIEXPORT jint JNICALL Java_tsinghua_stargate_io_FpgaDispatcherImpl_writeIntoDevice
(JNIEnv *env, jclass obj, jobject fdo, jint channel, jlong address)
{
    void *buf = (void *)jlong_to_ptr(address);
    jlong fd = get_fd_val(env, fdo);
    return MemcpyHostToDevice((ConfigInfo *)fd,channel,buf);
}


JNIEXPORT jint JNICALL Java_tsinghua_stargate_io_FpgaDispatcherImpl_readFromDevice
  (JNIEnv *env, jclass obj, jobject fdo, jint channel,jlong address)
{
	int num;
    void *buf = (void *)jlong_to_ptr(address);
    jlong fd = get_fd_val(env, fdo);
    num=memcpyDeviceToHost((ConfigInfo *)fd,channel,buf);
    return num;
}

JNIEXPORT void JNICALL Java_tsinghua_stargate_io_FpgaDispatcherImpl_closeDevice
  (JNIEnv *env, jclass obj , jobject fdo)
{
    jlong fd = get_fd_val(env, fdo);
    DeviceClose((ConfigInfo *)fd);
}


JNIEXPORT void JNICALL Java_tsinghua_stargate_io_FpgaDispatcherImpl_reset0
  (JNIEnv *env, jclass obj, jobject fdo)
{
	jlong fd = get_fd_val(env, fdo);
	DeviceReset((ConfigInfo *)fd);
}













