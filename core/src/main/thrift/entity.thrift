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

namespace java tsinghua.stargate.rpc.thrift.message.entity

enum StoreTypeThrift {
  DISK = 0
  ALLUXIO = 1
  HDFS = 2
  IN_HEAP = 3
  OUT_HEAP = 4
}

enum AcceleratorWorkerTypeThrift {
  CPU = 0
  GPU = 1
  FPGA = 2
  ASIC = 3
}

enum ApplicationStateThrift {
  NEW = 0
  GRANT = 1
  REJECT = 2
  SUBMIT = 3
  ACCEPT = 4
  REPEAT = 5
  NONE = 6
  RUNNING = 7
  FINISHED = 8
  FAILED = 9
  KILLED = 10
}

struct ApplicationIdThrift {
  1: optional i32 id
  2: optional i64 timeStamp
}

struct AcceleratorResourceThrift {
  1: required string workloadId
  2: required i32 coreId
  3: optional i64 coreMemory
  4: optional i64 coreFrequency
}

struct ServiceDataThrift {
  1: required string path
  2: optional i64 capacity
  3: optional bool cached
  4: required StoreTypeThrift storeType
}

struct ServiceDataProcessThrift {
  1: required binary serviceDataProcessBinarys
}

struct ApplicationLaunchContextThrift {
  1: required AcceleratorWorkerTypeThrift acceleratorWorkerType
  2: optional ServiceDataThrift inputServiceData
  3: optional ServiceDataThrift outputServiceData
  4: optional string userApplicationId
  5: optional map<string, string> processors
  6: optional map<string, string> environments
  7: optional map<string, string> resources
}

struct ApplicationSubmissionContextThrift {
  1: required ApplicationStateThrift applicationState
  2: required ApplicationIdThrift applicationId
  3: required ApplicationLaunchContextThrift applicationLaunchContext
  4: required AcceleratorResourceThrift acceleratorResourceThrift
}

struct ApplicationResourceUsageReportThrift {
  1: optional AcceleratorWorkerTypeThrift workerType
  2: optional string hardwareId
  3: optional AcceleratorResourceThrift resource
}

struct ApplicationReportThrift {
  1: required ApplicationStateThrift applicationState
  2: required ApplicationIdThrift applicationId
  3: optional i64 startTime
  4: optional i64 finishTime
  5: optional ApplicationResourceUsageReportThrift applicationResourceUsageReportThrift
  6: optional ServiceDataThrift serviceData
  7: optional string diagnostics
}

struct CardReportThrift {
  1: required string cardId
  2: required string httpAddress
  3: optional i32 numTasks
  4: optional list<string> workloads
  5: optional list<AcceleratorResourceThrift> totalCapability
  6: optional list<AcceleratorResourceThrift> usedCapability
  7: optional list<AcceleratorResourceThrift> availableCapability
}