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
import tsinghua.stargate.rpc.message.entity.ApplicationId;
import tsinghua.stargate.rpc.message.entity.ApplicationReport;
import tsinghua.stargate.util.ReflectionUtils;

/**
 * The request sent by a client to the {@code StarGateDaemon} to get an
 * {@link ApplicationReport} for an application. The request should include the
 * {@link ApplicationId} of the application.
 * 
 * @see ApplicationReport
 * @see ApplicationStarGateProtocol#getApplicationReport(GetApplicationReportRequest)
 */
public abstract class GetApplicationReportRequest {

  public static GetApplicationReportRequest newInstance(
      ApplicationId applicationId) {
    GetApplicationReportRequest request =
        ReflectionUtils.get().getMsg(GetApplicationReportRequest.class);
    request.setApplicationId(applicationId);
    return request;
  }

  /**
   * Get the {@code ApplicationId} of the application.
   * 
   * @return the identifier of the application
   */
  public abstract ApplicationId getApplicationId();

  /**
   * Set the {@code ApplicationId} of the application.
   * 
   * @param applicationId the identifier of the application
   */
  public abstract void setApplicationId(ApplicationId applicationId);
}
