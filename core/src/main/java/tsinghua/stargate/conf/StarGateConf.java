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

package tsinghua.stargate.conf;

import java.util.Map;

import tsinghua.stargate.rpc.message.entity.ServiceData;
import tsinghua.stargate.rpc.message.entity.Worker;
import tsinghua.stargate.storage.BlockManager;
import tsinghua.stargate.storage.BlockManagerImpl;

/** The only entry point to access StarGate configuration. */
public class StarGateConf extends Configuration {

  private static final String STARGATE_CORE_CONFIGURATION_FILE =
      "stargate-core.xml";

  static {
    Configuration.addDefaultResource(STARGATE_CORE_CONFIGURATION_FILE);
  }

  public StarGateConf() {
    super();
  }

  public StarGateConf(Configuration conf) {
    if (!(conf instanceof StarGateConf))
      this.reloadConfiguration();
  }

  public StarGateConf setAppName(String appName) {
    set(NameSpace.APP_NAME, appName);
    return this;
  }

  public String getAppName() {
    return get(NameSpace.APP_NAME, NameSpace.DEFAULT_APP_NAME);
  }

  public StarGateConf setWorker(Worker worker) {
    set(NameSpace.ACCELERATOR_WORKER, worker.toString());
    return this;
  }

  public Worker getWorker() {
    return Worker.valueOf(get(NameSpace.ACCELERATOR_WORKER,
        NameSpace.DEFAULT_ACCELERATOR_WORKER));
  }

  public StarGateConf setWorkerCapacity(Integer capacity) {
    setInt(NameSpace.ACCELERATOR_WORKER_CAPACITY, capacity);
    return this;
  }

  public Integer getWorkerCapacity() {
    return getInt(NameSpace.ACCELERATOR_WORKER_CAPACITY,
        NameSpace.DEFAULT_ACCELERATOR_WORKER_CAPACITY);
  }

  public StarGateConf setWorkload(String workload) {
    set(NameSpace.ACCELERATOR_WORKLOAD, workload);
    return this;
  }

  public String getWorkload() {
    return get(NameSpace.ACCELERATOR_WORKLOAD);
  }

  public StarGateConf setProcessor(Map<String, String> processor) {
    setObject(NameSpace.APP_PROCESSORS, processor);
    return this;
  }

  @SuppressWarnings("unchecked")
  public Map<String, String> getProcessor() {
    return (Map<String, String>) getObject(NameSpace.APP_PROCESSORS);
  }

  public StarGateConf setInSD(ServiceData serviceData) {
    setObject(NameSpace.APP_SERVICEDATA_INPUT, serviceData);
    return this;
  }

  public ServiceData getInSD() {
    return (ServiceData) getObject(NameSpace.APP_SERVICEDATA_INPUT);
  }

  public StarGateConf setOutSD(ServiceData serviceData) {
    setObject(NameSpace.APP_SERVICEDATA_OUTPUT, serviceData);
    return this;
  }

  public ServiceData getOutSD() {
    return (ServiceData) getObject(NameSpace.APP_SERVICEDATA_OUTPUT);
  }

  public StarGateConf setResource(Map<String, String> resource) {
    setObject(NameSpace.APP_RESOURCES, resource);
    return this;
  }

  @SuppressWarnings("unchecked")
  public Map<String, String> getResource() {
    return (Map<String, String>) getObject(NameSpace.APP_RESOURCES);
  }

  public BlockManager getBlockManager() {
    return new BlockManagerImpl(new Configuration());
  }
}
