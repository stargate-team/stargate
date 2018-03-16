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

# compiler tools
XILINX_VIVADO_HLS ?= $(XILINX_SDX)/Vivado_HLS

SDX_CXX ?= $(XILINX_SDX)/bin/xcpp -fPIC -shared
XOCC ?= $(XILINX_SDX)/bin/xocc
EMCONFIGUTIL = $(XILINX_SDX)/bin/emconfigutil --od .
RM = rm -f
RMDIR = rm -rf

SDX_PLATFORM = xilinx_kcu1500_4ddr-xpr_4_0

# host compiler global settings
CXXFLAGS += -DRECORD_OVERALL_TIME -DUSE_DATA_TYPE=1 -DPARALLEL_POINTS=4 -DSDX_PLATFORM=$(SDX_PLATFORM) -D__USE_XOPEN2K8 -I./lib/logger/ -I./lib/cmdparser/ -I./lib/oclHelper/ -I./lib/xcl/ -I$(XILINX_SDX)/runtime/include/1_2/ -I$(XILINX_SDX)/Vivado_HLS/include/ -I./include/ -I$(JAVA_HOME)/include/ -I$(JAVA_HOME)/include/linux -O0 -g -Wall -c -fmessage-length=0 -std=c++14
LDFLAGS += -lxilinxopencl -lpthread -lrt -lstdc++ -L$(XILINX_SDX)/runtime/lib/x86_64

#
# OpenCL kernel files
#

BINARY_CONTAINERS += out/kmeans.xclbin

BUILD_SUBDIRS += out/kmeans
KMEANS_OBJS += out/kmeans/kmeans.xo
ALL_KERNEL_OBJS += out/kmeans/kmeans.xo

ALL_MESSAGE_FILES = $(subst .xo,.mdb,$(ALL_KERNEL_OBJS)) $(subst .xclbin,.mdb,$(BINARY_CONTAINERS))

#
# host files
#

HOST_OBJECTS += out/lib/cmdparser/cmdlineparser.o
HOST_OBJECTS += out/lib/logger/logger.o
HOST_OBJECTS += out/lib/oclHelper/oclErrorCodes.o
HOST_OBJECTS += out/lib/oclHelper/oclHelper.o
HOST_OBJECTS += out/lib/xcl/xcl.o
HOST_OBJECTS += out/src/Common.o
HOST_OBJECTS += out/src/FpgaDispatcherImpl.o
HOST_OBJECTS += out/src/Fpga_kmeans.o
HOST_OBJECTS += out/src/Host.o
HOST_OBJECTS += out/src/IOUtils.o
HOST_OBJECTS += out/src/NativeThread.o
HOST_OBJECTS += out/src/RiffaLayer.o
HOST_OBJECTS += out/src/cluster.o
HOST_OBJECTS += out/src/kmeans_clustering_cmodel.o
HOST_OBJECTS += out/src/rmse.o

HOST_EXE = out/libio.so

BUILD_SUBDIRS += out/lib/cmdparser/
BUILD_SUBDIRS += out/lib/logger/
BUILD_SUBDIRS += out/lib/oclHelper/
BUILD_SUBDIRS += out/lib/xcl/
BUILD_SUBDIRS += out/src/

EMCONFIG_FILE = out/emconfig.json

TARGETS:=sw_emu
# kernel compiler global settings
XOCC_OPTS = -t $(TARGETS) --platform $(SDX_PLATFORM) --save-temps

#
# primary build targets
#

.PHONY: all jni setup_riffa riffa install_riffa load_riffa clean clean_opencl clean_riffa unload unload_riffa uninstall uninstall_riffa remove
all: 	$(BINARY_CONTAINERS) $(HOST_EXE) $(EMCONFIG_FILE) setup_riffa riffa install_riffa load_riffa jni

