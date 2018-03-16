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

import tsinghua.stargate.rpc.message.SubmitApplicationResponse;
import tsinghua.stargate.rpc.message.entity.AcceleratorResource;
import tsinghua.stargate.rpc.message.entity.ApplicationState;
import tsinghua.stargate.rpc.thrift.message.SubmitApplicationResponseThrift;
import tsinghua.stargate.util.ThriftUtils;

public class SubmitApplicationResponseThriftImpl
    extends SubmitApplicationResponse {

  private SubmitApplicationResponseThrift thrift;
  private boolean reset = false;

  private AcceleratorResource allocatedResource;
  private ApplicationState ApplicationState;

  public SubmitApplicationResponseThriftImpl(
      SubmitApplicationResponseThrift thrift) {
    this.thrift = thrift;
  }

  public SubmitApplicationResponseThriftImpl() {
    this.thrift = new SubmitApplicationResponseThrift();
  }

  private void maybeInitThrift() {
    if (thrift == null) {
      thrift = new SubmitApplicationResponseThrift();
    }
  }

  public SubmitApplicationResponseThrift getThrift() {
    if (!reset) {
      return thrift;
    } else {
      mergeLocalToThrift();
    }
    return thrift;
  }

  private void mergeLocalToThrift() {
    maybeInitThrift();

    if (this.ApplicationState != null) {
      thrift.setApplicationState(
          ThriftUtils.convertToThriftFormat(this.ApplicationState));
    }
    reset = false;
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
}
