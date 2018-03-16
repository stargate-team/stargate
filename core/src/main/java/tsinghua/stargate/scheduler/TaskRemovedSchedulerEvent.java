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

/**
 * Trigger this event when a task is removed or killed by {@code UserAppImpl}.
 */
public class TaskRemovedSchedulerEvent extends SchedulerEvent {

  private final String taskId;
  private final String applicationId;

  public TaskRemovedSchedulerEvent(String applicationId, String taskId) {
    super(SchedulerEventType.TASK_REMOVED);
    this.applicationId = applicationId;
    this.taskId = taskId;
  }

  public String getTaskId() {
    return taskId;
  }

  public String getApplicationId() {
    return applicationId;
  }
}
