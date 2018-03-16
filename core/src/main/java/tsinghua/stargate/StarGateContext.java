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

import java.io.IOException;
import java.util.Map;

import org.apache.thrift.TException;

import tsinghua.stargate.client.Application;
import tsinghua.stargate.client.Client;
import tsinghua.stargate.conf.StarGateConf;
import tsinghua.stargate.exception.StarGateException;
import tsinghua.stargate.exception.StarGateRuntimeException;
import tsinghua.stargate.rpc.message.entity.*;
import tsinghua.stargate.util.AcceleratorResourceUtils;
import tsinghua.stargate.util.ReflectionUtils;
import tsinghua.stargate.util.Utils;

public class StarGateContext extends Log {

  private StarGateConf sgConf;
  private String appName;
  private Worker worker;
  private int workerCapacity;
  private String workload;
  private Map<String, String> processors;
  private ServiceData inServiceData;
  private ServiceData outServiceData;
  private Map<String, String> resources;

  private static Client client;
  private Application app;
  private ApplicationId appId;
  private ApplicationLaunchContext appLaunchContext;
  private ApplicationSubmissionContext appSubmissionContext;
  private ApplicationReport report;

  private boolean success;
  private static final int POLL_TIME_INTERVAL_MILLIS = 1000;

  public StarGateContext(StarGateConf sgConf) {
    this.sgConf = sgConf;
    appName = sgConf.getAppName();
    worker = sgConf.getWorker();
    workerCapacity = sgConf.getWorkerCapacity();
    workload = sgConf.getWorkload();
    processors = sgConf.getProcessor();
    inServiceData = sgConf.getInSD();
    outServiceData = sgConf.getOutSD();
    resources = sgConf.getResource();
  }

  public StarGateContext waitForCompletion() throws StarGateException {
    // Construct a client for accessing StarGateDaemon.
    if (null == client) {
      createAndStartClient();
    }

    // Create a new StarGateDaemon application.
    try {
      app = client.createApplication();
      appId = app.getNewApplicationResponse().getApplicationId();
      appLaunchContext = createLaunchContext();
      appSubmissionContext = createSubmissionContext(app, appLaunchContext);
    } catch (StarGateException e) {
      // TODO: Add more debug information
      error("Failed to build remote application");
      throw new StarGateRuntimeException(e);
    } catch (IOException e) {
      error("Failed to build remote application");
      throw new StarGateRuntimeException(e);
    } catch (TException e) {
      error("Failed to build remote application");
      throw new StarGateRuntimeException(e);
    }

    // Submit the new StarGateDaemon application.
    try {
      client.submitApplication(appSubmissionContext);
      report = client.getApplicationReport(appId);
    } catch (IOException e) {
      error("Failed to contact StarGate for application {}.", appId, e);
      throw new StarGateException(e);
    } catch (TException e) {
      error("Failed to contact StarGate for application {}.", appId, e);
      throw new StarGateException(e);
    }

    // Poll the report of this new StarGateDaemon application.
    boolean poll = true;
    while (poll) {
      if (report != null) {
        ApplicationState appState = report.getApplicationState();
        info("Application report for {} (state: {})", appId, appState);
        switch (appState) {
        case RUNNING:
          ApplicationResourceUsageReport resourceUsageReport =
              report.getApplicationResourceUsageReport();
          AcceleratorResource resource =
              resourceUsageReport.getAcceleratorResource();
          info(
              "Running on accelerator\n\tid: {}\n\ttype: {}"
                  + "\n\tworkload: {}\n\tcore: {}\n\tmaxMemory: {}",
              resourceUsageReport.getHardwareId(),
              resourceUsageReport.getWorker(),
              resource.getAcceleratorWorkload(),
              resource.getAcceleratorCoreId(),
              Utils.bytes2String(resource.getAcceleratorCoreMemory()));
          break;
        case FINISHED:
          poll = false;
          success = true;
          info("Successfully finished application {}", appId);
          break;
        case FAILED:
          if (report.getDiagnostics() != null) {
            info("Failed to run application {} with diagnostics: {}", appId,
                report.getDiagnostics());
          }
          poll = false;
          success = false;
          break;
        default:
          break;
        }

        try {
          Thread.sleep(POLL_TIME_INTERVAL_MILLIS);
          report = client.getApplicationReport(appId);
        } catch (InterruptedException e) {
          error("Interrupted while getting application report for {}", appId,
              e);
          Thread.currentThread().interrupt();
        } catch (TException e) {
          error("Failed to get application report for {}.", appId, e);
          throw new StarGateException(e);
        } catch (IOException e) {
          error("Failed to get application report for {}.", appId, e);
          throw new StarGateException(e);
        }
      }
    }

    return this;
  }

  // Create and start RPC client proxy
  private static void createAndStartClient() {
    StarGateConf conf = new StarGateConf();
    client = Client.create();
    client.init(conf);
    client.start();
  }

  // Create a context for launching StarGateDamon application
  private ApplicationLaunchContext createLaunchContext() throws IOException {
    final ApplicationLaunchContext alc =
        ReflectionUtils.get().getMsg(ApplicationLaunchContext.class);
    alc.setUserAppId(this.appName);
    alc.setWorker(this.worker);
    alc.setInputServiceData(this.inServiceData);
    alc.setOutputServiceData(this.outServiceData);
    alc.setResources(this.resources);
    alc.setServiceDataProcessor(this.processors);
    return alc;
  }

  // Create a context for submitting the new StarGateDamon application
  private ApplicationSubmissionContext createSubmissionContext(Application app,
      ApplicationLaunchContext launchContext) {
    final ApplicationId appId =
        app.getNewApplicationResponse().getApplicationId();

    final ApplicationSubmissionContext asc =
        app.getApplicationSubmissionContext();
    asc.setApplicationId(appId);
    asc.setApplicationLaunchContext(launchContext);
    asc.setAcceleratorResource(AcceleratorResourceUtils.newInstance(workload));
    return asc;
  }

  public String getAppId() {
    return appId.toString();
  }

  public boolean isSuccess() {
    return success;
  }
}
