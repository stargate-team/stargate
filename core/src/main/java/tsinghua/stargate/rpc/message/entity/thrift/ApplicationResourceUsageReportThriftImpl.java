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
import tsinghua.stargate.rpc.message.entity.ApplicationResourceUsageReport;
import tsinghua.stargate.rpc.message.entity.Worker;
import tsinghua.stargate.rpc.thrift.message.entity.ApplicationResourceUsageReportThrift;
import tsinghua.stargate.util.ThriftUtils;

public class ApplicationResourceUsageReportThriftImpl
    extends ApplicationResourceUsageReport {

  private ApplicationResourceUsageReportThrift thrift;
  private boolean reset = false;

  private AcceleratorResource resource;

  private Worker worker;

  public ApplicationResourceUsageReportThriftImpl(
      ApplicationResourceUsageReportThrift thrift) {
    this.thrift = thrift;
  }

  public ApplicationResourceUsageReportThriftImpl() {
    this.thrift = new ApplicationResourceUsageReportThrift();
  }

  public Worker getWorker() {

    if (this.worker != null) {
      return this.worker;
    }

    if (!thrift.isSetWorkerType()) {
      return null;
    }

    this.worker = ThriftUtils.convertFromThriftFormat(thrift.getWorkerType());
    return worker;
  }

  public void setWorker(Worker type) {
    maybeInitThrift();
    if (type == null) {
      thrift.unsetWorkerType();
    }
    this.worker = type;
    this.reset = true;
  }

  @Override
  public String getHardwareId() {
    return thrift.getHardwareId();
  }

  @Override
  public void setHardwareId(String hardwareId) {

    if (hardwareId == null) {
      thrift.unsetHardwareId();
    }
    thrift.setHardwareId(hardwareId);
  }

  @Override
  public AcceleratorResource getAcceleratorResource() {
    if (this.resource != null) {
      return this.resource;
    }

    if (!thrift.isSetResource()) {
      return null;
    }

    this.resource = ThriftUtils.convertFromThriftFormat(thrift.getResource());
    return this.resource;
  }

  @Override
  public void setAcceleratorResource(AcceleratorResource assignedResource) {
    maybeInitThrift();
    if (assignedResource == null) {
      thrift.unsetResource();
    }
    this.resource = assignedResource;
    this.reset = true;
  }

  private void maybeInitThrift() {
    if (thrift == null) {
      thrift = new ApplicationResourceUsageReportThrift();
    }
  }

  private void mergeLocalToThrift() {
    maybeInitThrift();

    if (this.resource != null) {
      thrift.setResource(ThriftUtils.convertToThriftFormat(this.resource));
    }

    if (this.getWorker() != null) {
      thrift.setWorkerType(ThriftUtils.convertToThriftFormat(this.worker));
    }
    reset = false;
  }

  public ApplicationResourceUsageReportThrift getThrift() {

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
