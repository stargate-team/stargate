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

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import tsinghua.stargate.DaemonContext;
import tsinghua.stargate.Log;
import tsinghua.stargate.rpc.message.entity.AcceleratorResource;
import tsinghua.stargate.rpc.message.entity.Worker;
import tsinghua.stargate.rpc.message.entity.ApplicationResourceUsageReport;
import tsinghua.stargate.task.Task;
import tsinghua.stargate.task.TaskAllocatedEvent;
import tsinghua.stargate.util.AcceleratorResourceUtils;
import tsinghua.stargate.util.ReflectionUtils;

@SuppressWarnings("unchecked")
public class SchedulableTask extends Log implements Schedulable {

  private DaemonContext context;

  private FairScheduler scheduler;

  private AcceleratorResource allocatedResource;

  private SchedulableQueue queue;

  private Task task;

  private boolean isScheduled;

  private long startTime;
  private long finishTime;

  private String allocatedHardWareId;

  private int allocatedCardId;

  private Worker worker;

  private boolean stopped;
  private boolean completed;

  private AcceleratorResources askResources;

  private CoreFrequencyComparator frequencyComparator =
      new CoreFrequencyComparator();

  public SchedulableTask(DaemonContext context, FairScheduler scheduler,
      SchedulableQueue queue, Task task) {
    this.context = context;
    this.scheduler = scheduler;
    this.queue = queue;
    this.task = task;
    this.startTime = System.currentTimeMillis();
    this.allocatedResource =
        AcceleratorResourceUtils.newInstance(task.getWorkload());
    this.isScheduled = false;
    this.askResources = new AcceleratorResourcesImpl();
  }

  // -- Schedulable machinery --

  @Override
  public String getName() {
    return task.getTaskId().getId();
  }

  @Override
  public Float getWeight() {
    return scheduler.getWeight(task.getWorkload());
  }

  @Override
  public long getStartTime() {
    return startTime;
  }

  @Override
  public long getSubmissionDuration() {
    return System.currentTimeMillis() - this.startTime;
  }

  @Override
  public AcceleratorResources askResources() {
    return askResources;
  }

  @Override
  public AcceleratorResources offerMaxShare() {
    return scheduler.getNodeResources();
  }

  @Override
  public AcceleratorResource assignAcceleratorResource(String cardId) {
    SchedulableAccelerator card = scheduler.getNodeAccelerators().get(cardId);

    List<AcceleratorResource> availableResources = card.getAvailableResources();
    AcceleratorResource selectedResource;
    synchronized (this) {
      if (!assignAcceleratorPreCheck(availableResources)) {
        return AcceleratorResourceUtils.none();
      }

      selectedResource = assignCore(availableResources, task.getWorkload());
      card.allocate(this, selectedResource);
      allocatedResource = selectedResource;
    }

    if (!allocatedResource.equals(AcceleratorResourceUtils.none())) {
      askResources.add(allocatedResource);
      allocatedHardWareId = card.getHardwareId();
      allocatedCardId = card.getCardId();
      worker = card.getWorker();
      context.getDispatcher().getEventHandler()
          .handle(new TaskAllocatedEvent(task.getTaskId(), this.allocatedCardId,
              allocatedResource.getAcceleratorCoreId(), allocatedResource));
    }

    return selectedResource;
  }

  private boolean assignAcceleratorPreCheck(
      List<AcceleratorResource> availableResources) {
    if (availableResources == null || availableResources.size() == 0
        || isScheduled)
      return false;
    for (AcceleratorResource resource : availableResources) {
      if (AcceleratorResourceUtils.lessThan(allocatedResource, resource)) {
        return true;
      }
    }
    return false;
  }

  public CoreFrequencyComparator getFrequencyComparator() {
    return frequencyComparator;
  }

  public synchronized AcceleratorResource assignCore(
      List<AcceleratorResource> availableResources, String workload) {

    // select the highest available frequency core
    Collections.sort(availableResources, getFrequencyComparator());
    for (AcceleratorResource resource : availableResources) {
      if (resource.getAcceleratorWorkload().equals(workload)) {
        isScheduled = true;
        return resource;
      }
    }
    return AcceleratorResourceUtils.none();
  }

  /**
   * Sort accelerator cards by available resources.
   */
  private class CoreFrequencyComparator
      implements Comparator<AcceleratorResource> {
    @Override
    public int compare(AcceleratorResource s1, AcceleratorResource s2) {
      int frequency1 = s1.getAcceleratorCoreFrequency();
      int frequency2 = s2.getAcceleratorCoreFrequency();
      return frequency2 - frequency1;
    }
  }

  // -- Instance methods --

  public String getAllocatedHardWareId() {
    return allocatedHardWareId;
  }

  public AcceleratorResource getAllocatedResource() {
    return allocatedResource;
  }

  public ApplicationResourceUsageReport getResourceUsageReport() {
    ApplicationResourceUsageReport report =
        ReflectionUtils.get().getMsg(ApplicationResourceUsageReport.class);
    report.setWorker(worker);
    report.setHardwareId(allocatedHardWareId);

    if (allocatedResource.getAcceleratorWorkload() == null)
      System.out.println(this.allocatedResource);

    report.setAcceleratorResource(allocatedResource);
    return report;
  }

  public SchedulableQueue getQueue() {
    return queue;
  }

  public void setQueue(SchedulableQueue queue) {
    this.queue = queue;
  }

  public String getUserAppId() {
    return task.getTaskId().getUserAppId();
  }

  public String getANMAppId() {
    return task.getTaskId().getDaemonAppId().toString();
  }

  public Task getTask() {
    return task;
  }

  public void completed() {
    completed = true;
    finishTime = System.currentTimeMillis();
    info("Completed {} cost {}s", getName(), (finishTime - startTime) / 1000);
  }

  public boolean isStopped() {
    return stopped;
  }

  public synchronized void stop() {
    this.stopped = true;
  }
}
