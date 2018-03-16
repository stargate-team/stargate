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

package tsinghua.stargate.app;

import tsinghua.stargate.event.EventHandler;
import tsinghua.stargate.rpc.message.entity.ApplicationId;
import tsinghua.stargate.rpc.message.entity.ApplicationReport;
import tsinghua.stargate.task.Task;
import tsinghua.stargate.task.TaskId;

/**
 * The interface to an Application in the StarGateDaemon. Take a look at
 * {@link DaemonAppImpl} for its implementation. This interface exposes methods
 * to access various updates in application status/report.
 */
public interface DaemonApp extends EventHandler<DaemonAppEvent> {

  /**
   * The application id for this {@link DaemonApp}.
   *
   * @return the {@link ApplicationId} for this {@link DaemonApp}.
   */
  ApplicationId getApplicationId();

  /**
   * To get the status of an application in the ANM, this method can be used.
   *
   * @return the {@link ApplicationReport} detailing the status of the
   *         application.
   */
  ApplicationReport generateAppReport();

  /**
   * {@link DaemonApp} can have multiple accelerator tasks {@link Task}. This
   * method returns the all {@link Task}s for the DaemonApp.
   *
   * @param taskId the accelerator task Id
   * @return the {@link Task} corresponding to the {@link TaskId}
   */
  Task getAcceleratorTask(TaskId taskId);
}
