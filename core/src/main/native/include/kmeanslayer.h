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

#ifndef _KMEANS_H
#define _KMEANS_H

#include "common.h"

class KmeansLayer :public AcceleratedDevice{
public:
	explicit KmeansLayer();
	virtual  ~KmeansLayer();
	static KmeansLayer* m_kmeansLayer;
	static KmeansLayer* getInstance();
	string inputFileName;
	string outputFileName;
	int  minNclusters;
	int  maxNclusters;
	float threshold;
	int  globalSize;
	int  nloops;
protected:
	std::map<int,ConfigInfo *> m_configInfo;
public:
	int nfeatures;
	int npoints;
	float **cluster_centres;
	int *membership;
private:
	int isRMSE;
	float rmse;
	int bestNclusters;
    float **features;
public:
    ConfigInfo* DeviceOpen(int deviceId);
    int DeviceClose(ConfigInfo *config);
	void ConfigKmeansAcceleratedMoudle(int deviceId);
	int ConfigKmeansInputData(ConfigInfo *config,float *buff);
	int StaredKmeansAcceleratedModule(string bitFile);
	void StopKmeansAcceleratedModule();
};

ConfigInfo *DeviceOpen(int deviceId);
void DeviceReset(ConfigInfo *);
int DeviceConfig(int deviceId);
void DeviceClose(ConfigInfo *config);
int DeviceList(vector<FpgaInfoList> &info);
int MemcpyHostToDevice(ConfigInfo *,int channel,void *buff);
int memcpyDeviceToHost(ConfigInfo *,int chanenl,void *buff);
#endif
