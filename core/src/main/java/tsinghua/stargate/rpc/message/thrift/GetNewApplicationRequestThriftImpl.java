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

import tsinghua.stargate.rpc.message.GetNewApplicationRequest;
import tsinghua.stargate.rpc.message.entity.ApplicationState;
import tsinghua.stargate.rpc.thrift.message.GetNewApplicationRequestThrift;
import tsinghua.stargate.rpc.thrift.message.entity.ApplicationStateThrift;

public class GetNewApplicationRequestThriftImpl
    extends GetNewApplicationRequest {

  private GetNewApplicationRequestThrift thrift;
  private boolean reset = true;
  private ApplicationState appState;

  public GetNewApplicationRequestThriftImpl() {
    this.thrift = new GetNewApplicationRequestThrift();
  }

  public GetNewApplicationRequestThriftImpl(
      GetNewApplicationRequestThrift thrift) {
    this.thrift = thrift;
  }

  @Override
  public ApplicationState getAppState() {
    if (this.appState != null) {
      return this.appState;
    }

    if (!thrift.isSetApplicationState()) {
      return null;
    }

    this.appState = convertFromThriftFormat(thrift.getApplicationState());
    return this.appState;
  }

  @Override
  public void setAppState(ApplicationState type) {
    maybeInitThrift();
    if (appState == null) {
      thrift.unsetApplicationState();
    }
    this.appState = type;
    this.reset = true;
  }

  private void maybeInitThrift() {
    if (thrift == null) {
      thrift = new GetNewApplicationRequestThrift();
    }
  }

  public GetNewApplicationRequestThrift getThrift() {
    if (!reset) {
      return thrift;
    } else {
      mergeLocalToThrift();
    }
    return thrift;
  }

  private void mergeLocalToThrift() {
    maybeInitThrift();
    if (this.appState != null) {
      thrift.setApplicationState(convertToThriftFormat(this.appState));
    }
    reset = false;
  }

  private ApplicationState convertFromThriftFormat(
      ApplicationStateThrift thrift) {
    return appState.valueOf(thrift.getValue());
  }

  private ApplicationStateThrift convertToThriftFormat(
      ApplicationState ApplicationState) {
    return ApplicationStateThrift.findByValue(ApplicationState.getValue());
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    GetNewApplicationRequestThriftImpl that =
        (GetNewApplicationRequestThriftImpl) o;

    return getThrift() != null ? getThrift().equals(that.getThrift())
        : that.getThrift() == null;

  }

  @Override
  public int hashCode() {
    return getThrift() != null ? getThrift().hashCode() : 0;
  }

  @Override
  public String toString() {
    return getThrift().toString();
  }
}
