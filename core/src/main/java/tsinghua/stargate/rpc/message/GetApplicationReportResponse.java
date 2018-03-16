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

package tsinghua.stargate.rpc.message;

import tsinghua.stargate.rpc.ApplicationStarGateProtocol;
import tsinghua.stargate.rpc.message.entity.ApplicationReport;
import tsinghua.stargate.util.ReflectionUtils;

/**
 * The response sent by the {@code StarGateDaemon ANM} to a client requesting an
 * application report.
 *
 * <p>
 * The response includes an {@link ApplicationReport} which has details such as
 * user, queue, name, host on which the application is running, RPC port,
 * diagnostics, start-time etc.
 *
 * @see ApplicationReport
 * @see ApplicationStarGateProtocol#getApplicationReport(GetApplicationReportRequest)
 */
public abstract class GetApplicationReportResponse {

  public static GetApplicationReportResponse newInstance(
      ApplicationReport ApplicationReport) {
    GetApplicationReportResponse response =
        ReflectionUtils.get().getMsg(GetApplicationReportResponse.class);
    response.setApplicationReport(ApplicationReport);
    return response;
  }

  /**
   * Get the {@code ApplicationReport} for the application.
   * 
   * @return {@code ApplicationReport} for the application
   */
  public abstract ApplicationReport getApplicationReport();

  /**
   * Set the {@code ApplicationReport} for the application.
   * 
   * @param ApplicationReport the report of the application
   */
  public abstract void setApplicationReport(
      ApplicationReport ApplicationReport);
}
