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

package tsinghua.stargate.rpc.message.entity.thrift;

import tsinghua.stargate.rpc.message.entity.AcceleratorResource;
import tsinghua.stargate.rpc.message.entity.ApplicationId;
import tsinghua.stargate.rpc.message.entity.ApplicationLaunchContext;
import tsinghua.stargate.rpc.message.entity.ApplicationState;
import tsinghua.stargate.rpc.message.entity.ApplicationSubmissionContext;
import tsinghua.stargate.rpc.thrift.message.entity.ApplicationLaunchContextThrift;
import tsinghua.stargate.rpc.thrift.message.entity.ApplicationSubmissionContextThrift;
import tsinghua.stargate.util.ThriftUtils;

public class ApplicationSubmissionContextThriftImpl
    extends ApplicationSubmissionContext {

  private ApplicationSubmissionContextThrift thrift;
  private boolean reset = false;

  private ApplicationState applicationState;
  private ApplicationId applicationId;
  private ApplicationLaunchContext applicationLaunchContext;
  private AcceleratorResource resource;

  public ApplicationSubmissionContextThriftImpl(
      ApplicationSubmissionContextThrift thrift) {
    this.thrift = thrift;
  }

  public ApplicationSubmissionContextThriftImpl() {
    this.thrift = new ApplicationSubmissionContextThrift();
  }

  public static ApplicationLaunchContext convertFromThriftFormat(
      ApplicationLaunchContextThrift thrift) {
    return new ApplicationLaunchContextThriftImpl(thrift);
  }

  public static ApplicationLaunchContextThrift convertToThriftFormat(
      ApplicationLaunchContext applicationLaunchContext) {
    return ((ApplicationLaunchContextThriftImpl) applicationLaunchContext)
        .getThrift();
  }

  private void maybeInitThrift() {
    if (thrift == null) {
      thrift = new ApplicationSubmissionContextThrift();
    }
  }

  @Override
  public ApplicationState getApplicationState() {
    if (this.applicationState != null) {
      return this.applicationState;
    }

    if (!thrift.isSetApplicationState()) {
      return null;
    }

    this.applicationState =
        ThriftUtils.convertFromThriftFormat(thrift.getApplicationState());
    return this.applicationState;
  }

  @Override
  public void setApplicationState(ApplicationState type) {
    maybeInitThrift();
    if (this.applicationState == null) {
      thrift.unsetApplicationState();
    }
    this.applicationState = type;
    this.reset = true;
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

  @Override
  public ApplicationLaunchContext getApplicationLaunchContext() {

    if (this.applicationLaunchContext != null) {
      return this.applicationLaunchContext;
    }

    if (!thrift.isSetApplicationLaunchContext()) {
      return null;
    }

    this.applicationLaunchContext =
        convertFromThriftFormat(thrift.getApplicationLaunchContext());
    return this.applicationLaunchContext;
  }

  @Override
  public void setApplicationLaunchContext(
      ApplicationLaunchContext applicationLaunchContext) {
    maybeInitThrift();
    if (applicationLaunchContext == null) {
      thrift.unsetApplicationLaunchContext();
    }
    this.applicationLaunchContext = applicationLaunchContext;
    this.reset = true;
  }

  @Override
  public AcceleratorResource getAcceleratorResource() {
    if (this.resource != null) {
      return this.resource;
    }

    if (!thrift.isSetAcceleratorResourceThrift()) {
      return null;
    }

    this.resource = ThriftUtils
        .convertFromThriftFormat(thrift.getAcceleratorResourceThrift());
    return this.resource;
  }

  @Override
  public void setAcceleratorResource(AcceleratorResource resource) {
    maybeInitThrift();
    if (resource == null) {
      thrift.unsetAcceleratorResourceThrift();
    }
    this.resource = resource;
    this.reset = true;
  }

  private void mergeLocalToThrift() {
    maybeInitThrift();
    if (this.applicationId != null) {
      thrift.setApplicationId(
          ThriftUtils.convertToThriftFormat(this.applicationId));
    }

    if (this.resource != null) {
      thrift.setAcceleratorResourceThrift(
          ThriftUtils.convertToThriftFormat(this.resource));
    }

    if (this.applicationState != null) {
      thrift.setApplicationState(
          ThriftUtils.convertToThriftFormat(this.applicationState));
    }

    if (this.applicationLaunchContext != null) {
      thrift.setApplicationLaunchContext(
          convertToThriftFormat(this.applicationLaunchContext));
    }
    reset = false;
  }

  public ApplicationSubmissionContextThrift getThrift() {
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
