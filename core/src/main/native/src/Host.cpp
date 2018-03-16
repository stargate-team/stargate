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

#define _CRT_SECURE_NO_DEPRECATE 1

#ifndef FLT_MAX
#define FLT_MAX 3.40282347e+38
#endif

#include <stdio.h>
#include <unistd.h>
#include <string.h>
#include <fcntl.h>
#include "kmeans.h"
#include "fpga_kmeans.h"
#include "cmdlineparser.h"
#include <iostream>
#include "fstream"
#include "cstring"
#include "fcntl.h"
#include "kmeanslayer.h"
#include "RiffaLayer.h"
#include "FpgaDispatcherImpl.h"

using namespace sda::utils;

KmeansLayer* KmeansLayer::m_kmeansLayer;

KmeansLayer::KmeansLayer():
	threshold(0.001),
	minNclusters(5),
	maxNclusters(5),
	globalSize(2),
	inputFileName("../data/100"),
	outputFileName("../data/100.gold_c5"),
	npoints(0),
	nfeatures(0),
	nloops(1),
	isRMSE(0),
	bestNclusters(0)
{

}
KmeansLayer::~KmeansLayer() {


}

KmeansLayer* KmeansLayer::getInstance()
{
	if(m_kmeansLayer==NULL)
		m_kmeansLayer=new KmeansLayer();
	return m_kmeansLayer;
}


void KmeansLayer::ConfigKmeansAcceleratedMoudle(int deviceId){
    int countNum=0;
    countNum=m_configInfo.count(deviceId);

    if(countNum>0){
    	ConfigInfo *mconfigInfo=m_configInfo[deviceId];
    	this->minNclusters		=mconfigInfo->kmeansInfo.minNclusters;
    	this->maxNclusters		=mconfigInfo->kmeansInfo.maxNclusters;
    	this->threshold   		=mconfigInfo->kmeansInfo.threshold;
    	this->inputFileName	 	=mconfigInfo->inputFileName;
    	this->outputFileName    =mconfigInfo->outputFileName;
    }
    fpga_kmeans_setup(globalSize);
}

int KmeansLayer::ConfigKmeansInputData(ConfigInfo *config,float *buff){
	if(config->inputDyte!=FILETYPE)
	{
		npoints=config->kmeansInfo.fileLine;
		nfeatures=config->kmeansInfo.fileRow;
		membership = (int*) malloc(npoints * sizeof(int));
		features    = (float**)malloc(npoints*          sizeof(float*));
		features[0] = (float*) malloc(npoints*nfeatures*sizeof(float));
	    for (int i=1; i<npoints; i++)
	        features[i] = features[i-1] + nfeatures;
	    memcpy(features[0], buff, npoints*nfeatures*sizeof(float));
	}
	else
	{
		char    line[1024];
		float   *buf;
	    int i,j;
	    FILE *infile;
	    if ((infile = fopen(inputFileName.c_str(), "r")) == NULL) {
	        std::cout<<"Error: no such file"<<inputFileName<<"std::endl";
	        exit(EXIT_FAILURE);
	    }
	    while (fgets(line, 1024, infile) != NULL)
	    if (strtok(line, " \t\n") != 0)
	            npoints++;
	    rewind(infile);
	    while (fgets(line, 1024, infile) != NULL) {
	        if (strtok(line, " \t\n") != 0) {
	            /* ignore the id (first attribute): nfeatures = 1; */
	            while (strtok(NULL, " ,\t\n") != NULL) nfeatures++;
	            break;
	        }
	    }

	    /* allocate space for features[] and read attributes of all objects */
	    buf         = (float*) malloc(npoints*nfeatures*sizeof(float));
	    features    = (float**)malloc(npoints*          sizeof(float*));
	    features[0] = (float*) malloc(npoints*nfeatures*sizeof(float));
	    for (i=1; i<npoints; i++)
	        features[i] = features[i-1] + nfeatures;
	    rewind(infile);
	    i = 0;
	    while (fgets(line, 1024, infile) != NULL) {
	        if (strtok(line, " \t\n") == NULL) continue;
	        for (j=0; j<nfeatures; j++) {
	            buf[i] = atof(strtok(NULL, " ,\t\n"));
	            i++;
	        }
	    }
	    fclose(infile);
	    memcpy(features[0], buf, npoints*nfeatures*sizeof(float)); /* now features holds 2-dimensional array of features */
	    free(buf);
	}
    return 1;
}

