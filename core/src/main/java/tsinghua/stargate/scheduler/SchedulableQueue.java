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

package tsinghua.stargate.scheduler;

import tsinghua.stargate.rpc.message.entity.AcceleratorResource;

public abstract class SchedulableQueue implements Schedulable {

  /** Queue name, e.g. "stargate.loopback.app". */
  private final String name;
  private final SchedulableParentQueue parent;
  FairScheduler scheduler;

  SchedulableQueue(String name, SchedulableParentQueue parent,
      FairScheduler scheduler) {
    this.name = name;
    this.parent = parent;
    this.scheduler = scheduler;
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public Float getWeight() {
    // Extract workload name from this queue name
    String queueName = this.name;
    int startIndex = queueName.indexOf(".") + 1;
    int endIndex = (queueName.indexOf(".", startIndex) == -1)
        ? queueName.length() : queueName.indexOf(".", startIndex);
    String workload = queueName.substring(startIndex, endIndex);
    return scheduler.getWeight(workload);
  }

  @Override
  public AcceleratorResources offerMaxShare() {
    return scheduler.getNodeResources();
  }

  /**
   * Helper method to check if the queue should attempt assigning resources.
   *
   * @return true if check passes (can assignResources) or false otherwise
   */
  protected boolean assignAcceleratorCardPreCheck(String cardId) {
    return scheduler.getNodeAccelerators().containsKey(cardId);
  }

  public SchedulableParentQueue getParent() {
    return parent;
  }

  @Override
  public String toString() {
    return getName();
  }

  @Override
  public AcceleratorResource assignAcceleratorResource(String cardId) {
    return null;
  }

  abstract void assignResources();
}
