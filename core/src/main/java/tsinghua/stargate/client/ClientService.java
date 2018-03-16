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

package tsinghua.stargate.client;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.thrift.TException;

import tsinghua.stargate.DaemonContext;
import tsinghua.stargate.StarGateDaemon;
import tsinghua.stargate.app.AppManager;
import tsinghua.stargate.app.DaemonApp;
import tsinghua.stargate.conf.Configuration;
import tsinghua.stargate.conf.NameSpace;
import tsinghua.stargate.exception.StarGateException;
import tsinghua.stargate.rpc.ApplicationStarGateProtocol;
import tsinghua.stargate.rpc.RPC;
import tsinghua.stargate.rpc.message.*;
import tsinghua.stargate.rpc.message.entity.*;
import tsinghua.stargate.rpc.workhorse.RpcServer;
import tsinghua.stargate.scheduler.AcceleratorReport;
import tsinghua.stargate.service.AbstractService;
import tsinghua.stargate.util.ReflectionUtils;
import tsinghua.stargate.util.Utils;

/**
 * The client interface to the {@link StarGateDaemon server}. This module
 * handles all rpc client interfaces to server.
 */
public class ClientService extends AbstractService
    implements ApplicationStarGateProtocol {

  final private AtomicInteger applicationCounter = new AtomicInteger(0);
  private final AppManager appManager;
  private Configuration conf;
  private long timestamp;
  private RpcServer server;
  private DaemonContext context;

  public ClientService(DaemonContext context, AppManager appManager) {
    super("ClientService");
    this.context = context;
    this.appManager = appManager;
  }

  @Override
  protected void serviceInit(Configuration conf) throws Exception {
    info("Init service '{}'", this.getClass().getSimpleName());
    this.conf = conf;
    this.server = getServer();
    super.serviceInit(conf);
  }

  RpcServer getServer() {
    InetSocketAddress address = conf.getSocketAddr(NameSpace.RPC_SERVER_ADDRESS,
        NameSpace.DEFAULT_RPC_SERVER_ADDRESS,
        NameSpace.DEFAULT_RPC_SERVER_PORT);
    int ioQueueSize = conf.getInt(NameSpace.RPC_SERVER_THREAD_IO_QUEUE_SIZE,
        NameSpace.DEFAULT_RPC_SERVER_THREAD_IO_QUEUE_SIZE);
    int ioThreads = conf.getInt(NameSpace.RPC_SERVER_THREAD_IO_COUNT,
        NameSpace.DEFAULT_RPC_SERVER_THREAD_IO_COUNT);
    int workerThreads = conf.getInt(NameSpace.RPC_SERVER_THREAD_WORKER_COUNT,
        NameSpace.DEFAULT_RPC_SERVER_THREAD_WORKER_COUNT);

    return RPC.create(conf).getServer(conf, address, ioQueueSize, ioThreads,
        workerThreads, ApplicationStarGateProtocol.class, this);
  }

  @Override
  protected void serviceStart() throws Exception {
    this.server.start();
    info("Successfully started service '{}'", this.getClass().getSimpleName());
    super.serviceStart();
  }

  @Override
  protected void serviceStop() throws Exception {
    if (this.server != null) {
      this.server.stop();
    }
    info("Successfully stopped service '{}'", this.getClass().getSimpleName());
    super.serviceStop();
  }

  public void setContext(DaemonContext context) {
    this.context = context;
  }

  // -- ApplicationStarGateProtocol machinery --

  @Override
  public GetNewApplicationResponse getNewApplication(
      GetNewApplicationRequest request)
      throws StarGateException, IOException, TException {
    if (request.getAppState() != ApplicationState.NEW) {
      error("Application state should be new");
      return null;
    }

    ApplicationId appId = ApplicationId.newInstance(getTimestamp(),
        applicationCounter.incrementAndGet());
    info("Allocate a new application {}", appId);

    GetNewApplicationResponse response =
        ReflectionUtils.get().getMsg(GetNewApplicationResponse.class);
    response.setApplicationId(appId);
    response.setApplicationState(ApplicationState.GRANT);
    return response;
  }

  private long getTimestamp() {
    timestamp = System.currentTimeMillis();
    return timestamp;
  }

  @Override
  public SubmitApplicationResponse submitApplication(
      SubmitApplicationRequest request)
      throws StarGateException, IOException, TException {
    SubmitApplicationResponse response =
        ReflectionUtils.get().getMsg(SubmitApplicationResponse.class);

    ApplicationSubmissionContext submissionContext =
        request.getAppSubmissionContext();
    if (submissionContext.getApplicationState() != ApplicationState.SUBMIT) {
      error("Application state should be submitted");
      return null;
    }

    AcceleratorResource resource = submissionContext.getAcceleratorResource();
    if (resource.getAcceleratorWorkload() == null) {
      error("User does not provide workload");
      response.setApplicationState(ApplicationState.NONE);
      return response;
    }

    ApplicationLaunchContext launchContext =
        submissionContext.getApplicationLaunchContext();
    if (launchContext.getUserAppId() == null) {
      launchContext.setUserAppId(NameSpace.DEFAULT_APP_NAME);
    }
    if (launchContext.getWorker() == null) {
      launchContext.setWorker(
          Worker.valueOf(NameSpace.DEFAULT_ACCELERATOR_WORKER.toUpperCase()));
    }

    appManager.submitApplication(submissionContext);

    info(
        "Accept application {} (userAppId: {}) with "
            + "accelerator (type: {}, workload: {}, core: {}, maxMemory: {})",
        submissionContext.getApplicationId(), launchContext.getUserAppId(),
        launchContext.getWorker(), resource.getAcceleratorWorkload(),
        resource.getAcceleratorCoreId(),
        Utils.bytes2String(resource.getAcceleratorCoreMemory()));

    response.setApplicationState(ApplicationState.ACCEPT);

    return response;
  }

  @Override
  public GetApplicationReportResponse getApplicationReport(
      GetApplicationReportRequest request)
      throws StarGateException, IOException {
    ApplicationId appId = request.getApplicationId();
    GetApplicationReportResponse response =
        ReflectionUtils.get().getMsg(GetApplicationReportResponse.class);
    DaemonApp daemonApp = context.getDaemonApps().get(appId);
    response.setApplicationReport(daemonApp.generateAppReport());
    return response;
  }

  public CardReport getCardReport(String cardId) throws StarGateException {
    AcceleratorReport acceleratorReport =
        context.getScheduler().getCardReport(cardId);
    CardReport report = ReflectionUtils.get().getMsg(CardReport.class);
    try {
      report.setCardId(cardId);
      report.setHttpAddress(Utils.getLocalHostLANAddress().getHostAddress());
      report.setNumTasks(acceleratorReport.getNum());
      report.setUsedCapability(acceleratorReport.getUsed().getAllResources());
      report.setTotalCapability(acceleratorReport.getTotal().getAllResources());
    } catch (SocketException e) {
      error("get local ip exception");
      throw new StarGateException(e);
    } catch (UnknownHostException e) {
      error("get local ip exception");
      throw new StarGateException(e);
    }
    return report;
  }

}
