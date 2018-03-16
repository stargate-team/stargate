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
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import tsinghua.stargate.Log;
import tsinghua.stargate.rpc.message.entity.AcceleratorResource;
import tsinghua.stargate.rpc.message.entity.Worker;
import tsinghua.stargate.util.AcceleratorResourceUtils;
import tsinghua.stargate.util.Utils;

public class SchedulableAccelerator extends Log {

  private final ConcurrentHashMap<String, SchedulableTask> launchedTasks =
      new ConcurrentHashMap<>();
  private Accelerator card;
  private List<AcceleratorResource> totalResources = new ArrayList<>();
  private List<AcceleratorResource> availableResources = new ArrayList<>();

  private List<AcceleratorResource> usedResources = new ArrayList<>();

  private int numTasks;

  public SchedulableAccelerator(Accelerator card) {
    this.card = card;
    for (AcceleratorResource resource : card.getResourceCapability()) {
      totalResources.add(AcceleratorResourceUtils.clone(resource));
      availableResources.add(AcceleratorResourceUtils.clone(resource));
    }
  }

  public synchronized void allocate(SchedulableTask task,
      AcceleratorResource resource) {
    if (resource.equals(AcceleratorResourceUtils.none())) {
      return;
    }

    numTasks++;
    launchedTasks.put(task.getName(), task);

    deductAvailableResources(resource);

    info(
        "Assign accelerator {} (workload: {}, core: {}, maxMemory: {}) "
            + "to task {} (userAppId: {} anmAppId: {})",
        getHardwareId(), resource.getAcceleratorWorkload(),
        resource.getAcceleratorCoreId(),
        Utils.bytes2String(resource.getAcceleratorCoreMemory()),
        task.getName(), task.getUserAppId(), task.getANMAppId());
  }

  private synchronized void deductAvailableResources(
      AcceleratorResource resource) {
    if (resource == null) {
      error("Invalid deduction of null resource for " + getHardwareId());
      return;
    }
    availableResources.remove(resource);
    usedResources.add(resource);
  }

  /**
   * Release an allocated accelerator task on this card.
   *
   * @param task Accelerator task to be released
   */
  public synchronized void release(SchedulableTask task) {
    if (!verifyTaskName(task.getName())) {
      error("No such task {} running on accelerator {}", task.getName(),
          getHardwareId());
      return;
    }

    AcceleratorResource resource = task.getAllocatedResource();

    if (null != launchedTasks.remove(task.getName())) {
      updateResources(resource);
    }

    info(
        "Release accelerator {} (workload: {}, core: {}, maxMemory: {}) "
            + "from task {} (userAppId: {})",
        getHardwareId(), resource.getAcceleratorWorkload(),
        resource.getAcceleratorCoreId(),
        Utils.bytes2String(resource.getAcceleratorCoreMemory()),
        task.getName(), task.getUserAppId());
  }

  private synchronized boolean verifyTaskName(String taskId) {
    return launchedTasks.containsKey(taskId);
  }

  private synchronized void updateResources(AcceleratorResource resource) {
    if (resource == null) {
      error("No specified resources for accelerator " + getHardwareId());
      return;
    }
    availableResources.add(resource);
    usedResources.remove(resource);
    numTasks--;
  }

  public String getHardwareId() {
    return card.getAcceleratorId();
  }

  public int getCardId() {
    return card.getAcceleratorIndex();
  }

  public Worker getWorker() {
    return card.getWorker();
  }

  public List<AcceleratorResource> getTotalResources() {
    return totalResources;
  }

  public List<AcceleratorResource> getAvailableResources() {
    return availableResources;
  }

  public List<AcceleratorResource> getUsedResources() {
    return usedResources;
  }

  public int getNumTasks() {
    return numTasks;
  }
}
