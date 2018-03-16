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

import tsinghua.stargate.rpc.message.SubmitApplicationRequest;
import tsinghua.stargate.rpc.message.entity.ApplicationSubmissionContext;
import tsinghua.stargate.rpc.message.entity.thrift.ApplicationSubmissionContextThriftImpl;
import tsinghua.stargate.rpc.thrift.message.SubmitApplicationRequestThrift;
import tsinghua.stargate.rpc.thrift.message.entity.ApplicationSubmissionContextThrift;

public class SubmitApplicationRequestThriftImpl
    extends SubmitApplicationRequest {

  private SubmitApplicationRequestThrift thrift;
  private boolean reset = false;
  private ApplicationSubmissionContext appSubmissionContext = null;

  public SubmitApplicationRequestThriftImpl(
      SubmitApplicationRequestThrift thrift) {
    this.thrift = thrift;
  }

  public SubmitApplicationRequestThriftImpl() {
    this.thrift = new SubmitApplicationRequestThrift();
  }

  @Override
  public ApplicationSubmissionContext getAppSubmissionContext() {

    if (this.appSubmissionContext != null) {
      return this.appSubmissionContext;
    }

    if (!thrift.isSetApplicationSubmissionContext()) {
      return null;
    }

    this.appSubmissionContext =
        convertFromThriftFormat(thrift.getApplicationSubmissionContext());
    return this.appSubmissionContext;
  }

  @Override
  public void setAppSubmissionContext(ApplicationSubmissionContext context) {
    maybeInitThrift();
    if (context == null) {
      thrift.unsetApplicationSubmissionContext();
    }
    this.appSubmissionContext = context;
    this.reset = true;
  }

  private void maybeInitThrift() {
    if (thrift == null) {
      thrift = new SubmitApplicationRequestThrift();
    }
  }

  private ApplicationSubmissionContext convertFromThriftFormat(
      ApplicationSubmissionContextThrift thrift) {
    return new ApplicationSubmissionContextThriftImpl(thrift);
  }

  private ApplicationSubmissionContextThrift convertToThriftFormat(
      ApplicationSubmissionContext t) {
    return ((ApplicationSubmissionContextThriftImpl) t).getThrift();
  }

  public SubmitApplicationRequestThrift getThrift() {
    if (!reset) {
      return thrift;
    } else {
      mergeLocalToThrift();
    }
    return thrift;
  }

  private void mergeLocalToThrift() {
    maybeInitThrift();
    if (this.appSubmissionContext != null) {
      thrift.setApplicationSubmissionContext(
          convertToThriftFormat(this.appSubmissionContext));
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
}
