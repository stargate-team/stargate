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

#ifndef __RIFFALAYER_H
#define __RIFFALAYER_H

#include "common.h"
#define NUM_FPGAS	5
#define MAJOR_NUM 100
#define DEVICE_NAME "riffa"
#define VENDOR_ID0 0x10EE
#define VENDOR_ID1 0x1172

struct fpga_chnl_io
{
    int id;
    int chnl;
    unsigned int len;
    unsigned int offset;
    unsigned int last;
    unsigned long long timeout;
    char * data;
};
typedef struct fpga_chnl_io fpga_chnl_io;

struct fpga_info_list
{
    int num_fpgas;
    int id[NUM_FPGAS];
    int num_chnls[NUM_FPGAS];
    char name[NUM_FPGAS][16];
    int vendor_id[NUM_FPGAS];
    int device_id[NUM_FPGAS];
};
typedef struct fpga_info_list fpga_info_list;


#define IOCTL_SEND _IOW(MAJOR_NUM, 1, fpga_chnl_io *)
#define IOCTL_RECV _IOR(MAJOR_NUM, 2, fpga_chnl_io *)
#define IOCTL_LIST _IOR(MAJOR_NUM, 3, fpga_info_list *)
#define IOCTL_RESET _IOW(MAJOR_NUM, 4, int)

class RiffaLayer{

public:
    explicit RiffaLayer();
    virtual ~RiffaLayer();
    static RiffaLayer* m_riffaLayer;
    static RiffaLayer* getInstance();
    int  DeviceList(std::vector<FpgaInfoList> &info);
protected:
    int deviceNum;
public:
    int fd;
    int deviceOffNum;
    int registry;
    int DeviceRead(ConfigInfo *config,int channel,void *buff);
    int DeviceWrite(ConfigInfo *config,int channel,void *buff);
    int DeviceClose(ConfigInfo *config);
    ConfigInfo *DeviceOpen(int deviceId);
    void DeviceReset(ConfigInfo *config);
    int ConfigAcceleratedMoudle(int deviceId);
protected:
    std::map<int,ConfigInfo *> m_configInfo;
private:
    int destOff;
	int last;
};

#endif
