int KmeansLayer::StaredKmeansAcceleratedModule(string bitFile){
	int m_status;
	int index;

    cluster_centres = NULL;
    index = cluster(npoints,            /* number of data points */
                    nfeatures,          /* number of features for each point */
                    features,           /* array: [npoints][nfeatures] */
					membership,
					minNclusters,      /* range of min to max number of clusters */
					maxNclusters,
                    threshold,          /* loop termination factor */
                    &bestNclusters,    /* return: number between min and max */
                    &cluster_centres,   /* return: [best_nclusters][nfeatures] */
                    &rmse,              /* Root Mean Squared Error */
                    isRMSE,             /* calculate RMSE */
                    nloops,             /* number of iteration for each number of clusters */
					outputFileName.c_str(),
					bitFile.c_str());

    std::cout<<"I/O completed"<<std::endl;
    std::cout<<"fileName:"<<inputFileName<<std::endl;
    std::cout<<"Number of objects:"<<npoints<<std::endl;
    std::cout<<"Number of features:"<<nfeatures<<std::endl;
    std::cout<<"Number of min cluster:"<<minNclusters<<std::endl;
    std::cout<<"Number of max cluster:"<<maxNclusters<<std::endl;
    std::cout<<"threashold:"<<threshold<<std::endl;

	return m_status;
}

void KmeansLayer::StopKmeansAcceleratedModule(){

    free(features[0]);
    free(features);
    free(membership);
}

ConfigInfo* KmeansLayer::DeviceOpen(int deviceId)
{
	int countNum;
	ConfigInfo *mcInfo;
	countNum=m_configInfo.count(deviceId);
	if(countNum==0){
		mcInfo=new ConfigInfo;
		m_configInfo.insert(pair<int,ConfigInfo *>(deviceId,mcInfo));
	}
	return m_configInfo[deviceId];
}

int KmeansLayer::DeviceClose(ConfigInfo *config)
{
	int id;
	auto find_item = std::find_if(m_configInfo.begin(), m_configInfo.end(), [config](const std::map<int,ConfigInfo *>::value_type item)
	{
		return item.second == config;
	});

	if (find_item!= m_configInfo.end())
	{
		id = (*find_item).first;
		m_configInfo.erase(find_item);
		delete config;
	}
	return 1;
}
#ifdef TEST_APP
int main(int argc,char **argv){

	KmeansLayer* m_kmeansLayer=KmeansLayer::getInstance();
	m_kmeansLayer->DeviceQuery();
	//m_kmeansLayer->ChooseDevice(1);
	m_kmeansLayer->ConfigKmeansAcceleratedMoudle(0);
	m_kmeansLayer->StaredKmeansAcceleratedModule("/home/manager/data/software/stargate-opencl/src/main/native/org/apache/stargate/io/System");

	RiffaLayer * m_riffaLayer;
	m_riffaLayer = RiffaLayer::getInstance();
	vector<FpgaInfoList> list;
	m_riffaLayer->DeviceList(list);
	ConfigInfo *config=DeviceOpen(0);
	config->riffaInfo.last=1;
	config->riffaInfo.destOff=1;
	config->writeOverTime=2500;
	config->readOverTime=2500;
	config->inputDataLen=512;
	config->outputDataLen=512;

	int *txBuff=new int[512];
	int *rxBuff=new int[512];

	for(int i=0;i<512;i++){
		txBuff[i]=i;
	}

	int writeSize=m_riffaLayer->DeviceWrite(config,9,txBuff);
	int readSize=m_riffaLayer->DeviceRead(config,9,rxBuff);

	for(int i=0;i<512;i++){
		if(txBuff[i]!=rxBuff[i]){
			std::cout<<"index:"<<i<<" txBuff:"<<txBuff[i]<<" rxBuff:"<<rxBuff[i]<<std::endl;
		}
	}
	m_riffaLayer->DeviceClose(config);

	delete rxBuff;
	delete txBuff;
	return 1;
}
#endif

int DeviceList(vector<FpgaInfoList> &info)
{
	KmeansLayer* m_kmeansLayer;
	m_kmeansLayer= KmeansLayer::getInstance();
	m_kmeansLayer->DeviceQuery();
	m_kmeansLayer->DeviceList(info);

	RiffaLayer * m_riffaLayer;
	m_riffaLayer= RiffaLayer::getInstance();
	m_riffaLayer->DeviceList(info);
	return 1;
}

