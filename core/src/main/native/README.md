K-Means
======================

## 1. OVERVIEW
This is OpenCL Based K-Means clustering Implementation for Xilinx FPGA 
Devices. K-means clustering is a method of vector quantization, that 
is popular for cluster analysis in data mining. K-means clustering 
aims to partition n observations into k clusters in which each 
observation belongs to the cluster with the nearest mean, serving as
a prototype of the cluster.

The application sets two compute units by default so that it will fit 
all Xilinx Devices. For bigger Xilinx Devices, user can increase the 
number of Compute units in Makefile and can get better performance.
It is tested upto 6 compute units for ku115 device and got 
approximately 6x improvement with respect to single compute units, which 
shows that application is more compute bound (not memory bound) and 
performance is directly proportional to number of compute units.

## 2. HOW TO DOWNLOAD THE SOFTWARE
Log on to the following website for sdaccel software.
```
https://china.xilinx.com/support/download/index.html/content/xilinx/zh/downloadNav/sdx-development-environments.html    
```
## 3. COMPILATION AND EXECUTION
### Compiling for Application Emulation
As part of the capabilities available to an application developer, SDAccel includes environments to test the correctness of an application at both a software functional level and a hardware emulated level.
These modes, which are named sw_emu, allow the developer to profile and evaluate the performance of a design before compiling for board execution.
It is recommended that all applications are executed in at least the sw_emu mode before being compiled and executed on an FPGA board.

make TARGETS=<sw_emu> all

##3. COMPILATION AND EXECUTION
### Compiling for Reality Board
make TARGETS=<hw> all

##4. ENVIRONMENT VARIABLE

export LD_LIBRARY_PATH=$XILINX_SDX/runtime/lib/x86_64/:$LD_LIBRARY_PATH

export XILINX_SDX=/opt/Xilinx/SDx/2017.2

export JAVA_HOME=/home/manager/data/software/java/java

