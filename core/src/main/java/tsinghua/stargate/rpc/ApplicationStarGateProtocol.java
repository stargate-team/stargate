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

package tsinghua.stargate.rpc;

import java.io.IOException;

import org.apache.thrift.TException;

import tsinghua.stargate.exception.StarGateException;
import tsinghua.stargate.rpc.message.*;
import tsinghua.stargate.rpc.message.entity.AcceleratorResource;
import tsinghua.stargate.rpc.message.entity.ApplicationId;
import tsinghua.stargate.rpc.message.entity.ApplicationLaunchContext;
import tsinghua.stargate.rpc.message.entity.ApplicationReport;

/**
 * Protocol between applications and {@code StarGateDaemon ANM} which is an API
 * for clients who want to employ StarGate services.
 *
 * <p>
 * This API contains methods of creating/submitting ANM applications and
 * retrieving report for them.
 */
public interface ApplicationStarGateProtocol {

  /**
   * Method for client to register a new application with ANM.
   *
   * <p>
   * ANM responds with a new, monotonically increasing, {@link ApplicationId}
   * for submitting a new application.
   *
   * <p>
   * ANM also responds with details such as maximum accelerator capabilities in
   * this node as specified in {@link GetNewApplicationResponse}.
   *
   * @param request to register a new application with ANM
   * @return response containing the new {@code ApplicationId} to be used to
   *         submit an application
   * @throws StarGateException
   * @throws IOException
   * @throws TException
   * @see GetNewApplicationRequest
   * @see GetNewApplicationResponse
   */
  GetNewApplicationResponse getNewApplication(GetNewApplicationRequest request)
      throws StarGateException, IOException, TException;

  /**
   * Method for client to submit application to ANM.
   *
   * <p>
   * Client is responsible for providing the manifest of submitting application,
   * such as {@link AcceleratorResource AcceleratorResource} required to run the
   * application, {@link ApplicationLaunchContext ApplicationLaunchContext} for
   * launching the submitted application, using {@link SubmitApplicationRequest
   * SubmitApplicationRequest}.
   *
   * <p>
   * Currently ANM sends an immediate (empty) {@link SubmitApplicationResponse
   * SubmitApplicationResponse} on accepting the submission. For retrieving app
   * state, employ {@link #getApplicationReport(GetApplicationReportRequest)
   * getApplicationReport(GetApplicationReportRequest)} method in a polling way.
   *
   * <p>
   * During submission stage, ANM checks the existence of app. If so, it will
   * simply return {@code SubmitApplicationResponse SubmitApplicationResponse}.
   *
   * @param request to submit application to ANM
   * @return (empty) response on accepting the submission
   * @throws StarGateException
   * @throws IOException
   * @throws TException
   * @see SubmitApplicationRequest
   * @see SubmitApplicationResponse
   * @see #getNewApplication(GetNewApplicationRequest)
   * @see #getApplicationReport(GetApplicationReportRequest)
   */
  SubmitApplicationResponse submitApplication(SubmitApplicationRequest request)
      throws StarGateException, IOException, TException;

  /**
   * Method for client to get the report for application from ANM.
   *
   * <p>
   * Client pads {@link GetApplicationReportRequest GetApplicationReportRequest}
   * with app's {@link ApplicationId ApplicationId} for retrieving its report.
   *
   * <p>
   * ANM responds with a {@link ApplicationReport ApplicationReport} wrapped by
   * {@link GetApplicationReportResponse GetApplicationReportResponse}.
   *
   *
   * @param request to retrieve report for application
   * @return response containing report for application
   * @throws StarGateException
   * @throws IOException
   * @throws TException
   * @see #submitApplication(SubmitApplicationRequest)
   */
  GetApplicationReportResponse getApplicationReport(
      GetApplicationReportRequest request)
      throws StarGateException, IOException, TException;

}
