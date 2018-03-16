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

namespace java tsinghua.stargate.rpc.thrift

include "message.thrift"

service ApplicationStarGateProtocolService {
  message.GetNewApplicationResponseThrift getNewApplication(1: message.GetNewApplicationRequestThrift request)
  message.SubmitApplicationResponseThrift submitApplication(1: message.SubmitApplicationRequestThrift request)
  message.GetApplicationReportResponseThrift getApplicationReport(1: message.GetApplicationReportRequestThrift request)
}