ConfigInfo *DeviceOpen(int deviceId)
{
	ConfigInfo *configInfo;
	KmeansLayer* m_kmeansLayer;
	RiffaLayer * m_riffaLayer;
	m_kmeansLayer= KmeansLayer::getInstance();
	m_riffaLayer = RiffaLayer::getInstance();

	if(deviceId<m_kmeansLayer->hardwareNum){
		configInfo=m_kmeansLayer->DeviceOpen(deviceId);
	}
	else{
		configInfo=m_riffaLayer->DeviceOpen(deviceId);
	}
	return configInfo;
}

int DeviceConfig(int deviceId)
{
	KmeansLayer* m_kmeansLayer;
	RiffaLayer * m_riffaLayer;
	m_kmeansLayer= KmeansLayer::getInstance();
	m_riffaLayer = RiffaLayer::getInstance();
	if(deviceId<m_kmeansLayer->hardwareNum)
		m_kmeansLayer->ConfigKmeansAcceleratedMoudle(deviceId);
	else
		m_riffaLayer->ConfigAcceleratedMoudle(deviceId);
	return 1;
}

//template <typename Dtype>
int MemcpyHostToDevice(ConfigInfo *config,int channel,void *buff)
{
	int writeSize;
	KmeansLayer* m_kmeansLayer;
	RiffaLayer * m_riffaLayer;
	if(config->configType.compare("kmeans")==0){
		float *data;
		data=(float*)buff;
		m_kmeansLayer= KmeansLayer::getInstance();
		m_kmeansLayer->ConfigKmeansInputData(config,(float*)buff);
		m_kmeansLayer->StaredKmeansAcceleratedModule(config->kmeansInfo.accelerateBitFile);
        writeSize=config->kmeansInfo.fileLine*config->kmeansInfo.fileRow*sizeof(float);
	}
	if(config->configType.compare("riffa")==0){
		m_riffaLayer = RiffaLayer::getInstance();
		writeSize=m_riffaLayer->DeviceWrite(config,channel,buff);
	}
	return writeSize;
}

int memcpyDeviceToHost(ConfigInfo *config,int channel,void *buff)
{
	KmeansLayer* m_kmeansLayer;
	RiffaLayer * m_riffaLayer;

	int readSize=0;

	if(config->configType.compare("kmeans")==0){
		float *data;
		data=(float*)buff;
		m_kmeansLayer= KmeansLayer::getInstance();

		int offset_lable=1;
		int offset_lable_data=m_kmeansLayer->npoints;
		int index_lable=(offset_lable+offset_lable_data)*sizeof(float);
		data[0]=index_lable;

		for(int i=0;i<100;i++)
			data[1+i]=m_kmeansLayer->membership[i];

		int offset_Coord=1;
		int offset_Coord_data=m_kmeansLayer->minNclusters*m_kmeansLayer->nfeatures;
		int index_Coord=(offset_Coord+offset_Coord_data)*sizeof(float);
		data[1+m_kmeansLayer->npoints]=index_Coord;

		for(int i=0;i<m_kmeansLayer->minNclusters;i++)
			memcpy(1+m_kmeansLayer->npoints+1+data+i*m_kmeansLayer->nfeatures, m_kmeansLayer->cluster_centres[i],m_kmeansLayer->nfeatures*sizeof(float));

		readSize=(1+m_kmeansLayer->npoints+1+m_kmeansLayer->minNclusters*m_kmeansLayer->nfeatures)*sizeof(float);
	}

	if(config->configType.compare("riffa")==0){
		m_riffaLayer = RiffaLayer::getInstance();
		readSize=m_riffaLayer->DeviceRead(config,channel,buff);
	}
	return readSize;
}

void DeviceReset(ConfigInfo *config)
{
	RiffaLayer * m_riffaLayer;
	KmeansLayer* m_kmeansLayer;

	if(config->configType.compare("kmeans")==0){

	}

	if(config->configType.compare("riffa")==0){
		m_riffaLayer = RiffaLayer::getInstance();
		m_riffaLayer->DeviceReset(config);
	}
}

void DeviceClose(ConfigInfo *config)
{
	KmeansLayer* m_kmeansLayer;
	RiffaLayer * m_riffaLayer;

	if(config->configType.compare("kmeans")==0){
		m_kmeansLayer= KmeansLayer::getInstance();
		m_kmeansLayer->StopKmeansAcceleratedModule();
		m_kmeansLayer->DeviceClose(config);
	}

	if(config->configType.compare("riffa")==0){
		m_riffaLayer = RiffaLayer::getInstance();
		m_riffaLayer->DeviceClose(config);
	}
}


