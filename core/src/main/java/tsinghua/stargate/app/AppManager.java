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

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import tsinghua.stargate.DaemonContext;
import tsinghua.stargate.Log;
import tsinghua.stargate.rpc.message.entity.*;
import tsinghua.stargate.task.TaskManagerImpl;

/**
 * A manager for maintaining a list of `StarGateDaemon` applications.
 */
@SuppressWarnings("unchecked")
public class AppManager extends Log {

  private final ConcurrentHashMap<ApplicationId, DaemonApp> applications =
      new ConcurrentHashMap<>();

  private DaemonContext context;
  private TaskManagerImpl taskManager;

  public AppManager(DaemonContext context, TaskManagerImpl taskManager) {
    this.context = context;
    this.taskManager = taskManager;
  }

  public void submitApplication(
      ApplicationSubmissionContext submissionContext) {
    createDaemonApp(submissionContext);
    ApplicationId daemonAppId = submissionContext.getApplicationId();
    context.getDispatcher().getEventHandler()
        .handle(new DaemonAppEvent(daemonAppId, DaemonAppEventType.START));
  }

  // TODO: Assembly all application information for showing and monitoring.
  private DaemonApp createDaemonApp(
      ApplicationSubmissionContext submissionContext) {
    ApplicationId daemonAppId = submissionContext.getApplicationId();
    AcceleratorResource resource = submissionContext.getAcceleratorResource();
    ApplicationLaunchContext launchContext =
        submissionContext.getApplicationLaunchContext();

    String userAppId = launchContext.getUserAppId();
    Worker worker = launchContext.getWorker();
    ServiceData inputServiceData = launchContext.getInputServiceData();
    ServiceData outputServiceData = launchContext.getOutputServiceData();
    Map<String, String> resources = launchContext.getResources();
    Map<String, String> environments = launchContext.getEnvironments();
    Map<String, String> processors = launchContext.getServiceDataProcessor();

    DaemonApp daemonApp = new DaemonAppImpl(daemonAppId, taskManager, context,
        resource, userAppId, worker, inputServiceData, outputServiceData,
        processors, environments, resources);

    if (context.getDaemonApps().putIfAbsent(daemonAppId, daemonApp) != null) {
      warn("Found duplicated application {}", daemonAppId);
    }

    return daemonApp;
  }

  public ConcurrentHashMap<ApplicationId, DaemonApp> getApplications() {
    return applications;
  }
}
