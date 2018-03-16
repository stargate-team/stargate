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

import java.util.Collection;
import java.util.Map;

import tsinghua.stargate.rpc.message.GetNewApplicationResponse;
import tsinghua.stargate.util.ReflectionUtils;

/**
 * <p>
 * <code>ApplicationLaunchContext</code> represents all of the information
 * needed by the <code>StarGateDaemon</code> to launch a application.
 * </p>
 *
 * <p>
 * It includes details such as:
 * <ul>
 * <li>ApplicationId of the user source application.</li>
 * <li>Accelerator worker type expected to run</li>
 * <li>Optional, application-specific service data</li>
 * <li>service data process unit</li>
 * </ul>
 * </p>
 */
public abstract class ApplicationLaunchContext {

  public static ApplicationLaunchContext newInstance(String sourceApplicationId,
      Worker worker, ServiceData input, ServiceData output,
      Collection<String> jars) {
    ApplicationLaunchContext applicationLaunchContext =
        ReflectionUtils.get().getMsg(ApplicationLaunchContext.class);

    applicationLaunchContext.setUserAppId(sourceApplicationId);
    applicationLaunchContext.setWorker(worker);
    applicationLaunchContext.setInputServiceData(input);
    applicationLaunchContext.setOutputServiceData(output);
    return applicationLaunchContext;
  }

  /**
   * Get <code>Worker</code> accelerator application run
   *
   * @return <code>Worker</code> accelerator application run
   */
  public abstract Worker getWorker();

  /**
   * Set accelerator application run<code>Worker</code> eg. FPGA,CPU
   *
   * @param worker <code>Worker</code> accelerator application run
   */
  public abstract void setWorker(Worker worker);

  /**
   * Get <em>source application id</em>.
   *
   * @return <em>source application id</em>.
   */
  public abstract String getUserAppId();

  /**
   * <p>
   * Set accelerator <em>source application id</em>.There are two kinds of
   * application id, one is source application id from user like apache spark or
   * apache flink and stargate perform scheduling by it,The other application id
   * is assigned by stargate self and user get {@link GetNewApplicationResponse}
   * when create new application
   * </p>
   *
   * @param id <em>source application id</em>
   */
  public abstract void setUserAppId(String id);

  /**
   * <p>
   * Get application-specific <em>input service data</em>, this is a third party
   * data from user application can store in various formats.when application
   * run accelerator,it first pull <em>service data</em> from database,disk or
   * other ways and can cache it in memory
   * </p>
   *
   * @return application-specific <em>input service data</em>
   */
  public abstract ServiceData getInputServiceData();

  /**
   * Set application-specific <em>input service data</em>.
   *
   * @param serviceData application-specific <em>input service data</em>
   */
  public abstract void setInputServiceData(ServiceData serviceData);

  /**
   * <p>
   * Get application-specific <em>output service data</em> which accelerator
   * task complete.
   *
   * @return application-specific <em>output service data</em>
   */
  public abstract ServiceData getOutputServiceData();

  /**
   * Set application-specific <em>output service data</em>.
   *
   * @param serviceData application-specific <em>output service data</em>
   */
  public abstract void setOutputServiceData(ServiceData serviceData);

  /**
   * Get <em>service data processor</em>.
   *
   * @return <em>service data processor</em>
   */
  public abstract Map<String, String> getServiceDataProcessor();

  /**
   * set <em>service data processor</em>.
   *
   * @param processors <em>service data processor</em>
   */
  public abstract void setServiceDataProcessor(Map<String, String> processors);

  public abstract Map<String, String> getEnvironments();

  public abstract void setEnvironments(Map<String, String> environments);

  public abstract Map<String, String> getResources();

  public abstract void setResources(Map<String, String> resources);
}
