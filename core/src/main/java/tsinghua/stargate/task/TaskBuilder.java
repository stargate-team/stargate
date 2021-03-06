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

import java.util.concurrent.atomic.AtomicInteger;

import tsinghua.stargate.DaemonContext;
import tsinghua.stargate.Log;
import tsinghua.stargate.rpc.message.entity.ApplicationId;
import tsinghua.stargate.rpc.message.entity.Worker;

/** A builder for creating tasks. */
public class TaskBuilder extends Log {

  private String workloadId;

  private int refCount = 1;

  private AtomicInteger nextTaskId = new AtomicInteger(0);

  TaskBuilder(String workloadId) {
    this.workloadId = workloadId;
  }

  /**
   * Create a new {@link Task task}. Note: each task has the unique
   * {@link TaskId task id} but can have the same {@code userAppId}, e.g., a
   * spark job can be divided into multiple tasks which have the unique task id
   * but have the same application id.
   * 
   * @param context a `StarGateDaemon` context
   * @param daemonAppId an app id auto-generated by `StarGateDaemon`
   * @param userAppId an app id provided by user
   * @param worker the newly created task's target platform for execution
   * @return a new {@link Task task} instance
   */
  Task createTask(DaemonContext context, ApplicationId daemonAppId,
      String userAppId, Worker worker) {
    return new TaskImpl(context, userAppId, daemonAppId, worker, workloadId,
        getNextTaskId());
  }

  /**
   * Get a new task id.
   *
   * @return a new task id.
   */
  private int getNextTaskId() {
    return nextTaskId.getAndIncrement();
  }

  synchronized void incCount() {
    refCount++;
  }

  @Override
  public String toString() {
    return "TaskBuilder for workload: " + workloadId;
  }
}