jni:
	cd ../java/ && \
	javah -jni -o ../native/include/FpgaDispatcherImpl.h  tsinghua.stargate.io.FpgaDispatcherImpl && \
	javah -jni -o ../native/include/IOUtils.h   tsinghua.stargate.io.IOUtils  && \
	javah -jni -o ../native/include/NativeThread.h tsinghua.stargate.io.NativeThread

setup_riffa:
	sudo make setup -C ./driver/linux

riffa:
	make -C ./driver/linux

install_riffa:
	sudo make install -C ./driver/linux

load_riffa:
	sudo make load -C ./driver/linux

clean: clean_opencl clean_riffa

clean_opencl:
	-$(RMDIR) out
    
clean_riffa:
	make clean -C ./driver/linux

unload: unload_riffa

unload_riffa:
	sudo make unload -C ./driver/linux

uninstall: uninstall_riffa

uninstall_riffa:
	sudo make uninstall -C ./driver/linux

remove: unload uninstall clean

.PHONY: incremental
incremental: all

include MakeEmConfig.mk

#
# binary container: kmeans.xclbin
#

out/kmeans/kmeans.xo: ./src/kmeans.cl
	@mkdir -p $(@D)
	-@$(RM) $@
	$(XOCC) $(XOCC_OPTS) -c -k kmeans -g --messageDb $(subst .xo,.mdb,$@) -I"$(<D)" --xp misc:solution_name=out/_xocc_compile_kmeans_kmeans -D PARALLEL_POINTS=4 -D PARALLEL_FEATURES=2 -D USE_DATA_TYPE=1 -o"$@" "$<"
	-@$(RMDIR) .Xil

out/kmeans.xclbin: $(KMEANS_OBJS)
	$(XOCC) $(XOCC_OPTS) -l --nk kmeans:2 -g --messageDb $(subst .xclbin,.mdb,$@) --xp misc:solution_name=out/_xocc_link_kmeans -o"$@" $(+)
	-@$(RMDIR) .Xil

#
# host rules
#

out/lib/cmdparser/cmdlineparser.o: ./lib/cmdparser/cmdlineparser.cpp ./lib/cmdparser/cmdlineparser.h ./lib/logger/logger.h
	@mkdir -p $(@D)
	$(SDX_CXX) $(CXXFLAGS) -DRECORD_OVERALL_TIME -DUSE_DATA_TYPE=1 -DPARALLEL_POINTS=4 -DSDX_PLATFORM=$(SDX_PLATFORM) -D__USE_XOPEN2K8 -I./lib/logger/ -I./lib/cmdparser/ -I./lib/oclHelper/ -I./lib/xcl/ -I$(XILINX_SDX)/runtime/include/1_2/ -I$(XILINX_SDX)/Vivado_HLS/include/ -I./include/ -I$(JAVA_HOME)/include/ -I$(JAVA_HOME)/include/linux -O0 -g -Wall -c -fmessage-length=0 -o "$@" "$<"

out/lib/logger/logger.o: ./lib/logger/logger.cpp ./lib/logger/logger.h
	@mkdir -p $(@D)
	$(SDX_CXX) $(CXXFLAGS) -DRECORD_OVERALL_TIME -DUSE_DATA_TYPE=1 -DPARALLEL_POINTS=4 -DSDX_PLATFORM=$(SDX_PLATFORM) -D__USE_XOPEN2K8 -I./lib/logger/ -I./lib/cmdparser/ -I./lib/oclHelper/ -I./lib/xcl/ -I$(XILINX_SDX)/runtime/include/1_2/ -I$(XILINX_SDX)/Vivado_HLS/include/ -I./include/ -I$(JAVA_HOME)/include/ -I$(JAVA_HOME)/include/linux -O0 -g -Wall -c -fmessage-length=0 -o "$@" "$<"

