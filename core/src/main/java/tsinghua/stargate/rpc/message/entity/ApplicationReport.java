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

package tsinghua.stargate.rpc.message.entity;

import tsinghua.stargate.util.ReflectionUtils;

/**
 * This class is a report of an application.
 *
 * <p>
 * It includes details such as:
 * <ul>
 * <li>{@link ApplicationId} accelerator on which the application runs.</li>
 * <li>Applications user.</li>
 * <li>Application queue.</li>
 * <li>Application name.</li>
 * <li>Host on which the application is running.</li>
 * <li>RPC port of the application.</li>
 * <li>{@link ApplicationState} of the application.</li>
 * <li>Diagnostic information in case of errors.</li>
 * <li>Start time of the application.</li>
 * </ul>
 * </p>
 */
public abstract class ApplicationReport {

  public static ApplicationReport newInstance(ApplicationState applicationState,
      ApplicationId appId, long startTime, long finishTime,
      ApplicationResourceUsageReport applicationResourceUsageReport) {
    ApplicationReport report =
        ReflectionUtils.get().getMsg(ApplicationReport.class);
    report.setApplicationState(applicationState);
    report.setApplicationId(appId);
    report.setStartTime(startTime);
    report.setFinishTime(finishTime);
    report.setApplicationResourceUsageReport(applicationResourceUsageReport);
    return report;
  }

  /**
   * Get the <em>ApplicationId</em> of the application.
   *
   * @return <em>ApplicationId</em> of the application
   */
  public abstract ApplicationId getApplicationId();

  public abstract void setApplicationId(ApplicationId appId);

  /**
   * Get the <em>start time</em> of the application.
   *
   * @return <em>start time</em> of the application
   */
  public abstract long getStartTime();

  public abstract void setStartTime(long startTime);

  /**
   * Get the <em>finish time</em> of the application.
   *
   * @return <em>finish time</em> of the application
   */
  public abstract long getFinishTime();

  public abstract void setFinishTime(long finishTime);

  /**
   * Retrieve the structure containing the job resources for this application
   *
   * @return the job resources structure for this application
   */

  public abstract ApplicationResourceUsageReport getApplicationResourceUsageReport();

  /**
   * Store the structure containing the job resources for this application
   *
   * @param appResources structure for this application
   */
  public abstract void setApplicationResourceUsageReport(
      ApplicationResourceUsageReport appResources);

  /**
   * Get the application running status in stargate
   * 
   * @return application running status
   */
  public abstract ApplicationState getApplicationState();

  public abstract void setApplicationState(ApplicationState type);

  /**
   * Get the <em>diagnositic information</em> of the application in case of
   * errors.
   * 
   * @return <em>diagnositic information</em> of the application in case of
   *         errors
   */
  public abstract String getDiagnostics();

  public abstract void setDiagnostics(String diagnostics);
}
