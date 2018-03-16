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

package tsinghua.stargate.scheduler;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import tsinghua.stargate.Log;
import tsinghua.stargate.conf.Configuration;
import tsinghua.stargate.conf.NameSpace;
import tsinghua.stargate.exception.StarGateRuntimeException;
import tsinghua.stargate.rpc.message.entity.AcceleratorResource;
import tsinghua.stargate.rpc.message.entity.Worker;

import com.google.common.base.Splitter;

/**
 * This class represents an accelerator in a node. Note: Each node in a cluster
 * may own multiple accelerators and each accelerator can hold multiple cores
 * logically.
 */
public class Accelerator extends Log {

  private String acceleratorId;
  private int acceleratorIndex;
  private int numCores = 0;
  private Worker worker;

  private List<AcceleratorResource> resourceCapacity = new ArrayList<>();

  public Accelerator(Configuration conf, String acceleratorId,
      int acceleratorIndex) {
    this.acceleratorId = acceleratorId;
    this.acceleratorIndex = acceleratorIndex;

    info("Loading the configuration of installed accelerator {}",
        acceleratorId);

    try {
      Splitter splitter = Splitter.on(',').trimResults().omitEmptyStrings();
      this.worker = Worker
          .valueOf(conf.get(NameSpace.ACCELERATOR_TYPE_PREFIX + acceleratorId));
      Iterator<String> coreIdIterator = splitter
          .split(
              conf.get(NameSpace.ACCELERATOR_CORE_IDS_PREFIX + acceleratorId))
          .iterator();

      Iterator<String> coreWorkloadIterator = splitter
          .split(conf
              .get(NameSpace.ACCELERATOR_CORE_WORKLOADS_PREFIX + acceleratorId))
          .iterator();
      Iterator<String> coreMemoryIterator = splitter
          .split(conf
              .get(NameSpace.ACCELERATOR_CORE_MEMORY_PREFIX + acceleratorId))
          .iterator();

      Iterator<String> coreFrequencyIterator = splitter
          .split(conf
              .get(NameSpace.ACCELERATOR_CORE_FREQUENCY_PREFIX + acceleratorId))
          .iterator();

      while (coreIdIterator.hasNext()) {
        int coreId = Integer.parseInt(coreIdIterator.next());

        String coreWorkload = coreWorkloadIterator.next();

        if (coreWorkload == null) {
          throw new StarGateRuntimeException("core Workload can not null");
        }

        String tmp = coreMemoryIterator.next();
        int coreMemory;
        if (tmp == null) {
          coreMemory = NameSpace.DEFAULT_ACCELERATOR_CORE_MEMORY;
        } else {
          coreMemory = Integer.parseInt(tmp);
        }

        int frequency;
        tmp = coreFrequencyIterator.next();
        if (tmp == null) {
          frequency = NameSpace.DEFAULT_ACCELERATOR_CORE_FREQUENCY;
        } else {
          frequency = Integer.parseInt(tmp);
        }

        info(
            "Accelerator {} (workload: {}, core: {}, maxMemory: {}Mb,frequency:{}M)",
            acceleratorId, coreWorkload, coreId, coreMemory, frequency);
        resourceCapacity.add(AcceleratorResource.newInstance(coreWorkload,
            coreMemory, coreId, frequency));
        numCores++;
      }
    } catch (Exception e) {
      error("Find invalid accelerator {}", acceleratorId);
      throw new StarGateRuntimeException(e);
    }
  }

  public String getAcceleratorId() {
    return acceleratorId;
  }

  public int getNumCores() {
    return numCores;
  }

  public int getAcceleratorIndex() {
    return acceleratorIndex;
  }

  public Worker getWorker() {
    return worker;
  }

  public List<AcceleratorResource> getResourceCapability() {
    return resourceCapacity;
  }
}
