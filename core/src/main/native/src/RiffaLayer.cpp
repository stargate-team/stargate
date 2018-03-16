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

#include "RiffaLayer.h"
#include <stdio.h>
#include <stdlib.h>
#include <sys/ioctl.h>
#include <unistd.h>
#include <fcntl.h>
#include "RiffaLayer.h"
#include <unistd.h>
#include <string.h>

RiffaLayer* RiffaLayer::m_riffaLayer;

RiffaLayer::RiffaLayer():
    fd(0),
    deviceOffNum(0),
    registry(REGISTRYNON)
{

}

RiffaLayer::~RiffaLayer() {
	delete m_riffaLayer;
}

RiffaLayer* RiffaLayer::getInstance()
{
    if(m_riffaLayer==NULL)
        m_riffaLayer=new RiffaLayer();
    return m_riffaLayer;
}

int RiffaLayer::DeviceList(std::vector<FpgaInfoList> &info)
{
    int rc;
    std::string nameString;
    std::string::size_type idx;
    this->fd = open("/dev/" DEVICE_NAME, O_RDWR | O_SYNC);
    if (this->fd < 0)
    {
    	deviceOffNum=0;
    	deviceNum=0;
        std::cout<<RED<<"RIFFA Device non exist"<<BLACK<<std::endl;
        return 0;
    }

    fpga_info_list list;
    rc = ioctl(this->fd,IOCTL_LIST,&list);
    if(rc<0)
        std::cout<<"Ioctl List Failure"<<std::endl;
    close(this->fd);

    if(this->registry==REGISTRYNON){
        this->registry=REGISTRYFINISH;
        this->deviceOffNum=info.size();

        int activateDevice= list.num_fpgas;
        deviceNum=activateDevice;
        std::cout<<"(Zc709 board Num:)"<<activateDevice<<std::endl;
        for(int i=0;i<activateDevice;i++)
        {
            FpgaInfoList mfpgaInfoList;
            mfpgaInfoList.devName=list.name[i];
            mfpgaInfoList.numChnls=list.num_chnls[i];
            mfpgaInfoList.id=list.id[i]+this->deviceOffNum;
            info.push_back(mfpgaInfoList);
            std::cout<<"RiffaLayer.cpp "<<"(devName :)"<<list.name[i]<<std::endl;
            std::cout<<"RiffaLayer.cpp "<<"(numChnls :)"<<list.num_chnls[i]<<std::endl;
            std::cout<<"RiffaLayer.cpp "<<"(id :)"<<list.id[i]+this->deviceOffNum<<std::endl;
        }
    }
    return 1;
}

int RiffaLayer::DeviceClose(ConfigInfo *config)
{
	close(this->fd);

	if(config!=NULL){
		return 0;
	}

	int id;
	auto find_item = std::find_if(m_configInfo.begin(), m_configInfo.end(), [config](const std::map<int,ConfigInfo *>::value_type item)
	{
		return item.second == config;
	});

	if (find_item!= m_configInfo.end())
	{
		id = (*find_item).first;
		m_configInfo.erase(find_item);
		if(config!=NULL)
			delete config;
	}
}

int RiffaLayer::DeviceWrite(ConfigInfo *config,int channel,void *buff)
{
	fpga_chnl_io io;
	int id;

	auto find_item = std::find_if(m_configInfo.begin(), m_configInfo.end(), [config](const std::map<int,ConfigInfo *>::value_type item)
	{
		return item.second == config;
	});

	if (find_item!= m_configInfo.end())
	{
		id = (*find_item).first;
	}
	io.id=id;
	io.chnl=channel;
	io.len =config->inputDataLen;
	io.offset=config->riffaInfo.destOff;
	io.last=config->riffaInfo.last;
	io.timeout=config->writeOverTime;
	io.data =(char *)buff;

	return ioctl(this->fd,IOCTL_SEND,&io);
}

int RiffaLayer::DeviceRead(ConfigInfo *config,int channel,void *buff)
{

	int id;
	int readSize;

	auto find_item = std::find_if(m_configInfo.begin(), m_configInfo.end(), [config](const std::map<int,ConfigInfo *>::value_type item)
	{
		return item.second == config;
	});

	if (find_item!= m_configInfo.end())
	{
		id = (*find_item).first;
	}
	fpga_chnl_io io;
	io.id = id;
	io.chnl = channel;
	io.len = config->outputDataLen;
	io.timeout = config->readOverTime;
	io.data = (char *)buff;
	return ioctl(this->fd, IOCTL_RECV,&io);
}

void RiffaLayer::DeviceReset(ConfigInfo *config)
{
	int id;

	auto find_item = std::find_if(m_configInfo.begin(), m_configInfo.end(), [config](const std::map<int,ConfigInfo *>::value_type item)
	{
		return item.second == config;
	});

	if (find_item!= m_configInfo.end())
	{
		id = (*find_item).first;
		ioctl(this->fd,IOCTL_RESET,id);
	}
}

ConfigInfo* RiffaLayer::DeviceOpen(int deviceId)
{
	this->fd = open("/dev/"DEVICE_NAME, O_RDWR | O_SYNC);
	if (this->fd < 0)
	{
		std::cout<<RED<<"RIFFA Device non exist"<<BLACK<<std::endl;
		return NULL;
	}

	int countNum;
	ConfigInfo *mcInfo;
	countNum=m_configInfo.count(deviceId-deviceOffNum);
	if(countNum==0){
		mcInfo=new ConfigInfo;
		m_configInfo.insert(std::pair<int,ConfigInfo *>((deviceId-deviceOffNum),mcInfo));
	}
	return m_configInfo[deviceId-deviceOffNum];
}

int RiffaLayer::ConfigAcceleratedMoudle(int deviceId){
    int countNum=0;
    countNum=m_configInfo.count(deviceId-deviceOffNum);

    if(countNum>0){
    	ConfigInfo *mconfigInfo=m_configInfo[deviceId];
    	this->destOff=mconfigInfo->riffaInfo.destOff;
    	this->last=mconfigInfo->riffaInfo.last;
    }
	return 1;
}








