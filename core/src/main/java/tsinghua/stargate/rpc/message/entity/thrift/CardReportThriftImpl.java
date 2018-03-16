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

import java.util.ArrayList;
import java.util.List;

import tsinghua.stargate.rpc.message.entity.AcceleratorResource;
import tsinghua.stargate.rpc.message.entity.CardReport;
import tsinghua.stargate.rpc.thrift.message.entity.AcceleratorResourceThrift;
import tsinghua.stargate.rpc.thrift.message.entity.CardReportThrift;
import tsinghua.stargate.util.ThriftUtils;

public class CardReportThriftImpl extends CardReport {

  private CardReportThrift thrift;
  private boolean reset = false;

  private List<AcceleratorResource> usedResources;
  private List<AcceleratorResource> totalResources;

  @Override
  public String getCardId() {
    return thrift.getCardId();
  }

  @Override
  public void setCardId(String cardId) {
    if (cardId == null) {
      thrift.unsetCardId();
    } else {
      thrift.setCardId(cardId);
    }
  }

  @Override
  public String getHttpAddress() {
    return thrift.getHttpAddress();
  }

  @Override
  public void setHttpAddress(String httpAddress) {
    if (httpAddress == null) {
      thrift.unsetHttpAddress();
    } else {
      thrift.setHttpAddress(httpAddress);
    }
  }

  @Override
  public List<AcceleratorResource> getUsedCapability() {
    if (this.usedResources != null) {
      return usedResources;
    } else {
      usedResources = new ArrayList<>();
      for (AcceleratorResourceThrift resourceThrift : thrift
          .getUsedCapability()) {
        usedResources.add(ThriftUtils.convertFromThriftFormat(resourceThrift));
      }
      return usedResources;
    }
  }

  @Override
  public void setUsedCapability(List<AcceleratorResource> used) {
    if (used == null || used.isEmpty()) {
      usedResources.clear();
      return;
    } else {
      usedResources = new ArrayList<>();
      usedResources.addAll(used);
    }
    this.reset = true;
  }

  @Override
  public List<AcceleratorResource> getTotalCapability() {
    if (this.totalResources != null) {
      return totalResources;
    } else {
      totalResources = new ArrayList<>();
      for (AcceleratorResourceThrift resourceThrift : thrift
          .getUsedCapability()) {
        totalResources.add(ThriftUtils.convertFromThriftFormat(resourceThrift));
      }
      return totalResources;
    }

  }

  @Override
  public void setTotalCapability(List<AcceleratorResource> capability) {
    if (capability == null || capability.isEmpty()) {
      totalResources.clear();
      return;
    } else {
      totalResources = new ArrayList<>();
      totalResources.addAll(capability);
    }
    this.reset = true;
  }

  @Override
  public int getNumTasks() {
    return thrift.getNumTasks();
  }

  @Override
  public void setNumTasks(int numNumTasks) {
    thrift.setNumTasks(numNumTasks);
  }

  public CardReportThrift getThrift() {
    if (!reset) {
      return thrift;
    } else {
      mergeLocalToThrift();
    }
    return thrift;
  }

  private void maybeInitThrift() {
    if (thrift == null) {
      thrift = new CardReportThrift();
    }
  }

  private void mergeLocalToThrift() {
    maybeInitThrift();

    if (this.usedResources != null) {
      for (AcceleratorResource resource : usedResources) {
        thrift.addToUsedCapability(ThriftUtils.convertToThriftFormat(resource));
      }
    }

    if (this.totalResources != null) {
      for (AcceleratorResource resource : totalResources) {
        thrift.addToUsedCapability(ThriftUtils.convertToThriftFormat(resource));
      }
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