out/lib/oclHelper/oclHelper.o: ./lib/oclHelper/oclHelper.cpp ./lib/oclHelper/oclHelper.h
	@mkdir -p $(@D)
	$(SDX_CXX) $(CXXFLAGS) -DRECORD_OVERALL_TIME -DUSE_DATA_TYPE=1 -DPARALLEL_POINTS=4 -DSDX_PLATFORM=$(SDX_PLATFORM) -D__USE_XOPEN2K8 -I./lib/logger/ -I./lib/cmdparser/ -I./lib/oclHelper/ -I./lib/xcl/ -I$(XILINX_SDX)/runtime/include/1_2/ -I$(XILINX_SDX)/Vivado_HLS/include/ -I./include/ -I$(JAVA_HOME)/include/ -I$(JAVA_HOME)/include/linux -O0 -g -Wall -c -fmessage-length=0 -o "$@" "$<"

out/lib/xcl/xcl.o: ./lib/xcl/xcl.c ./lib/xcl/xcl.h
	@mkdir -p $(@D)
	$(SDX_CXX) $(CXXFLAGS) -DRECORD_OVERALL_TIME -DUSE_DATA_TYPE=1 -DPARALLEL_POINTS=4 -DSDX_PLATFORM=$(SDX_PLATFORM) -D__USE_XOPEN2K8 -I./lib/logger/ -I./lib/cmdparser/ -I./lib/oclHelper/ -I./lib/xcl/ -I$(XILINX_SDX)/runtime/include/1_2/ -I$(XILINX_SDX)/Vivado_HLS/include/ -I./include/ -I$(JAVA_HOME)/include/ -I$(JAVA_HOME)/include/linux -O0 -g -Wall -c -fmessage-length=0 -o "$@" "$<"

out/src/Common.o: ./src/Common.cpp ./include/common.h /usr/include/stdio.h /usr/include/stdlib.h /usr/include/string.h
	@mkdir -p $(@D)
	$(SDX_CXX) $(CXXFLAGS) -DRECORD_OVERALL_TIME -DUSE_DATA_TYPE=1 -DPARALLEL_POINTS=4 -DSDX_PLATFORM=$(SDX_PLATFORM) -D__USE_XOPEN2K8 -I./lib/logger/ -I./lib/cmdparser/ -I./lib/oclHelper/ -I./lib/xcl/ -I$(XILINX_SDX)/runtime/include/1_2/ -I$(XILINX_SDX)/Vivado_HLS/include/ -I./include/ -I$(JAVA_HOME)/include/ -I$(JAVA_HOME)/include/linux -O0 -g -Wall -c -fmessage-length=0 -o "$@" "$<"

out/src/FpgaDispatcherImpl.o: ./src/FpgaDispatcherImpl.cpp ./include/FpgaDispatcherImpl.h ./include/common.h ./include/kmeanslayer.h /usr/include/string.h ./include/pci_util.h
	@mkdir -p $(@D)
	$(SDX_CXX) $(CXXFLAGS) -DRECORD_OVERALL_TIME -DUSE_DATA_TYPE=1 -DPARALLEL_POINTS=4 -DSDX_PLATFORM=$(SDX_PLATFORM) -D__USE_XOPEN2K8 -I./lib/logger/ -I./lib/cmdparser/ -I./lib/oclHelper/ -I./lib/xcl/ -I$(XILINX_SDX)/runtime/include/1_2/ -I$(XILINX_SDX)/Vivado_HLS/include/ -I./include/ -I$(JAVA_HOME)/include/ -I$(JAVA_HOME)/include/linux -O0 -g -Wall -c -fmessage-length=0 -o "$@" "$<"

out/src/Fpga_kmeans.o: ./src/Fpga_kmeans.cpp ./include/fpga_kmeans.h ./include/kmeans.h ./lib/xcl/xcl.h ./lib/oclHelper/oclHelper.h
	@mkdir -p $(@D)
	$(SDX_CXX) $(CXXFLAGS) -DRECORD_OVERALL_TIME -DUSE_DATA_TYPE=1 -DPARALLEL_POINTS=4 -DSDX_PLATFORM=$(SDX_PLATFORM) -D__USE_XOPEN2K8 -I./lib/logger/ -I./lib/cmdparser/ -I./lib/oclHelper/ -I./lib/xcl/ -I$(XILINX_SDX)/runtime/include/1_2/ -I$(XILINX_SDX)/Vivado_HLS/include/ -I./include/ -I$(JAVA_HOME)/include/ -I$(JAVA_HOME)/include/linux -O0 -g -Wall -c -fmessage-length=0 -o "$@" "$<"

