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

import tsinghua.stargate.app.AppManager;
import tsinghua.stargate.app.DaemonApp;
import tsinghua.stargate.app.UserApp;
import tsinghua.stargate.client.ClientService;
import tsinghua.stargate.event.Dispatcher;
import tsinghua.stargate.rpc.message.entity.ApplicationId;
import tsinghua.stargate.scheduler.Scheduler;
import tsinghua.stargate.storage.BlockManagerService;
import tsinghua.stargate.task.TaskManager;

public class DaemonContextImpl implements DaemonContext {

  private Dispatcher dispatcher;
  private Scheduler scheduler;
  private BlockManagerService blockManagerService;
  private TaskManager taskManager;
  private AppManager appManager;
  private ClientService clientService;

  @Override
  public Dispatcher getDispatcher() {
    return this.dispatcher;
  }

  public void setDispatcher(Dispatcher dispatcher) {
    this.dispatcher = dispatcher;
  }

  @Override
  public Scheduler getScheduler() {
    return scheduler;
  }

  public void setScheduler(Scheduler scheduler) {
    this.scheduler = scheduler;
  }

  @Override
  public BlockManagerService getBlockManagerService() {
    return blockManagerService;
  }

  void setBlockManagerService(BlockManagerService blockManagerService) {
    this.blockManagerService = blockManagerService;
  }

  @Override
  public TaskManager getTaskManager() {
    return taskManager;
  }

  public void setTaskManager(TaskManager taskManager) {
    this.taskManager = taskManager;
  }

  @Override
  public Map<String, UserApp> getUserApps() {
    return taskManager.getApplications();
  }

  public void setAppManager(AppManager appManager) {
    this.appManager = appManager;
  }

  @Override
  public ConcurrentHashMap<ApplicationId, DaemonApp> getDaemonApps() {
    return appManager.getApplications();
  }

  @Override
  public ClientService getClientService() {
    return this.clientService;
  }

  public void setClientService(ClientService clientService) {
    this.clientService = clientService;
  }
}