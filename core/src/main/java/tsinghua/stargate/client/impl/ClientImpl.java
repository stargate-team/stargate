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

package tsinghua.stargate.client.impl;

import java.io.IOException;

import org.apache.thrift.TException;

import tsinghua.stargate.client.Application;
import tsinghua.stargate.client.Client;
import tsinghua.stargate.client.proxy.ClientProxy;
import tsinghua.stargate.conf.Configuration;
import tsinghua.stargate.exception.StarGateException;
import tsinghua.stargate.exception.StarGateRuntimeException;
import tsinghua.stargate.rpc.ApplicationStarGateProtocol;
import tsinghua.stargate.rpc.message.*;
import tsinghua.stargate.rpc.message.entity.ApplicationId;
import tsinghua.stargate.rpc.message.entity.ApplicationReport;
import tsinghua.stargate.rpc.message.entity.ApplicationState;
import tsinghua.stargate.rpc.message.entity.ApplicationSubmissionContext;
import tsinghua.stargate.rpc.workhorse.RpcManager;
import tsinghua.stargate.util.ReflectionUtils;

/** The implementation of {@link Client Client}. */
public class ClientImpl extends Client {

  private ApplicationStarGateProtocol sgdClient;

  public ClientImpl() {
    super("ClientImpl");
  }

  @Override
  protected void serviceInit(Configuration conf) throws Exception {
    info("Init service '{}'", this.getClass().getSimpleName());
    super.serviceInit(conf);
  }

  @Override
  protected void serviceStart() throws Exception {
    sgdClient =
        ClientProxy.createProxy(getConfig(), ApplicationStarGateProtocol.class);
    info("Successfully started service '{}'", this.getClass().getSimpleName());
    super.serviceStart();
  }

  @Override
  protected void serviceStop() throws Exception {
    if (sgdClient != null) {
      RpcManager.stopProxy(sgdClient);
    }
    info("Successfully stopped service '{}'", this.getClass().getSimpleName());
    super.serviceStop();
  }

  @Override
  public Application createApplication()
      throws StarGateException, IOException, TException {
    ApplicationSubmissionContext context =
        ReflectionUtils.get().getMsg(ApplicationSubmissionContext.class);
    GetNewApplicationResponse response = getNewApplication();
    return new Application(response, context);
  }

  private GetNewApplicationResponse getNewApplication()
      throws IOException, StarGateException, TException {
    GetNewApplicationRequest request =
        ReflectionUtils.get().getMsg(GetNewApplicationRequest.class);
    if (request.getAppState() != ApplicationState.NEW
        || request.getAppState() == null) {
      request.setAppState(ApplicationState.NEW);
    }
    return sgdClient.getNewApplication(request);
  }

  @Override
  public ApplicationState submitApplication(
      ApplicationSubmissionContext appSubmissionContext)
      throws StarGateException, IOException, TException {

    if (appSubmissionContext.getApplicationId() == null) {
      throw new StarGateRuntimeException(
          "ApplicationId is not provided in ApplicationSubmissionContext");
    }

    if (appSubmissionContext.getApplicationState() == null
        || appSubmissionContext
            .getApplicationState() != ApplicationState.SUBMIT) {
      appSubmissionContext.setApplicationState(ApplicationState.SUBMIT);
    }

    SubmitApplicationRequest request =
        ReflectionUtils.get().getMsg(SubmitApplicationRequest.class);
    request.setAppSubmissionContext(appSubmissionContext);

    SubmitApplicationResponse response = sgdClient.submitApplication(request);
    return response.getApplicationState();
  }

  @Override
  public ApplicationReport getApplicationReport(ApplicationId appId)
      throws StarGateException, IOException, TException {
    GetApplicationReportResponse response;

    GetApplicationReportRequest request =
        ReflectionUtils.get().getMsg(GetApplicationReportRequest.class);
    request.setApplicationId(appId);
    response = sgdClient.getApplicationReport(request);
    return response.getApplicationReport();
  }
}