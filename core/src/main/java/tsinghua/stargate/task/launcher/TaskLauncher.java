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

import java.util.concurrent.*;

import tsinghua.stargate.DaemonContext;
import tsinghua.stargate.conf.Configuration;
import tsinghua.stargate.conf.NameSpace;
import tsinghua.stargate.event.EventHandler;
import tsinghua.stargate.service.AbstractService;
import tsinghua.stargate.task.Task;
import tsinghua.stargate.task.TaskState;
import tsinghua.stargate.util.ThreadUtils;

public class TaskLauncher extends AbstractService
    implements EventHandler<TaskLauncherEvent> {

  private final DaemonContext context;
  private final Configuration conf;

  private final LauncherThread launcherThread;
  private final ExecutorService launchPool;
  private final BlockingQueue<FutureTask> launchEvents =
      new LinkedBlockingQueue<>();

  public TaskLauncher(DaemonContext context, Configuration conf) {
    super("TaskLauncher");
    this.context = context;
    this.conf = conf;
    launcherThread = new LauncherThread();
    launchPool = createLaunchPool();
  }

  private ExecutorService createLaunchPool() {
    int launchThreads = conf.getInt(NameSpace.TASK_THREAD_COUNT,
        NameSpace.DEFAULT_TASK_THREAD_COUNT);
    return ThreadUtils.getExecutor().newCachedThreadPool("TaskLauncher",
        launchThreads, 1L, TimeUnit.HOURS);
  }

  @Override
  protected void serviceInit(Configuration conf) throws Exception {
    info("Init service '{}'", this.getClass().getSimpleName());
    super.serviceInit(conf);
  }

  @Override
  protected void serviceStart() throws Exception {
    launcherThread.start();
    info("Successfully started service '{}'", this.getClass().getSimpleName());
    super.serviceStart();
  }

  @Override
  protected void serviceStop() throws Exception {
    launcherThread.cancel();
    try {
      launcherThread.join();
    } catch (InterruptedException e) {
      warn(launcherThread.getName() + " interrupted during join", e);
    }
    launchPool.shutdownNow();
    info("Successfully stopped service '{}'", this.getClass().getSimpleName());
    super.serviceStop();
  }

  @Override
  public void handle(TaskLauncherEvent event) {
    Task task = event.getTask();
    switch (event.getType()) {
    case LAUNCH_TASK:
      launch(task);
      break;

    case CLEANUP_TASK:
      cleanup(task);
      break;

    default:
      break;
    }
  }

  private void launch(Task task) {
    launchEvents.add(createFutureTask(task));
  }

  private void cleanup(Task task) {
    launchEvents.add(createFutureTask(task));
  }

  private FutureTask createFutureTask(Task task) {
    CallableTask callableTask = new CallableTask(context, task);
    return new FutureTask<>(callableTask);
  }

  class LauncherThread extends Thread {

    LauncherThread() {
      super("Launcher Thread");
      setDaemon(true);
    }

    @Override
    public void run() {
      while (!Thread.currentThread().isInterrupted()) {
        FutureTask toLaunch = null;
        try {
          toLaunch = launchEvents.take();
          if (toLaunch != null) {
            launchPool.submit(toLaunch);
          }
        } catch (InterruptedException e) {
          warn("Interrupted when {} waiting for launching new task", getName());
          try {
            if (toLaunch != null) {
              TaskState state = (TaskState) toLaunch.get();
              trace("Tracing current running task (state: {})", state);
            }
          } catch (InterruptedException ie) {
            warn("Interrupted when {} trying to get task result", getName());
          } catch (ExecutionException ee) {
            error("Running task launched by {} failed to complete computing",
                getName());
          }
          Thread.currentThread().interrupt();
        }
      }
    }

    void cancel() {
      interrupt();
    }
  }
}
