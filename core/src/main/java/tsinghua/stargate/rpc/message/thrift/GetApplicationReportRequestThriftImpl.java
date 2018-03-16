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

package tsinghua.stargate.rpc.message.thrift;

import tsinghua.stargate.rpc.message.GetApplicationReportRequest;
import tsinghua.stargate.rpc.message.entity.ApplicationId;
import tsinghua.stargate.rpc.thrift.message.GetApplicationReportRequestThrift;
import tsinghua.stargate.util.ThriftUtils;

public class GetApplicationReportRequestThriftImpl
    extends GetApplicationReportRequest {

  private GetApplicationReportRequestThrift thrift;
  private boolean reset = false;
  private ApplicationId applicationId = null;

  public GetApplicationReportRequestThriftImpl(
      GetApplicationReportRequestThrift thrift) {
    this.thrift = thrift;
  }

  public GetApplicationReportRequestThriftImpl() {
    this.thrift = new GetApplicationReportRequestThrift();
  }

  @Override
  public ApplicationId getApplicationId() {
    if (this.applicationId != null) {
      return this.applicationId;
    }

    if (!thrift.isSetApplicationId()) {
      return null;
    }

    this.applicationId =
        ThriftUtils.convertFromThriftFormat(thrift.getApplicationId());
    return this.applicationId;
  }

  @Override
  public void setApplicationId(ApplicationId applicationId) {
    maybeInitThrift();
    if (applicationId == null) {
      thrift.unsetApplicationId();
    }
    this.applicationId = applicationId;
    this.reset = true;
  }

  private void maybeInitThrift() {
    if (thrift == null) {
      thrift = new GetApplicationReportRequestThrift();
    }
  }

  private void mergeLocalToThrift() {
    maybeInitThrift();
    if (this.applicationId != null) {
      thrift.setApplicationId(
          ThriftUtils.convertToThriftFormat(this.applicationId));
    }
    reset = false;
  }

  public GetApplicationReportRequestThrift getThrift() {
    if (!reset) {
      return thrift;
    } else {
      mergeLocalToThrift();
    }
    return thrift;
  }

  @Override
  public int hashCode() {
    return getThrift().hashCode();
  }

  @Override
  public boolean equals(Object other) {
    if (other == null) {
      return false;
    }
    if (other.getClass().isAssignableFrom(this.getClass())) {
      return this.getThrift().equals(this.getClass().cast(other).getThrift());
    }
    return false;
  }

  @Override
  public String toString() {
    return getThrift().toString();
  }
}
