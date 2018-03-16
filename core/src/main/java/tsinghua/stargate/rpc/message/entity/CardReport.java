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

import java.util.List;

import tsinghua.stargate.util.ReflectionUtils;

/**
 * <p>
 * <code>CardReport</code> is a summary of runtime information of an accelerator
 * card in the node.
 * </p>
 *
 * <p>
 * It includes details such as:
 * <ul>
 * <li>card id of the card.</li>
 * <li>HTTP Tracking URL of the node.</li>
 * <li>Used {@link AcceleratorResource} on the node.</li>
 * <li>Total available {@link AcceleratorResource} of the node.</li>
 * <li>Number of running containers on the node.</li>
 * <li>Total accelerator workloads run on the card
 * <li/>
 * </ul>
 * </p>
 *
 *
 */
public abstract class CardReport {

  public static CardReport newInstance(String cardId, String httpAddress,
      String numTasks, List<String> workloads, List<AcceleratorResource> used,
      List<AcceleratorResource> capability) {
    CardReport cardReport = ReflectionUtils.get().getMsg(CardReport.class);
    return cardReport;
  }

  /**
   * Get the <code>cardId</code> of the card.
   * 
   * @return <code>cardId</code> of the card
   */
  public abstract String getCardId();

  public abstract void setCardId(String cardId);

  /**
   * Get the <em>http address</em> of the card.
   * 
   * @return <em>http address</em> of the card
   */
  public abstract String getHttpAddress();

  public abstract void setHttpAddress(String httpAddress);

  /**
   * Get <em>used</em> <code>AcceleratorResource</code> on the node.
   * 
   * @return <em>used</em> <code>AcceleratorResource</code> on the node
   */

  public abstract List<AcceleratorResource> getUsedCapability();

  public abstract void setUsedCapability(List<AcceleratorResource> used);

  /**
   * Get the <em>total</em> <code>AcceleratorResource</code> on the node.
   * 
   * @return <em>total</em> <code>AcceleratorResource</code> on the node
   */

  public abstract List<AcceleratorResource> getTotalCapability();

  public abstract void setTotalCapability(List<AcceleratorResource> capability);

  /**
   * Get the <em>number of allocated tasks</em> on the card.
   * 
   * @return <em>number of allocated tasks</em> on the card
   */
  public abstract int getNumTasks();

  public abstract void setNumTasks(int numContainers);
}
