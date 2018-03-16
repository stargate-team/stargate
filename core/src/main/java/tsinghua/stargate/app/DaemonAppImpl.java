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

package tsinghua.stargate.app;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.ReadLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.WriteLock;

import tsinghua.stargate.DaemonContext;
import tsinghua.stargate.Log;
import tsinghua.stargate.exception.StarGateException;
import tsinghua.stargate.rpc.message.entity.*;
import tsinghua.stargate.task.*;
import tsinghua.stargate.util.ReflectionUtils;

@SuppressWarnings("unchecked")
public class DaemonAppImpl extends Log implements DaemonApp {

  private final ApplicationId anmAppId;

  private final AcceleratorResource acceleratorResource;
  private final String userAppId;
  private final Worker worker;
  private final ServiceData inputServiceData;
  private final ServiceData outputServiceData;
  private final Map<String, String> resources;
  private final Map<String, String> environments;
  private final Map<String, String> processors;

  private final Map<TaskId, Task> tasks = new LinkedHashMap<TaskId, Task>();
  private final ReadLock readLock;
  private final WriteLock writeLock;
  private TaskManagerImpl taskManager;
  private DaemonContext context;
  private volatile TaskImpl currentTask;
  private long startTime;
  private long finishTime = 0;
  private ApplicationState state = ApplicationState.NEW;

  private final StringBuilder diagnostics = new StringBuilder();

  public DaemonAppImpl(ApplicationId applicationId, TaskManagerImpl taskManager,
      DaemonContext context, AcceleratorResource acceleratorResource,
      String userAppId, Worker worker, ServiceData inputServiceData,
      ServiceData outputServiceData, Map<String, String> processors,
      Map<String, String> environments, Map<String, String> resources) {
    this.anmAppId = applicationId;
    this.taskManager = taskManager;
    this.acceleratorResource = acceleratorResource;
    this.context = context;
    this.userAppId = userAppId;
    this.worker = worker;
    this.inputServiceData = inputServiceData;
    this.outputServiceData = outputServiceData;
    this.startTime = System.currentTimeMillis();
    this.processors = processors;
    this.resources = resources;
    this.environments = environments;

    ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
    this.readLock = lock.readLock();
    this.writeLock = lock.writeLock();
  }

  @Override
  public ApplicationId getApplicationId() {
    return this.anmAppId;
  }

  @Override
  public ApplicationReport generateAppReport() {
    ApplicationReport appReport =
        ReflectionUtils.get().getMsg(ApplicationReport.class);
    appReport.setApplicationState(state);
    appReport.setStartTime(startTime);
    appReport.setFinishTime(finishTime);
    appReport.setApplicationId(anmAppId);
    appReport.setDiagnostics(diagnostics.toString());
    String taskId = null;
    if (currentTask != null) {
      taskId = currentTask.getTaskId().getId();
    }
    if (taskId != null) {
      ApplicationResourceUsageReport resourceUsageReport =
          context.getScheduler().getTaskResourceUsageReport(taskId);
      appReport.setApplicationResourceUsageReport(resourceUsageReport);
    }
    return appReport;
  }

  private synchronized void createAndStartNewTask() {
    this.currentTask = (TaskImpl) taskManager.createTask(anmAppId, userAppId,
        worker, acceleratorResource.getAcceleratorWorkload());

    try {
      currentTask.createBlockData(inputServiceData, outputServiceData);
      currentTask.createDependencies(resources, environments, processors);
    } catch (StarGateException e) {
      String msg = "crate accelerator task failed";
      error(msg);
      context.getDispatcher().getEventHandler()
          .handle(new TaskFailedEvent(currentTask.getTaskId(), msg));
    }

    tasks.put(currentTask.getTaskId(), currentTask);
    state = ApplicationState.SUBMIT;
    startTime = System.currentTimeMillis();
    context.getDispatcher().getEventHandler()
        .handle(new TaskStartedEvent(currentTask.getTaskId()));
  }

  @Override
  public void handle(DaemonAppEvent event) {
    this.writeLock.lock();
    try {
      switch (event.getType()) {
      case START:
        debug("Create accelerator task");
        if (!(event instanceof DaemonAppEvent)) {
          throw new RuntimeException("Unexpected event type: " + event);
        }
        createAndStartNewTask();
        break;

      case TASK_ACCEPTED:
        state = ApplicationState.ACCEPT;
        break;

      case TASK_REJECTED:
        diagnostics.append(((DaemonAppRejectedEvent) event).getMessage());
        state = ApplicationState.FAILED;
        break;

      case TASK_RUNNING:
        state = ApplicationState.RUNNING;
        break;

      case TASK_FINISHED:
        state = ApplicationState.FINISHED;
        break;

      case TASK_FAILED:
        if (!(event instanceof DaemonAppEvent)) {
          throw new RuntimeException("Unexpected event type: " + event);
        }
        diagnostics.append(((DaemonAppFailedTaskEvent) event).getDiagnostics());
        state = ApplicationState.FAILED;
        break;

      case KILLED:
        state = ApplicationState.KILLED;
        break;

      default:
        break;
      }
    } finally {
      this.writeLock.unlock();
    }
  }

  @Override
  public Task getAcceleratorTask(TaskId taskId) {
    this.readLock.lock();
    try {
      return this.tasks.get(taskId);
    } finally {
      this.readLock.unlock();
    }
  }
}