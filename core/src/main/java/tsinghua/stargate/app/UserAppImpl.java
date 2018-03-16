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

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import tsinghua.stargate.event.Dispatcher;
import tsinghua.stargate.scheduler.Scheduler;
import tsinghua.stargate.scheduler.TaskAddedSchedulerEvent;
import tsinghua.stargate.task.Task;

@SuppressWarnings("unchecked")
public class UserAppImpl implements UserApp {

  private final ConcurrentMap<String, Task> tasks = new ConcurrentHashMap<>();

  private final Dispatcher dispatcher;
  private final String appId;

  private volatile Task currentTask;

  private Scheduler scheduler;

  public UserAppImpl(String appId, Dispatcher dispatcher) {
    this.dispatcher = dispatcher;
    this.appId = appId;
  }

  @Override
  public String getUserAppId() {
    return appId;
  }

  @Override
  public String getApplicationState() {
    return null;
  }

  @Override
  public void startTask(Task task) {
    addAcceleratorTask(task);
    dispatcher.getEventHandler()
        .handle(new TaskAddedSchedulerEvent(task.getTaskId()));
  }

  /**
   * add accelerator Task to application
   *
   * @param task TaskImpl
   */
  private void addAcceleratorTask(Task task) {
    tasks.putIfAbsent(task.getTaskId().getId(), task);
    currentTask = task;
  }

  @Override
  public Task getTask(String taskId) {
    return tasks.get(taskId);
  }

  @Override
  public void removeTask(String taskId) {
    tasks.remove(taskId);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    UserAppImpl that = (UserAppImpl) o;

    return appId != null ? appId.equals(that.appId) : that.appId == null;
  }

  @Override
  public int hashCode() {
    return appId != null ? appId.hashCode() : 0;
  }
}