out/src/Host.o: ./src/Host.cpp ./include/kmeans.h ./include/fpga_kmeans.h ./lib/cmdparser/cmdlineparser.h /usr/include/c++/5/fstream /usr/include/c++/5/cstring /usr/include/fcntl.h ./include/kmeanslayer.h ./include/RiffaLayer.h ./include/FpgaDispatcherImpl.h
	@mkdir -p $(@D)
	$(SDX_CXX) $(CXXFLAGS) -DRECORD_OVERALL_TIME -DUSE_DATA_TYPE=1 -DPARALLEL_POINTS=4 -DSDX_PLATFORM=$(SDX_PLATFORM) -D__USE_XOPEN2K8 -I./lib/logger/ -I./lib/cmdparser/ -I./lib/oclHelper/ -I./lib/xcl/ -I$(XILINX_SDX)/runtime/include/1_2/ -I$(XILINX_SDX)/Vivado_HLS/include/ -I./include/ -I$(JAVA_HOME)/include/ -I$(JAVA_HOME)/include/linux -O0 -g -Wall -c -fmessage-length=0 -o "$@" "$<"

out/src/IOUtils.o: ./src/IOUtils.cpp ./include/IOUtils.h
	@mkdir -p $(@D)
	$(SDX_CXX) $(CXXFLAGS) -DRECORD_OVERALL_TIME -DUSE_DATA_TYPE=1 -DPARALLEL_POINTS=4 -DSDX_PLATFORM=$(SDX_PLATFORM) -D__USE_XOPEN2K8 -I./lib/logger/ -I./lib/cmdparser/ -I./lib/oclHelper/ -I./lib/xcl/ -I$(XILINX_SDX)/runtime/include/1_2/ -I$(XILINX_SDX)/Vivado_HLS/include/ -I./include/ -I$(JAVA_HOME)/include/ -I$(JAVA_HOME)/include/linux -O0 -g -Wall -c -fmessage-length=0 -o "$@" "$<"

out/src/NativeThread.o: ./src/NativeThread.cpp ./include/NativeThread.h
	@mkdir -p $(@D)
	$(SDX_CXX) $(CXXFLAGS) -DRECORD_OVERALL_TIME -DUSE_DATA_TYPE=1 -DPARALLEL_POINTS=4 -DSDX_PLATFORM=$(SDX_PLATFORM) -D__USE_XOPEN2K8 -I./lib/logger/ -I./lib/cmdparser/ -I./lib/oclHelper/ -I./lib/xcl/ -I$(XILINX_SDX)/runtime/include/1_2/ -I$(XILINX_SDX)/Vivado_HLS/include/ -I./include/ -I$(JAVA_HOME)/include/ -I$(JAVA_HOME)/include/linux -O0 -g -Wall -c -fmessage-length=0 -o "$@" "$<"

out/src/RiffaLayer.o: ./src/RiffaLayer.cpp ./include/RiffaLayer.h ./include/RiffaLayer.h
	@mkdir -p $(@D)
	$(SDX_CXX) $(CXXFLAGS) -DRECORD_OVERALL_TIME -DUSE_DATA_TYPE=1 -DPARALLEL_POINTS=4 -DSDX_PLATFORM=$(SDX_PLATFORM) -D__USE_XOPEN2K8 -I./lib/logger/ -I./lib/cmdparser/ -I./lib/oclHelper/ -I./lib/xcl/ -I$(XILINX_SDX)/runtime/include/1_2/ -I$(XILINX_SDX)/Vivado_HLS/include/ -I./include/ -I$(JAVA_HOME)/include/ -I$(JAVA_HOME)/include/linux -O0 -g -Wall -c -fmessage-length=0 -o "$@" "$<"

