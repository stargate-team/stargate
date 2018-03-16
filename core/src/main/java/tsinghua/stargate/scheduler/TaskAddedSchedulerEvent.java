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

import tsinghua.stargate.task.TaskId;

/**
 * Trigger this event when a task is added into {@code UserAppImpl}.
 */
public class TaskAddedSchedulerEvent extends SchedulerEvent {

  private final TaskId taskId;

  public TaskAddedSchedulerEvent(TaskId taskId) {
    super(SchedulerEventType.TASK_ADDED);
    this.taskId = taskId;
  }

  TaskId getTaskId() {
    return taskId;
  }
}
