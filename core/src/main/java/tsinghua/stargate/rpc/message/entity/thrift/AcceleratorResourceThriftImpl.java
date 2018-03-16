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
import tsinghua.stargate.rpc.thrift.message.entity.AcceleratorResourceThrift;

/**
 * implement the AcceleratorResource
 */
public class AcceleratorResourceThriftImpl extends AcceleratorResource {

  private AcceleratorResourceThrift thrift;

  public AcceleratorResourceThriftImpl() {
    thrift = new AcceleratorResourceThrift();
  }

  public AcceleratorResourceThriftImpl(AcceleratorResourceThrift thrift) {
    this.thrift = thrift;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String getAcceleratorWorkload() {
    return thrift.getWorkloadId();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void setAcceleratorWorkload(String workload) {
    thrift.setWorkloadId(workload);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public int getAcceleratorCoreMemory() {
    return (int) thrift.getCoreMemory();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void setAcceleratorCoreMemory(Integer memory) {
    if (memory == null) {
      thrift.unsetCoreMemory();
    }
    thrift.setCoreMemory(memory);
  }

  @Override
  public int getAcceleratorCoreId() {
    return thrift.getCoreId();
  }

  @Override
  public void setAcceleratorCoreId(Integer id) {
    thrift.setCoreId(id);
  }

  @Override
  public Integer getAcceleratorCoreFrequency() {
    return (int) thrift.getCoreFrequency();
  }

  @Override
  public void setAcceleratorCoreFrequency(Integer coreFrequency) {
    if (coreFrequency == null) {
      thrift.unsetCoreFrequency();
    }
    thrift.setCoreFrequency(coreFrequency);
  }

  public AcceleratorResourceThrift getThrift() {
    return thrift;
  }

  public int compareTo(AcceleratorResource other) {
    return getAcceleratorWorkload().compareTo(other.getAcceleratorWorkload());
  }
}
