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

#include "common.h"
#include "stdio.h"
#include "stdlib.h"
#include "string.h"

AcceleratedDevice::AcceleratedDevice():
	deviceOffNum(0),
	hardwareNum(0),
	registry(REGISTRYNON)
{
	// TODO Auto-generated constructor stub
}

AcceleratedDevice::~AcceleratedDevice() {
	m_PlatformId.resize(0);
}

unsigned int AcceleratedDevice::DeviceQuery()
{
	bool status;
	cl_uint num_platforms;
	cl_uint deviceNum;
	int err;
	char *xcl_mode = getenv("XCL_EMULATION_MODE");
	char *xcl_target = getenv("XCL_TARGET");
	if(xcl_mode == NULL)
		m_mode ="hw";
	else {
		if(strcmp(xcl_mode,"true") == 0) {
			if(xcl_target == NULL)
				m_mode ="sw_emu";
			else
				m_mode = xcl_target;
		}
		else
			m_mode = xcl_mode;
		/* TODO: Remove once 2016.4 is released */
		err = setenv("XCL_EMULATION_MODE", "true", 1);
		if(err != 0) {
			std::cout<<"Error: cannot set XCL_EMULATION_MODE\n"<<std::endl;
			exit(EXIT_FAILURE);
		}
	}

	/*
	 * find opencl device already insert pcie-board
	*/

	FILE *ptr;
	char ps[1024]={0};
	char buf_ps[1024];
	sprintf(ps,"lspci | grep \'%s\'","Serial controller: Xilinx");
	if((ptr=popen(ps, "r"))!=NULL)
	{
		while(fgets(buf_ps, 1024, ptr)!=NULL)
		{

		}
		pclose(ptr);
	}
	std::string msg=buf_ps;
	string::size_type idx = msg.find("Serial controller: Xilinx");
	if(idx==string::npos)
		return 0;
	err = clGetPlatformIDs(0, NULL, &num_platforms);
	if (err != CL_SUCCESS) {
		std::cout<<"Error: no platforms available or OpenCL install broken"<<std::endl;
		return 0;
	}

	cl_platform_id *platform_ids = (cl_platform_id *) malloc(sizeof(cl_platform_id) * num_platforms);
	if (platform_ids == NULL) {
		std::cout<<"Error: Out of Memory"<<std::endl;
		return 0;
	}

	err = clGetPlatformIDs(num_platforms, platform_ids, NULL);
	if (err != CL_SUCCESS) {
		printf("Error: Failed to find an OpenCL platform!\n");
		return 0;
	}

	std::cout<<"OPENCL: (num_platforms):"<<num_platforms<<std::endl;

	size_t i;
	for(i = 0; i < num_platforms; i++) {
		size_t platform_name_size;
		err = clGetPlatformInfo(platform_ids[i], CL_PLATFORM_NAME,
		                        0, NULL, &platform_name_size);
		if( err != CL_SUCCESS) {
			printf("Error: Could not determine platform name!\n");
			return 0;
		}

		char *platform_name = (char*) malloc(sizeof(char)*platform_name_size);
		if(platform_name == NULL) {
			printf("Error: out of memory!\n");
			return 0;
		}

		err = clGetPlatformInfo(platform_ids[i], CL_PLATFORM_NAME,
		                        platform_name_size, platform_name, NULL);
		if(err != CL_SUCCESS) {
			printf("Error: could not determine platform name!\n");
			return 0;
		}


		std::cout<<"OPENCL: (platform_name):"<<platform_name<<std::endl;
		string vendorName="Xilinx";
		if(vendorName.compare(platform_name)==NULL){
			m_PlatformId.push_back(platform_ids[i]);
		}
		free(platform_name);
	}

	for(int i=0;i<m_PlatformId.size();i++)
	{
		int err;
		char *device_name;
		cl_uint frequency;
		cl_ulong GlobalSize;

		//Query Device Num  ---current method failure
		err = clGetDeviceIDs(m_PlatformId.at(i), CL_DEVICE_TYPE_ALL, 0, NULL, &deviceNum);
		if (err != CL_SUCCESS) {
			std::cout<<"Current platform don't exist OpenCL Device"<<std::endl;
			return 0;
		}
		std::cout<<"OPENCL: (DeviceNum):"<<deviceNum<<std::endl;


		for(int j=0;j<deviceNum;j++)
		{
			err = clGetDeviceIDs(m_PlatformId.at(i), CL_DEVICE_TYPE_ALL, 1, &m_deviceId, NULL);
			if (err != CL_SUCCESS) {
				std::cout<<"Error: could not get device ids"<<std::endl;
				exit(EXIT_FAILURE);
			}
			size_t device_name_size;
			err = clGetDeviceInfo(m_deviceId, CL_DEVICE_NAME,
								  0, NULL, &device_name_size);
			if(err != CL_SUCCESS) {
				std::cout<<"Error: could not determine device name"<<std::endl;
				exit(EXIT_FAILURE);
			}

			device_name = (char*) malloc(sizeof(char)*device_name_size);
			if(device_name == NULL) {
				std::cout<<"Error: Out of Memory!"<<std::endl;
				exit(EXIT_FAILURE);
			}

			err = clGetDeviceInfo(m_deviceId, CL_DEVICE_NAME,
								  device_name_size, device_name, NULL);
			if(err != CL_SUCCESS) {
				std::cout<<"Error: could not determine device name"<<std::endl;
				exit(EXIT_FAILURE);
			}

			err =clGetDeviceInfo(m_deviceId,CL_DEVICE_MAX_CLOCK_FREQUENCY,sizeof(cl_uint),&frequency,NULL);
			if(err != CL_SUCCESS){
				std::cout<<"Error: could not determine device frequency"<<std::endl;
				exit(EXIT_FAILURE);
			}

			err=clGetDeviceInfo(m_deviceId,CL_DEVICE_GLOBAL_MEM_SIZE,sizeof(cl_ulong),&GlobalSize,NULL);
			if(err != CL_SUCCESS){
				std::cout<<"Error: could not determine device global memsize"<<std::endl;
				exit(EXIT_FAILURE);
			}
			char deivceVersion[100];
			err=clGetDeviceInfo(m_deviceId,CL_DRIVER_VERSION,100,deivceVersion,NULL);
			if(err !=CL_SUCCESS)
			{
				std::cout<<"Error: could not determine device version"<<std::endl;
				exit(EXIT_FAILURE);
			}

			cl_uint vendorId;
			err=clGetDeviceInfo(m_deviceId,CL_DEVICE_VENDOR_ID,sizeof(cl_uint),&vendorId,NULL);
			if(err !=CL_SUCCESS)
			{
				std::cout<<"Error: could not determine device version"<<std::endl;
				exit(EXIT_FAILURE);
			}

			std::cout<<"OPENCL: (VendorId):"<<vendorId<<std::endl;
			std::cout<<"OPENCL: (Openclversion):"<<deivceVersion<<std::endl;
			std::cout<<"OPENCL:	(Global mem size):"<<(GlobalSize/1024/1024/1024)<<"(GB)"<<std::endl;
			std::cout<<"OPENCL:	(Device Frequency):"<<frequency<<"(MHz)"<<std::endl;
			std::cout<<"OPENCL:	(device name):"<<device_name<<std::endl;

			string stringName=device_name;
			m_deviceName.push_back(stringName);
			m_deviceFrequency.push_back(frequency);
			m_deviceGlobalMem.push_back(GlobalSize);
			m_hardwareId.push_back(vendorId);

			free(device_name);
		}
	}
	this->hardwareNum=deviceNum;
	return deviceNum;
}

