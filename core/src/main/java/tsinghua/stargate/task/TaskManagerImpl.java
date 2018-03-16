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

package tsinghua.stargate.task;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

import com.google.common.base.Preconditions;

import tsinghua.stargate.DaemonContext;
import tsinghua.stargate.app.UserApp;
import tsinghua.stargate.app.UserAppImpl;
import tsinghua.stargate.conf.Configuration;
import tsinghua.stargate.rpc.message.entity.ApplicationId;
import tsinghua.stargate.rpc.message.entity.Worker;
import tsinghua.stargate.service.CompositeService;
import tsinghua.stargate.task.launcher.TaskLauncher;
import tsinghua.stargate.task.launcher.TaskLauncherEventType;

public class TaskManagerImpl extends CompositeService implements TaskManager {

  private final DaemonContext context;

  private TaskLauncher taskLauncher;
  private final Map<String, TaskBuilder> taskBuilders = new HashMap<>();

  private final ReentrantLock lock = new ReentrantLock();
  private final Map<String, UserApp> apps = new HashMap<>();

  public TaskManagerImpl(DaemonContext context, Configuration conf) {
    super("TaskManagerImpl");
    this.context = context;
    taskLauncher = new TaskLauncher(context, conf);
  }

  @Override
  protected void serviceInit(Configuration conf) throws Exception {
    info("Init service '{}'", this.getClass().getSimpleName());
    context.getDispatcher().register(TaskLauncherEventType.class, taskLauncher);
    addService(taskLauncher);
    super.serviceInit(conf);
  }

  @Override
  protected void serviceStart() throws Exception {
    info("Successfully started service '{}'", this.getClass().getSimpleName());
    super.serviceStart();
  }

  @Override
  protected void serviceStop() throws Exception {
    info("Successfully stopped service '{}'", this.getClass().getSimpleName());
    super.serviceStop();
  }

  // -- TaskManager machinery --

  @Override
  public Task createTask(ApplicationId daemonAppId, String userAppId,
      Worker worker, String workloadId) {
    Task task = getTaskBuilder(workloadId).createTask(context, daemonAppId,
        userAppId, worker);

    info(
        "Create a task {} for application {} (userAppId: {}) "
            + "with accelerator (type: {}, workload: {})",
        task.getTaskId().getId(), daemonAppId, userAppId, worker, workloadId);

    lock.lock();
    try {
      if (null == context.getUserApps().get(userAppId)) {
        UserAppImpl application =
            new UserAppImpl(userAppId, context.getDispatcher());
        context.getUserApps().put(userAppId, application);
      }
    } finally {
      lock.unlock();
    }

    context.getUserApps().get(userAppId).startTask(task);

    return task;
  }

  /**
   * Get or create a {@link TaskBuilder task builder} instance for creating
   * tasks which have the same computational logic. Note: different workloads
   * have different `TaskBuilder` instances.
   *
   * @param workloadId computational logic
   * @return a {@link TaskBuilder task builder} instance for creating tasks
   *         which have the same computational logic
   */
  public synchronized TaskBuilder getTaskBuilder(String workloadId) {
    Preconditions.checkNotNull(workloadId);

    TaskBuilder taskBuilder = taskBuilders.get(workloadId);
    if (taskBuilder == null) {
      taskBuilder = new TaskBuilder(workloadId);
      taskBuilders.put(workloadId, taskBuilder);
    } else {
      taskBuilder.incCount();
    }
    info("Get or create {}", taskBuilder);

    return taskBuilder;
  }

  @Override
  public Map<String, UserApp> getApplications() {
    return apps;
  }
}
