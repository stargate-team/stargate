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

import tsinghua.stargate.rpc.message.entity.AcceleratorResource;

/**
 * An entity that can be scheduled for execution.
 *
 * <p>
 * A {@code Schedulable} may be a queue or task. It provides a common interface
 * so that algorithms, e.g. fair sharing, can be applied both within a queue and
 * across queues.
 *
 * <p>
 * A {@code Schedulable} is responsible for two roles:
 *
 * <ul>
 *
 * <li>
 * <p>
 * Assign resources through {@link #assignAcceleratorResource},
 *
 * <li>
 * <p>
 * Provide information about the queue/task to the scheduler, including:
 *
 * <ul>
 *
 * <li>
 * <p>
 * Services/resources assigned to queues/tasks.
 *
 * <li>
 * <p>
 * Weight of queue/task.
 *
 * <li>
 * <p>
 * Start time and submission duration of task.
 *
 * </ul>
 *
 * </ul>
 */
public interface Schedulable {

  /**
   * Name of queue/task, used for debugging as well as for breaking ties in
   * scheduling order deterministically.
   */
  String getName();

  /** Weight of queue/task in fair scheduling. */
  Float getWeight();

  /** Start time of task in FIFO policy; meaningless for queue. */
  long getStartTime();

  /**
   * Duration since the first submission of task; meaningless for queue.
   */
  long getSubmissionDuration();

  /**
   * A list of services/resources requested by queue/task.
   *
   */
  AcceleratorResources askResources();

  /**
   * A list of services/resources offered by the max share of queue. Default:
   * all accelerator resources in a node.
   *
   */
  AcceleratorResources offerMaxShare();

  /**
   * A resource is assigned on this node if possible.
   *
   */
  AcceleratorResource assignAcceleratorResource(String cardId);
}