bool AcceleratedDevice::ChooseDevice(const int DeiceId)
{
	bool status;
	int err;

	m_context = clCreateContext(0, DeiceId, &m_deviceId,NULL, NULL, &err);
	if (err != CL_SUCCESS) {
		std::cout<<"Error: Failed to create a compute context!"<<std::endl;
		exit(EXIT_FAILURE);
	}

	m_commandQueue = clCreateCommandQueue(m_context,
										  m_deviceId,
	                                      CL_QUEUE_PROFILING_ENABLE,
	                                      &err);
	if (err != CL_SUCCESS) {
		std::cout<<"Error: Failed to create a command queue!"<<std::endl;
		exit(EXIT_FAILURE);
	}

	return status;
}


void AcceleratedDevice::DeviceList(vector<FpgaInfoList> &info)
{
	if(this->registry==REGISTRYNON)
	{
		this->registry=REGISTRYFINISH;
		for(int i=0;i<hardwareNum;i++)
		{
			FpgaInfoList mfpgaInfoList;
			mfpgaInfoList.frequency	=m_deviceFrequency.at(i);
			mfpgaInfoList.devName	=m_deviceName.at(i);
			mfpgaInfoList.gloablMem =m_deviceGlobalMem.at(i);
			mfpgaInfoList.id        =m_hardwareId.at(i);
			mfpgaInfoList.numChnls  =1;
			info.push_back(mfpgaInfoList);
		}
	}
}




