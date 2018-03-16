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

namespace java tsinghua.stargate.rpc.thrift.message

include "entity.thrift"

struct GetNewApplicationRequestThrift {
  1: required entity.ApplicationStateThrift applicationState
}

struct GetNewApplicationResponseThrift {
  1: required entity.ApplicationStateThrift applicationState
  2: optional entity.ApplicationIdThrift applicationId
  3: optional entity.AcceleratorResourceThrift maximumCapability
}

struct SubmitApplicationRequestThrift {
  1: optional entity.ApplicationSubmissionContextThrift applicationSubmissionContext
}

struct SubmitApplicationResponseThrift {
  1: required entity.ApplicationStateThrift applicationState
}

struct GetApplicationReportRequestThrift {
  1: required entity.ApplicationIdThrift applicationId
}

struct GetApplicationReportResponseThrift {
  1: required entity.ApplicationReportThrift applicationReport
}
