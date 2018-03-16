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

package tsinghua.stargate.scheduler;

import java.util.concurrent.ConcurrentHashMap;

import tsinghua.stargate.event.EventHandler;
import tsinghua.stargate.rpc.message.entity.ApplicationResourceUsageReport;

/**
 * This interface is used by the components to talk to scheduler for allocating
 * or cleaning up accelerator resources.
 */
public interface Scheduler<T1, T2> extends EventHandler<SchedulerEvent> {

  /**
   * Get all schedulable accelerators in a node.
   *
   * @return all schedulable accelerators in a node
   */
  ConcurrentHashMap<String, T2> getNodeAccelerators();

  AcceleratorResources getNodeResources();

  ConcurrentHashMap<String, T1> getTasks();

  /**
   * Get a resource usage report from a given accelerator task id
   *
   * @param taskId the id of the accelerator task
   * @return resource usage report for this given accelerator task
   */
  ApplicationResourceUsageReport getTaskResourceUsageReport(String taskId);

  /**
   * Get node resource usage report.
   * 
   * @param cardId
   * @return the {@link AcceleratorReport} for the node or null if nodeId does
   *         not point to a defined node.
   */
  AcceleratorReport getCardReport(String cardId);
}