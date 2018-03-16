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
 * Contains various scheduling metrics to be reported
 */
public abstract class ApplicationResourceUsageReport {

  public static ApplicationResourceUsageReport newInstance(Worker worker,
      String hardwareId, AcceleratorResource resource) {
    ApplicationResourceUsageReport applicationResourceUsageReport =
        ReflectionUtils.get().getMsg(ApplicationResourceUsageReport.class);
    applicationResourceUsageReport.setWorker(worker);
    applicationResourceUsageReport.setHardwareId(hardwareId);
    applicationResourceUsageReport.setAcceleratorResource(resource);
    return applicationResourceUsageReport;
  }

  /**
   * Get the application run accelerator worker type i.e. GPUs, FPGAs, ASICs.
   *
   * @return <code>Worker</code>
   */
  public abstract Worker getWorker();

  public abstract void setWorker(Worker workerType);

  /**
   * Get the the application run hardware id
   *
   * @return the hardware id
   */
  public abstract String getHardwareId();

  public abstract void setHardwareId(String hardwareId);

  /**
   * Get the <code>AcceleratorResource</code> of the application assigned
   *
   * @return <code>AcceleratorResource</code> of the application assigned
   */
  public abstract AcceleratorResource getAcceleratorResource();

  public abstract void setAcceleratorResource(AcceleratorResource resource);
}
