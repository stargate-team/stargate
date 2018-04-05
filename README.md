# StarGate

[![License](https://img.shields.io/badge/License-Apache%202.0-brightgreen.svg)](https://opensource.org/licenses/Apache-2.0)

StarGate is a programming and runtime framework for enabing easy and efficient deployment of various accerators.

## Prerequisites

> * FPGA: Xilinx-VC709 (using Riffa Driver + Verilog) or Xilinx-KCU1500 (using OpenCL)
> * OS: Ubuntu14.04 (kernel: 4.4.0-81-generic, target for Xilinx-VC709) or Ubuntu16.04 (kernel: 4.10.0-42-generic (4.13 is not available), target for Xilinx-VC709 or Xilinx-KCU1500) 
> * Vivado: 2017.2 (64-bit)
> * JDK: >= 1.8
> * Scala: 2.10
> * Thrift: 0.9.3
> * Hadoop: 2.6.0
> * Spark: 1.6

## Building StarGate

StarGate is mainly built by using Apache Maven. Building StarGate requires Maven 3.3.9 or newer and Java 8+.

    
### build/build

StarGate now comes packaged with a self-contained Maven installation to ease building and deployment of StarGate from source located under the build/ directory. This script will automatically download and setup all necessary build requirements (Maven, Scala, Zinc and Thrift) locally within the build/ directory itself. It honors any mvn binary if present already, however, will pull down its own copy of Scala and Zinc regardless to ensure proper version requirements are met. build/build execution acts as a pass through to the mvn call allowing easy transition from previous build methods. As an example, one can build a version of StarGate as follows:

    ./build/build -DskipTests clean package

### Building with Riffa (PCIe driver) support

    ./build/build -Priffa -DskipTests clean package

### Building with Xilinx FPGA (software bitstream) support

    ./build/build -Pnative -DskipTests clean package

### Building with Xilinx FPGA (hardware bitstream) support

    ./build/build -Phardware -DskipTests clean package

## Running StarGate Application

A StarGate application can be a regular java application or a Spark application (see our examples). If you want to run your own StarGate application (Java and Spark-local type) on a node paired with FPGA accerators (e.g., Xilinx-VC709 or Xilinx-KCU1500), you should start a StarGate server JVM on that node and then start StarGate client to submit your application. On the other hand, if you want to run StarGate application in a cluster (e.g., Spark on YARN mode), you should configure Hadoop YARN capacity-scheduler.xml to support label scheduling, start StarGate servers on those nodes paired with FPGA accerators and then start StarGate client which uses typical spark-submit to submit your StarGate application. Some examples' start commands are listed as follows:

### Starting StarGate Server

    ./sbin/start-stargate.sh

### Starting StarGate Client

#### Loopback Example

    ./bin/stargate loopback
    ./bin/stargate spark-local-loopback
    ./bin/stargate spark-yarn-loopback
    
#### KMeans Example

    ./bin/stargate kmeans
    ./bin/stargate spark-local-kmeans
    ./bin/stargate spark-yarn-kmeans

# Contacts

For any question or discussion, please contact the authors:

* Allen Zhou: pangchingchow@gmail.com
* Heyang Zhou: zhouheyang1@126.com
* Junbao Hu: 521yiyi1414@gmail.com
