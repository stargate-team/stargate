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

#!/usr/bin/env bash

BASE_PATH="$(cd "`dirname "$0"`"/; pwd)"

TCL_SOURCE_PATH=$BASE_PATH"/""vivado.tcl"

PCI_SLOT_1=210203A037A4A
PCI_SLOT_2=210203A03486A

BITSTREAM_PATH=$BASE_PATH"/"$2

PCI_BUS_ID_1="0000:01:00.0"
PCI_BUS_ID_2="0000:03:00.0"

if [ $1 = 0 ] 
then
    sh program-jtag.sh $TCL_SOURCE_PATH $PCI_SLOT_1 $BITSTREAM_PATH
    sudo sh load-driver.sh $PCI_BUS_ID_1
elif [ $1 = 1 ] 
then
    sh program-jtag.sh $TCL_SOURCE_PATH $PCI_SLOT_2 $BITSTREAM_PATH
    sudo sh load-driver.sh $PCI_BUS_ID_2
fi