out/src/cluster.o: ./src/cluster.c ./include/kmeans.h ./include/fpga_kmeans.h
	@mkdir -p $(@D)
	$(SDX_CXX) $(CXXFLAGS) -DRECORD_OVERALL_TIME -DUSE_DATA_TYPE=1 -DPARALLEL_POINTS=4 -DSDX_PLATFORM=$(SDX_PLATFORM) -D__USE_XOPEN2K8 -I./lib/logger/ -I./lib/cmdparser/ -I./lib/oclHelper/ -I./lib/xcl/ -I$(XILINX_SDX)/runtime/include/1_2/ -I$(XILINX_SDX)/Vivado_HLS/include/ -I./include/ -I$(JAVA_HOME)/include/ -I$(JAVA_HOME)/include/linux -O0 -g -Wall -c -fmessage-length=0 -o "$@" "$<"

out/src/kmeans_clustering_cmodel.o: ./src/kmeans_clustering_cmodel.c ./include/kmeans.h
	@mkdir -p $(@D)
	$(SDX_CXX) $(CXXFLAGS) -DRECORD_OVERALL_TIME -DUSE_DATA_TYPE=1 -DPARALLEL_POINTS=4 -DSDX_PLATFORM=$(SDX_PLATFORM) -D__USE_XOPEN2K8 -I./lib/logger/ -I./lib/cmdparser/ -I./lib/oclHelper/ -I./lib/xcl/ -I$(XILINX_SDX)/runtime/include/1_2/ -I$(XILINX_SDX)/Vivado_HLS/include/ -I./include/ -I$(JAVA_HOME)/include/ -I$(JAVA_HOME)/include/linux -O0 -g -Wall -c -fmessage-length=0 -o "$@" "$<"

out/src/rmse.o: ./src/rmse.c ./include/kmeans.h
	@mkdir -p $(@D)
	$(SDX_CXX) $(CXXFLAGS) -DRECORD_OVERALL_TIME -DUSE_DATA_TYPE=1 -DPARALLEL_POINTS=4 -DSDX_PLATFORM=$(SDX_PLATFORM) -D__USE_XOPEN2K8 -I./lib/logger/ -I./lib/cmdparser/ -I./lib/oclHelper/ -I./lib/xcl/ -I$(XILINX_SDX)/runtime/include/1_2/ -I$(XILINX_SDX)/Vivado_HLS/include/ -I./include/ -I$(JAVA_HOME)/include/ -I$(JAVA_HOME)/include/linux -O0 -g -Wall -c -fmessage-length=0 -o "$@" "$<"

out/lib/oclHelper/%.o: ./lib/oclHelper/%.cpp
	@mkdir -p $(@D)
	$(SDX_CXX) $(CXXFLAGS) -DRECORD_OVERALL_TIME -DUSE_DATA_TYPE=1 -DPARALLEL_POINTS=4 -DSDX_PLATFORM=$(SDX_PLATFORM) -D__USE_XOPEN2K8 -I./lib/logger/ -I./lib/cmdparser/ -I./lib/oclHelper/ -I./lib/xcl/ -I$(XILINX_SDX)/runtime/include/1_2/ -I$(XILINX_SDX)/Vivado_HLS/include/ -I./include/ -I$(JAVA_HOME)/include/ -I$(JAVA_HOME)/include/linux -O0 -g -Wall -c -fmessage-length=0 -o "$@" "$<"

$(HOST_EXE): $(HOST_OBJECTS)
	$(SDX_CXX) -o "$@" $(+) $(LDFLAGS) -lxilinxopencl -lpthread -lrt -lstdc++ -L$(XILINX_SDX)/runtime/lib/x86_64

#
# emulation configuration file
#

$(EMCONFIG_FILE): MakeEmConfig.mk
	$(EMCONFIGUTIL) --od out/ --nd $(NUMBER_OF_DEVICES)  --platform $(SDX_PLATFORM)
	-@$(RMDIR) TempConfig .Xil

