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

import tsinghua.stargate.rpc.message.GetNewApplicationResponse;
import tsinghua.stargate.rpc.message.entity.AcceleratorResource;
import tsinghua.stargate.rpc.message.entity.ApplicationId;
import tsinghua.stargate.rpc.message.entity.ApplicationState;
import tsinghua.stargate.rpc.message.entity.thrift.AcceleratorResourceThriftImpl;
import tsinghua.stargate.rpc.message.entity.thrift.ApplicationIdThriftImpl;
import tsinghua.stargate.rpc.thrift.message.GetNewApplicationResponseThrift;
import tsinghua.stargate.rpc.thrift.message.entity.AcceleratorResourceThrift;
import tsinghua.stargate.rpc.thrift.message.entity.ApplicationIdThrift;
import tsinghua.stargate.util.ThriftUtils;

public class GetNewApplicationResponseThriftImpl
    extends GetNewApplicationResponse {

  private GetNewApplicationResponseThrift thrift;
  private boolean reset = true;

  private ApplicationId applicationId = null;
  private AcceleratorResource maximumResourceCapability = null;
  private ApplicationState ApplicationState;

  public GetNewApplicationResponseThriftImpl() {
    this.thrift = new GetNewApplicationResponseThrift();
  }

  public GetNewApplicationResponseThriftImpl(
      GetNewApplicationResponseThrift thrift) {
    this.thrift = thrift;
    this.reset = false;
  }

  public GetNewApplicationResponseThrift getThrift() {
    if (!reset) {
      return thrift;
    } else {
      mergeLocalToThrift();
    }
    return thrift;
  }

  private void mergeLocalToThrift() {
    maybeInitThrift();

    if (this.applicationId != null) {
      thrift.setApplicationId(convertToThriftFormat(this.applicationId));
    }

    if (this.maximumResourceCapability != null) {
      thrift.setMaximumCapability(
          convertToThriftFormat(this.maximumResourceCapability));
    }

    if (this.ApplicationState != null) {
      thrift.setApplicationState(
          ThriftUtils.convertToThriftFormat(this.ApplicationState));
    }

    reset = false;
  }

  private void maybeInitThrift() {
    if (thrift == null) {
      thrift = new GetNewApplicationResponseThrift();
    }
  }

  @Override
  public ApplicationId getApplicationId() {
    if (this.applicationId != null) {
      return this.applicationId;
    }

    if (!thrift.isSetApplicationId()) {
      return null;
    }

    this.applicationId = convertFromThriftFormat(thrift.getApplicationId());
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

  @Override
  public AcceleratorResource getMaxResourceCapability() {
    if (this.maximumResourceCapability != null) {
      return this.maximumResourceCapability;
    }

    if (!thrift.isSetMaximumCapability()) {
      return null;
    }

    this.maximumResourceCapability =
        convertFromThriftFormat(thrift.getMaximumCapability());
    return this.maximumResourceCapability;
  }

  @Override
  public void setMaxResourceCapability(AcceleratorResource capability) {
    maybeInitThrift();
    if (capability == null) {
      thrift.unsetMaximumCapability();
    }
    this.maximumResourceCapability = capability;
    this.reset = true;
  }

  @Override
  public ApplicationState getApplicationState() {
    if (this.ApplicationState != null) {
      return this.ApplicationState;
    }

    if (!thrift.isSetApplicationState()) {
      return null;
    }

    this.ApplicationState =
        ThriftUtils.convertFromThriftFormat(thrift.getApplicationState());
    return this.ApplicationState;
  }

  @Override
  public void setApplicationState(ApplicationState type) {
    maybeInitThrift();
    if (ApplicationState == null) {
      thrift.unsetApplicationState();
    }
    this.ApplicationState = type;
    this.reset = true;
  }

  private ApplicationId convertFromThriftFormat(ApplicationIdThrift thrift) {
    return new ApplicationIdThriftImpl(thrift);
  }

  private ApplicationIdThrift convertToThriftFormat(ApplicationId t) {
    return ((ApplicationIdThriftImpl) t).getThrift();
  }

  private AcceleratorResource convertFromThriftFormat(
      AcceleratorResourceThrift thrift) {
    return new AcceleratorResourceThriftImpl(thrift);
  }

  private AcceleratorResourceThrift convertToThriftFormat(
      AcceleratorResource t) {
    return ((AcceleratorResourceThriftImpl) t).getThrift();
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