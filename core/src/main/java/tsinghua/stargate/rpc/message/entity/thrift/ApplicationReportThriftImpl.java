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

import tsinghua.stargate.rpc.message.entity.ApplicationId;
import tsinghua.stargate.rpc.message.entity.ApplicationReport;
import tsinghua.stargate.rpc.message.entity.ApplicationResourceUsageReport;
import tsinghua.stargate.rpc.message.entity.ApplicationState;
import tsinghua.stargate.rpc.thrift.message.entity.ApplicationReportThrift;
import tsinghua.stargate.rpc.thrift.message.entity.ApplicationResourceUsageReportThrift;
import tsinghua.stargate.util.ThriftUtils;

public class ApplicationReportThriftImpl extends ApplicationReport {

  ApplicationReportThrift thrift;
  private boolean reset = false;
  private ApplicationId applicationId;
  private ApplicationResourceUsageReport applicationResourceUsageReport;

  public ApplicationReportThriftImpl(ApplicationReportThrift thrift) {
    this.thrift = thrift;
  }

  public ApplicationReportThriftImpl() {
    this.thrift = new ApplicationReportThrift();
  }

  private void maybeInitThrift() {
    if (thrift == null) {
      thrift = new ApplicationReportThrift();
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
  public long getStartTime() {
    return thrift.getStartTime();
  }

  @Override
  public void setStartTime(long startTime) {
    thrift.setStartTime(startTime);
  }

  @Override
  public long getFinishTime() {
    return thrift.getFinishTime();
  }

  @Override
  public void setFinishTime(long finishTime) {
    thrift.setFinishTime(finishTime);
  }

  @Override
  public ApplicationResourceUsageReport getApplicationResourceUsageReport() {
    if (this.applicationResourceUsageReport != null) {
      return this.applicationResourceUsageReport;
    }

    if (!thrift.isSetApplicationResourceUsageReportThrift()) {
      return null;
    }

    this.applicationResourceUsageReport = convertFromThriftFormat(
        thrift.getApplicationResourceUsageReportThrift());
    return this.applicationResourceUsageReport;
  }

  @Override
  public void setApplicationResourceUsageReport(
      ApplicationResourceUsageReport resourceUsageReport) {
    this.applicationResourceUsageReport = resourceUsageReport;
  }

  @Override
  public ApplicationState getApplicationState() {
    return ThriftUtils.convertFromThriftFormat(thrift.getApplicationState());
  }

  @Override
  public void setApplicationState(ApplicationState type) {
    thrift.setApplicationState(ThriftUtils.convertToThriftFormat(type));
  }

  @Override
  public String getDiagnostics() {
    return thrift.getDiagnostics();
  }

  @Override
  public void setDiagnostics(String diagnostics) {
    if (diagnostics == null) {
      thrift.unsetDiagnostics();
    } else {
      thrift.setDiagnostics(diagnostics);
    }
  }

  public ApplicationReportThrift getThrift() {

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
      thrift.setApplicationId(
          ThriftUtils.convertToThriftFormat(this.applicationId));
    }

    if (this.applicationResourceUsageReport != null) {
      thrift.setApplicationResourceUsageReportThrift(
          convertToThriftFormat(this.applicationResourceUsageReport));
    }
    reset = false;
  }

  public ApplicationResourceUsageReport convertFromThriftFormat(
      ApplicationResourceUsageReportThrift thrift) {
    return new ApplicationResourceUsageReportThriftImpl(thrift);
  }

  public ApplicationResourceUsageReportThrift convertToThriftFormat(
      ApplicationResourceUsageReport applicationResourceUsageReport) {
    return ((ApplicationResourceUsageReportThriftImpl) applicationResourceUsageReport)
        .getThrift();
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
