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

import java.util.Map;

import tsinghua.stargate.rpc.message.entity.Worker;
import tsinghua.stargate.rpc.message.entity.ApplicationLaunchContext;
import tsinghua.stargate.rpc.message.entity.ServiceData;
import tsinghua.stargate.rpc.thrift.message.entity.ApplicationLaunchContextThrift;
import tsinghua.stargate.rpc.thrift.message.entity.ServiceDataThrift;
import tsinghua.stargate.util.ThriftUtils;

import com.google.common.base.Preconditions;

public class ApplicationLaunchContextThriftImpl
    extends ApplicationLaunchContext {

  private ApplicationLaunchContextThrift thrift;
  private boolean reset = false;
  private Worker worker;
  private ServiceData inputServiceData;
  private ServiceData outputServiceData;

  public ApplicationLaunchContextThriftImpl(
      ApplicationLaunchContextThrift thrift) {
    this.thrift = thrift;
  }

  public ApplicationLaunchContextThriftImpl() {
    this.thrift = new ApplicationLaunchContextThrift();
  }

  public boolean isReset() {
    return reset;
  }

  @Override
  public Worker getWorker() {

    if (this.worker != null) {
      return this.worker;
    }

    if (!thrift.isSetAcceleratorWorkerType()) {
      return null;
    }

    this.worker =
        ThriftUtils.convertFromThriftFormat(thrift.getAcceleratorWorkerType());
    return worker;
  }

  @Override
  public void setWorker(Worker worker) {
    maybeInitThrift();
    if (worker == null) {
      thrift.unsetAcceleratorWorkerType();
    }
    this.worker = worker;
    this.reset = true;
  }

  private void maybeInitThrift() {
    if (thrift == null) {
      thrift = new ApplicationLaunchContextThrift();
    }
  }

  @Override
  public String getUserAppId() {
    Preconditions.checkNotNull(thrift);
    return thrift.getUserApplicationId();
  }

  @Override
  public void setUserAppId(String id) {
    Preconditions.checkNotNull(thrift);
    thrift.setUserApplicationId(id);
    reset = true;
  }

  @Override
  public ServiceData getInputServiceData() {
    if (this.inputServiceData != null) {
      return this.inputServiceData;
    }

    if (!thrift.isSetInputServiceData()) {
      return null;
    }

    this.inputServiceData =
        convertFromThriftFormat(thrift.getInputServiceData());
    return this.inputServiceData;
  }

  @Override
  public void setInputServiceData(ServiceData serviceData) {
    maybeInitThrift();
    if (serviceData == null) {
      thrift.unsetInputServiceData();
    }
    this.inputServiceData = serviceData;
    reset = true;
  }

  @Override
  public ServiceData getOutputServiceData() {

    if (this.outputServiceData != null) {
      return this.outputServiceData;
    }

    if (!thrift.isSetOutputServiceData()) {
      return null;
    }

    this.outputServiceData =
        convertFromThriftFormat(thrift.getOutputServiceData());
    return outputServiceData;
  }

  @Override
  public void setOutputServiceData(ServiceData serviceData) {
    maybeInitThrift();
    if (serviceData == null) {
      thrift.unsetOutputServiceData();
    }
    this.outputServiceData = serviceData;
    reset = true;
  }

  @Override
  public Map<String, String> getServiceDataProcessor() {
    return thrift.getProcessors();
  }

  @Override
  public void setServiceDataProcessor(Map<String, String> processors) {
    if (null == processors) {
      thrift.unsetProcessors();
    } else {
      thrift.setProcessors(processors);
    }
  }

  @Override
  public Map<String, String> getEnvironments() {
    return thrift.getEnvironments();
  }

  @Override
  public void setEnvironments(Map<String, String> environments) {
    if (null == environments) {
      thrift.unsetEnvironments();
    } else {
      thrift.setEnvironments(environments);
    }
  }

  @Override
  public Map<String, String> getResources() {
    return thrift.getResources();
  }

  @Override
  public void setResources(Map<String, String> resources) {
    if (null == resources) {
      thrift.unsetEnvironments();
    } else {
      thrift.setResources(resources);
    }
  }

  private void mergeLocalToThrift() {
    maybeInitThrift();

    if (this.inputServiceData != null) {
      thrift.setInputServiceData(convertToThriftFormat(this.inputServiceData));
    }

    if (this.worker != null) {
      thrift.setAcceleratorWorkerType(
          ThriftUtils.convertToThriftFormat(this.worker));
    }

    if (this.outputServiceData != null) {
      thrift
          .setOutputServiceData(convertToThriftFormat(this.outputServiceData));
    }
    reset = false;
  }

  private ServiceDataThriftImpl convertFromThriftFormat(
      ServiceDataThrift thrift) {
    return new ServiceDataThriftImpl(thrift);
  }

  private ServiceDataThrift convertToThriftFormat(ServiceData data) {
    return ((ServiceDataThriftImpl) data).getThrift();
  }

  public ApplicationLaunchContextThrift getThrift() {

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
