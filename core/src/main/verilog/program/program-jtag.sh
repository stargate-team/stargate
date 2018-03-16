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

export VIVADO_HOME=~/software/Xilinx/Vivado/2015.4
export PATH=$PATH:$VIVADO_HOME/bin

TCL_SOURCE_PARH=$1

PCI_SLOT_ID=$2

BITSTREAM_PATH=$3

vivado -nolog -nojournal -mode tcl -source $TCL_SOURCE_PARH -tclargs $PCI_SLOT_ID $BITSTREAM_PATH

rm -rf *jou
rm -rf *log
rm -rf .Xil