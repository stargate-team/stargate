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

import tsinghua.stargate.rpc.message.GetApplicationReportResponse;
import tsinghua.stargate.rpc.message.entity.ApplicationReport;
import tsinghua.stargate.rpc.message.entity.thrift.ApplicationReportThriftImpl;
import tsinghua.stargate.rpc.thrift.message.GetApplicationReportResponseThrift;
import tsinghua.stargate.rpc.thrift.message.entity.ApplicationReportThrift;

public class GetApplicationReportResponseThriftImpl
    extends GetApplicationReportResponse {

  private GetApplicationReportResponseThrift thrift;
  private boolean reset = false;
  private ApplicationReport applicationReport = null;

  public GetApplicationReportResponseThriftImpl(
      GetApplicationReportResponseThrift thrift) {
    this.thrift = thrift;
  }

  public GetApplicationReportResponseThriftImpl() {
    this.thrift = new GetApplicationReportResponseThrift();
  }

  private void maybeInitThrift() {
    if (thrift == null) {
      thrift = new GetApplicationReportResponseThrift();
    }
  }

  private void mergeLocalToThrift() {
    maybeInitThrift();
    if (this.applicationReport != null) {
      thrift
          .setApplicationReport(convertToThriftFormat(this.applicationReport));
    }
    reset = false;
  }

  @Override
  public ApplicationReport getApplicationReport() {
    if (this.applicationReport != null) {
      return this.applicationReport;
    }

    if (!thrift.isSetApplicationReport()) {
      return null;
    }

    this.applicationReport =
        convertFromThriftFormat(thrift.getApplicationReport());
    return this.applicationReport;
  }

  @Override
  public void setApplicationReport(ApplicationReport applicationReport) {
    maybeInitThrift();
    if (applicationReport == null) {
      thrift.unsetApplicationReport();
    }
    this.applicationReport = applicationReport;
    this.reset = true;
  }

  public GetApplicationReportResponseThrift getThrift() {
    if (!reset) {
      return thrift;
    } else {
      mergeLocalToThrift();
    }
    return thrift;
  }

  public ApplicationReportThrift convertToThriftFormat(
      ApplicationReport applicationReport) {
    return ((ApplicationReportThriftImpl) applicationReport).getThrift();
  }

  public ApplicationReport convertFromThriftFormat(
      ApplicationReportThrift thrift) {
    return new ApplicationReportThriftImpl(thrift);
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
