#
# Copyright 2017 The Tsinghua University
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#

#!/bin/bash

# 查找是不是存在 Riffa 驱动
tmp=$(lsmod | grep riffa)

# 若驱动存在，卸载 riffa 驱动
if test -z "$tmp"
then
  echo "RIFFA DRIVER IS NULL"  
else  
  rmmod riffa
  echo "RIFFA DRIVER IS NOT NULL"  
fi

# Xilinx 总线设备
PCI_BUS_ID=$1

# 移除 PCI 总线上对应的设备
echo 1 > /sys/bus/pci/devices/$PCI_BUS_ID/remove

# 重新扫描所有 PCI 总线设备
echo 1 > /sys/bus/pci/rescan

# 自动加载所有 PCI 总线设备
setpci -s $PCI_BUS_ID 04.w=7