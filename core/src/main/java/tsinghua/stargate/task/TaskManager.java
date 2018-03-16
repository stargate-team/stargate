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

import java.util.Map;

import tsinghua.stargate.app.UserApp;
import tsinghua.stargate.rpc.message.entity.ApplicationId;
import tsinghua.stargate.rpc.message.entity.Worker;

/**
 * A manager for managing accelerator scheduleTasks.
 */
public interface TaskManager {

  /**
   * create accelerator task and application add the accelerator task into
   * application
   *
   * @param daemonAppId Application Id generate by stargate
   * @param workloadId accelerator work load
   * @param worker accelerator worker type
   * @param appId user application id
   * @return accelerator task created
   */
  Task createTask(ApplicationId daemonAppId, String appId, Worker worker,
      String workloadId);

  /**
   * Get the builder for constructing accelerator scheduleTasks.
   *
   * @param workloadId the id accelerator workload
   * @return the builder for constructing accelerator scheduleTasks
   */
  TaskBuilder getTaskBuilder(String workloadId);

  /**
   * Get the accelerator applications submitted by user.
   *
   * @return the accelerator applications submitted by user
   */
  Map<String, UserApp> getApplications();
}
