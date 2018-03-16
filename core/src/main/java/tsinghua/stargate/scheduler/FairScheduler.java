/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
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
import java.util.concurrent.ConcurrentHashMap;

import com.google.common.base.Preconditions;
import com.google.common.base.Splitter;

import tsinghua.stargate.DaemonContext;
import tsinghua.stargate.app.DaemonAppRejectedEvent;
import tsinghua.stargate.app.UserApp;
import tsinghua.stargate.conf.Configuration;
import tsinghua.stargate.conf.NameSpace;
import tsinghua.stargate.exception.StarGateRuntimeException;
import tsinghua.stargate.rpc.message.entity.AcceleratorResource;
import tsinghua.stargate.rpc.message.entity.ApplicationId;
import tsinghua.stargate.rpc.message.entity.ApplicationResourceUsageReport;
import tsinghua.stargate.service.AbstractService;
import tsinghua.stargate.task.Task;
import tsinghua.stargate.task.TaskAddedEvent;
import tsinghua.stargate.task.TaskImpl;
import tsinghua.stargate.util.Utils;

@SuppressWarnings("unchecked")
public class FairScheduler extends AbstractService
    implements Scheduler<SchedulableTask, SchedulableAccelerator> {

  private Configuration conf;
  private DaemonContext context;

  /** A heartbeat thread for assigning resources to queues/tasks. */
  private Thread schedulingThread;
  /**
   * Scheduling interval in milliseconds.
   */
  private int schedulingInterval;

  /** All schedulable accelerators in a node, indexed by accelerator id. */
  private ConcurrentHashMap<String, SchedulableAccelerator> nodeAccelerators =
      new ConcurrentHashMap<>();
  /** All accelerator resources in a node. */
  private List<AcceleratorResource> nodeResources = new ArrayList<>();
  private AcceleratorResources maxShare = new AcceleratorResourcesImpl();

  private QueueManager queueManager;
  private ConcurrentHashMap<String, SchedulableTask> tasks =
      new ConcurrentHashMap<>();
  /** A cache for storing the weights of accelerator cards. */
  private ConcurrentHashMap<String, Float> taskWeights =
      new ConcurrentHashMap<>();

  public FairScheduler(DaemonContext context) {
    super("FairScheduler");
    this.context = context;
    schedulingThread = new SchedulingThread();
    schedulingThread.setName("FairScheduler Thread");
    schedulingThread.setDaemon(true);
    queueManager = new QueueManager(this);
  }

  @Override
  protected void serviceInit(Configuration conf) throws Exception {
    info("Init service '{}'", this.getClass().getSimpleName());
    this.conf = conf;
    queueManager.init();
    schedulingInterval = conf.getInt(NameSpace.SCHEDULING_INTERVAL,
        NameSpace.DEFAULT_SCHEDULING_INTERVAL);
  }

  @Override
  public void serviceStart() throws Exception {
    Preconditions.checkNotNull(schedulingThread, "Scheduling thread is null");
    info("Start {} for scheduling resources (interval: {} s)",
        schedulingThread.getName(), schedulingInterval / 1000);
    schedulingThread.start();
    info("Successfully started service '{}'", this.getClass().getSimpleName());
    super.serviceStart();
  }

  @Override
  protected void serviceStop() throws Exception {
    info("Successfully stopped service '{}'", this.getClass().getSimpleName());
    super.serviceStop();
  }

  @Override
  public void handle(SchedulerEvent event) {
    switch (event.getType()) {
    case TASK_ADDED:
      if (!(event instanceof TaskAddedSchedulerEvent)) {
        throw new StarGateRuntimeException("Unexpected event: " + event);
      }
      assignTask((TaskAddedSchedulerEvent) event);
      break;

    case TASK_REMOVED:
      if (!(event instanceof TaskRemovedSchedulerEvent)) {
        throw new StarGateRuntimeException("Unexpected event: " + event);
      }
      removeTask(((TaskRemovedSchedulerEvent) event));
      break;

    default:
      break;
    }
  }

  /**
   * Assign a task to a suitable schedulable queue after accelerator task added
   * event happens.
   *
   * @param event triggered when accelerator task is added
   */
  private synchronized void assignTask(TaskAddedSchedulerEvent event) {
    String userAppId = event.getTaskId().getUserAppId();
    String taskId = event.getTaskId().getId();
    ApplicationId anmAppId = event.getTaskId().getDaemonAppId();
    if (!acceleratorWorkLoadPreCheck(taskId)) {
      String message = "Reject " + anmAppId + " submitted by user " + userAppId
          + " with node no " + Utils.getTaskWorkLoad(taskId) + " accelerator";
      info(message);
      context.getDispatcher().getEventHandler()
          .handle(new DaemonAppRejectedEvent(event.getTaskId().getDaemonAppId(),
              message));
      return;
    }

    debug("Assign task {} to a queue", taskId);

    UserApp app = context.getUserApps().get(userAppId);
    TaskImpl task = (TaskImpl) app.getTask(taskId);
    String workload = task.getWorkload();

    String queueName = Utils.getQueueName(workload, userAppId);
    if (queueName.startsWith(".") || queueName.endsWith(".")) {
      error("Assign task {} to an invalid queue {}", taskId, queueName);
      return;
    }
    SchedulableLeafQueue leafQueue = queueManager.getLeafQueue(queueName, true);
    SchedulableTask schedulableTask = getSchedulableTask(task, leafQueue, this);
    tasks.put(schedulableTask.getName(), schedulableTask);
    leafQueue.addAcceleratorTask(schedulableTask, true);

    context.getDispatcher().getEventHandler()
        .handle(new TaskAddedEvent(task.getTaskId()));
  }

  private boolean acceleratorWorkLoadPreCheck(String taskId) {
    String workLoad = Utils.getTaskWorkLoad(taskId);
    return maxShare.getWorkloads().contains(workLoad);
  }

  private SchedulableTask getSchedulableTask(Task task, SchedulableQueue queue,
      FairScheduler scheduler) {
    return new SchedulableTask(context, scheduler, queue, task);
  }

  /**
   * Update the resources of the associated accelerator card and stop the
   * associated task after accelerator workload updated event happens.
   *
   */
  private synchronized void completedTask(String appId, String taskId) {
    SchedulableTask task = getTasks().get(taskId);
    if (task == null) {
      error("No such task:{} is scheduled.", taskId);
      return;
    }
    task.completed();

    SchedulableAccelerator card =
        getNodeAccelerators().get(task.getAllocatedHardWareId());
    if (card == null) {
      error("No such card:{} is assigned to task:{}.",
          task.getAllocatedHardWareId(), taskId);
      return;
    }
    card.release(task);

    context.getUserApps().get(appId).removeTask(task.getName());
  }

  /**
   * Remove a task from hooked schedulable queue after accelerator task removed
   * event happens.
   *
   * @param event triggered when accelerator task is removed
   */
  private synchronized void removeTask(TaskRemovedSchedulerEvent event) {
    String taskId = event.getTaskId();
    String appId = event.getApplicationId();
    completedTask(appId, taskId);

    SchedulableTask task = getTasks().get(taskId);
    task.stop();
    SchedulableLeafQueue queue =
        queueManager.getLeafQueue(task.getQueue().getName(), false);
    queue.removeRunningTask(task);
    getTasks().remove(taskId);
  }

  // -- Scheduler machinery --

  @Override
  public ConcurrentHashMap<String, SchedulableAccelerator> getNodeAccelerators() {
    return nodeAccelerators;
  }

  @Override
  public AcceleratorResources getNodeResources() {
    return maxShare;
  }

  @Override
  public ConcurrentHashMap<String, SchedulableTask> getTasks() {
    return tasks;
  }

  @Override
  public ApplicationResourceUsageReport getTaskResourceUsageReport(
      String taskId) {
    SchedulableTask task = tasks.get(taskId);
    if (task == null) {
      return null;
    }
    return task.getResourceUsageReport();
  }

  @Override
  public AcceleratorReport getCardReport(String cardId) {
    SchedulableAccelerator card = nodeAccelerators.get(cardId);
    return card == null ? null : new AcceleratorReport(card);
  }

  // -- Instance category --

  public Configuration getConf() {
    return conf;
  }

  public Float getWeight(String workload) {
    Float weight = taskWeights.get(workload);
    if (weight == null) {
      weight = conf.getFloat(NameSpace.ACCELERATOR_WORKLOAD_WEIGHT + workload,
          NameSpace.DEFAULT_ACCELERATOR_WORKLOAD_WEIGHT);
      Float l = taskWeights.putIfAbsent(workload, weight);
      if (l != null) {
        weight = l;
      }
    }
    return weight;
  }

  /**
   * Load all resources preparing for running on accelerators, which are called
   * only once when accelerators are first loaded.
   *
   * @param conf StarGate configuration file
   */
  public synchronized void loadResources(Configuration conf)
      throws StarGateRuntimeException {
    Splitter splitter = Splitter.on(',').trimResults().omitEmptyStrings();

    Iterator<String> acceleratorIds =
        splitter.split(conf.get(NameSpace.ACCELERATOR_IDS)).iterator();

    if (!acceleratorIds.hasNext()) {
      throw new StarGateRuntimeException("There is no available accelerator, "
          + "contact your cluster administrator for "
          + "configuring accelerators into stargate-core.xml");
    }

    int acceleratorIndex = -1;
    while (acceleratorIds.hasNext()) {
      String hardWareId = acceleratorIds.next();
      SchedulableAccelerator accelerator = new SchedulableAccelerator(
          new Accelerator(conf, hardWareId, ++acceleratorIndex));
      nodeAccelerators.put(hardWareId, accelerator);
      nodeResources.addAll(accelerator.getTotalResources());
    }
    maxShare.addAll(nodeResources);
  }

  private synchronized void schedule() {
    queueManager.getRootQueue().assignResources();
  }

  /**
   * A heartbeat thread for scheduling and assigning resources to schedulable
   * queues or tasks.
   */
  private class SchedulingThread extends Thread {
    @Override
    public void run() {
      while (!Thread.currentThread().isInterrupted()) {
        try {
          Thread.sleep(schedulingInterval);
          schedule();
        } catch (InterruptedException e) {
          // TODO: do some cleanup to make it exit gracefully
          warn("{} has been interrupted.", getName(), e);
          return;
        }
      }
    }
  }
}
