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

package tsinghua.stargate.task.launcher;

import java.util.concurrent.Callable;

import tsinghua.stargate.DaemonContext;
import tsinghua.stargate.task.Task;
import tsinghua.stargate.task.TaskFailedEvent;
import tsinghua.stargate.task.TaskFinishedEvent;
import tsinghua.stargate.task.TaskState;

@SuppressWarnings("unchecked")
public class CallableTask implements Callable<TaskState> {

  private final DaemonContext context;
  private final Task task;

  CallableTask(DaemonContext context, Task task) {
    this.context = context;
    this.task = task;
  }

  @Override
  public TaskState call() throws Exception {
    TaskState state = task.run();
    if (state == TaskState.EXITED_WITH_SUCCESS) {
      context.getDispatcher().getEventHandler()
          .handle(new TaskFinishedEvent(task.getTaskId()));
    } else {
      String diagnostics = task.getDiagnostics();
      context.getDispatcher().getEventHandler()
          .handle(new TaskFailedEvent(task.getTaskId(), diagnostics));
    }
    return state;
  }

  public Task getTask() {
    return task;
  }
}
