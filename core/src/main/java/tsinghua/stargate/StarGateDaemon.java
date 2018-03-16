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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import tsinghua.stargate.app.AppManager;
import tsinghua.stargate.app.DaemonApp;
import tsinghua.stargate.app.DaemonAppEvent;
import tsinghua.stargate.app.DaemonAppEventType;
import tsinghua.stargate.client.ClientService;
import tsinghua.stargate.conf.Configuration;
import tsinghua.stargate.conf.StarGateConf;
import tsinghua.stargate.event.AsyncDispatcher;
import tsinghua.stargate.event.Dispatcher;
import tsinghua.stargate.event.EventHandler;
import tsinghua.stargate.rpc.message.entity.ApplicationId;
import tsinghua.stargate.scheduler.FairScheduler;
import tsinghua.stargate.scheduler.SchedulableTask;
import tsinghua.stargate.scheduler.SchedulerEventType;
import tsinghua.stargate.service.CompositeService;
import tsinghua.stargate.service.Service;
import tsinghua.stargate.storage.BlockFetchEventType;
import tsinghua.stargate.storage.BlockManagerService;
import tsinghua.stargate.task.*;
import tsinghua.stargate.util.ShutdownHookManager;
import tsinghua.stargate.util.Utils;

/**
 * A daemon for managing all accelerator resources of a single node and
 * accepting user applications' submission.
 *
 * <p>
 * When users submit applications to {@link StarGateDaemon}, these applications
 * will be transform into schedulable tasks and assigned to corresponding
 * accelerator workers, e.g., FPGAs, ASICs. As a consequence, each accelerator
 * worker holds and maintains a task queue.
 *
 * <p>
 * {@link TaskBuilder A task builder} is responsible for converting applications
 * to {@link SchedulableTask schedulable tasks} which will be scheduled and
 * launched by {@link FairScheduler} using a scheduling algorithm.
 *
 * <p>
 * Before running a task, its associated data, computational logic and resources
 * will be downloaded and localized. After complete localizing step,
 * {@link TaskImpl} post ready tasks for execution on accelerator workers.
 */
public class StarGateDaemon extends CompositeService {

  private static final Logger LOG =
      LoggerFactory.getLogger(StarGateDaemon.class);

  /** Priority of the StarGateDaemon shutdown hook. */
  private static final int SHUTDOWN_HOOK_PRIORITY = 30;

  private Configuration conf;

  private Dispatcher dispatcher;

  private DaemonContextImpl context;

  private ClientService clientService;

  private TaskManagerImpl taskManager;

  private FairScheduler fairScheduler;

  private BlockManagerService blockManagerService;

  private AppManager appManager;

  public StarGateDaemon() {
    super("StarGateDaemon");
  }

  @Override
  protected void serviceInit(Configuration conf) throws Exception {
    info("Init service '{}'", this.getClass().getSimpleName());

    this.conf = conf;

    context = new DaemonContextImpl();

    dispatcher = createDispatcher();
    addService((Service) dispatcher);
    context.setDispatcher(dispatcher);

    fairScheduler = createFairScheduler();
    fairScheduler.loadResources(conf);
    addService(fairScheduler);
    context.setScheduler(fairScheduler);

    blockManagerService = createBlockManagerService();
    addService(blockManagerService);
    context.setBlockManagerService(blockManagerService);

    taskManager = createTaskManager();
    addService(taskManager);
    context.setTaskManager(taskManager);

    appManager = createAppManager();
    addIfService(appManager);
    context.setAppManager(appManager);

    clientService = createClientService();
    addService(clientService);
    context.setClientService(clientService);

    dispatcher.register(SchedulerEventType.class, fairScheduler);
    dispatcher.register(BlockFetchEventType.class, blockManagerService);
    dispatcher.register(TaskEventType.class, new TaskEventDispatcher(context));
    dispatcher.register(DaemonAppEventType.class,
        new DaemonAppEventDispatcher(context));

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

  private Dispatcher createDispatcher() {
    return new AsyncDispatcher();
  }

  private FairScheduler createFairScheduler() {
    return new FairScheduler(context);
  }

  private BlockManagerService createBlockManagerService() {
    return new BlockManagerService(context);
  }

  private TaskManagerImpl createTaskManager() {
    return new TaskManagerImpl(context, conf);
  }

  private AppManager createAppManager() {
    return new AppManager(context, taskManager);
  }

  private ClientService createClientService() {
    return new ClientService(context, appManager);
  }

  public static final class DaemonAppEventDispatcher
      implements EventHandler<DaemonAppEvent> {

    private final DaemonContext context;

    private DaemonAppEventDispatcher(DaemonContext context) {
      this.context = context;
    }

    @Override
    public void handle(DaemonAppEvent event) {
      ApplicationId daemonAppId = event.getApplicationId();
      DaemonApp daemonApp = context.getDaemonApps().get(daemonAppId);
      if (daemonApp != null) {
        try {
          daemonApp.handle(event);
        } catch (Throwable t) {
          LOG.error("Error in handling event type {} for application {}",
              event.getType(), daemonAppId, t);
        }
      }
    }
  }

  public static final class TaskEventDispatcher
      implements EventHandler<TaskEvent> {

    private final DaemonContext context;

    private TaskEventDispatcher(DaemonContext context) {
      this.context = context;
    }

    @Override
    public void handle(TaskEvent event) {
      ApplicationId daemonAppId = event.getTaskId().getDaemonAppId();
      DaemonApp daemonApp = context.getDaemonApps().get(daemonAppId);
      TaskId taskId = event.getTaskId();
      if (daemonApp != null) {
        Task task = daemonApp.getAcceleratorTask(taskId);
        if (task != null) {
          try {
            task.handle(event);
          } catch (Throwable t) {
            LOG.error("Error in handling event type {} for application {}",
                event.getType(), daemonAppId, t);
          }
        }
      }
    }
  }

  public DaemonContextImpl getContext() {
    return context;
  }

  public static void main(String[] argv) {
    Utils.startupShutdownMessage(LOG);
    Configuration conf = new StarGateConf();
    StarGateDaemon sgd = new StarGateDaemon();
    ShutdownHookManager.get().addShutdownHook(
        new CompositeServiceShutdownHook(sgd), SHUTDOWN_HOOK_PRIORITY);
    sgd.init(conf);
    sgd.start();
  }
}
