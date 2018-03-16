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

#ifndef SRC_COMMON_H_
#define SRC_COMMON_H_

#include <vector>
#include <iostream>
#include <sstream>
#include <map>
#include <string>
#include <CL/opencl.h>
#include <boost/shared_ptr.hpp>

using std::vector;
using std::map;
using std::string;
using boost::shared_ptr;

enum REGISTRYSTATUS{
    REGISTRYNON,
    REGISTRYFINISH,
};

enum INOUTTYPE{
	FILETYPE,
	FLOATTYPE,
	INTTYPE
};

#define BLUE    "\033[34m"      /* Blue */
#define BLACK   "\033[30m"      /* Black */
#define RED     "\033[31m"      /* Red */
#define WHITE   "\033[37m"      /* White */

typedef struct FpgaInfoList_s
{
	int id;
	int numChnls;
	string devName;
	int frequency;
	int gloablMem;
} FpgaInfoList;


typedef struct RiffaInfo_s{
	int destOff;
	int last;
}RiffaInfo;

typedef struct KmeansInfo_s{
	int minNclusters;
	int maxNclusters;
	float threshold;
	int fileLine;
	int fileRow;
	string accelerateBitFile;
}KmeansInfo;

typedef struct ConfigInfo_s
{
	string configType;
	RiffaInfo  riffaInfo;
	KmeansInfo kmeansInfo;

	string inputFileName;
	string outputFileName;
	int inputDyte;
	int outputDyte;
	int inputDataLen;
	int outputDataLen;
	int writeOverTime;
	int readOverTime;
} ConfigInfo;

class AcceleratedDevice {
public:
	explicit AcceleratedDevice();
	virtual ~AcceleratedDevice();

	static bool CheckDevice(const int deviceId);
	unsigned int DeviceQuery();
	bool ChooseDevice(const int DeiceId);
	void DeviceList(vector<FpgaInfoList> &info);
	int hardwareNum;
protected:
	string m_mode;
	vector<cl_platform_id> m_PlatformId;
	vector<string> m_deviceName;
	cl_device_id m_deviceId;
	vector<int> m_deviceFrequency;
	vector<int> m_deviceGlobalMem;
	vector<int> m_hardwareId;

	cl_context m_context;
	cl_command_queue m_commandQueue;
private:
    int deviceOffNum;
    int registry;
};

#endif /* SRC_COMMON_H_ */
