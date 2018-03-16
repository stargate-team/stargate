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

package tsinghua.stargate;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import tsinghua.stargate.app.DaemonApp;
import tsinghua.stargate.app.UserApp;
import tsinghua.stargate.client.ClientService;
import tsinghua.stargate.event.Dispatcher;
import tsinghua.stargate.rpc.message.entity.ApplicationId;
import tsinghua.stargate.scheduler.Scheduler;
import tsinghua.stargate.storage.BlockManagerService;
import tsinghua.stargate.task.TaskManager;

/**
 * This interface is used for sharing information across components in the
 * {@code StarGateDaemon}.
 */
public interface DaemonContext {

  Dispatcher getDispatcher();

  Scheduler getScheduler();

  BlockManagerService getBlockManagerService();

  TaskManager getTaskManager();

  Map<String, UserApp> getUserApps();

  ConcurrentHashMap<ApplicationId, DaemonApp> getDaemonApps();

  ClientService getClientService();
}
