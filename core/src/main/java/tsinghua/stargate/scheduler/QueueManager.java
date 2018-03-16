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

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

import tsinghua.stargate.Log;

/**
 * A manager for multi {@link Schedulable} queues.
 *
 * <p>
 * Maintain a list of queues as well as scheduling parameters.
 */
public class QueueManager extends Log {

  private static final String ACCELERATOR_QUEUE = "stargate.accelerator";

  private SchedulableQueue rootQueue;

  private final Map<String, SchedulableQueue> queues = new HashMap<>();

  private final List<SchedulableQueue> leafQueues =
      new CopyOnWriteArrayList<>();

  private final FairScheduler scheduler;

  QueueManager(FairScheduler scheduler) {
    this.scheduler = scheduler;
  }

  void init() {
    rootQueue = new SchedulableParentQueue(ACCELERATOR_QUEUE, null, scheduler);
    queues.put(rootQueue.getName(), rootQueue);
  }

  /**
   * Get a leaf queue by name, creating it if the {@code create} is true. If the
   * queue is not or can not be a leaf queue, i.e. it already exists as a parent
   * queue, or one of the parents in its {@code name} is already a leaf queue,
   * null is returned.
   * 
   * @param name the leaf queue name, e.g. stargate.accelerator.loopback.app
   * @param create specify whether to create the leaf queue or not when it
   *          doesn't exist
   * @return {@code null}, an existed or a new created leaf queue
   */
  SchedulableLeafQueue getLeafQueue(String name, boolean create) {
    SchedulableQueue queue = getQueue(name, create);
    return queue instanceof SchedulableParentQueue ? null
        : (SchedulableLeafQueue) queue;
  }

  private SchedulableQueue getQueue(String name, boolean create) {
    String fullName = getFullName(name);
    synchronized (queues) {
      SchedulableQueue queue = queues.get(fullName);
      if (queue == null && create) {
        queue = createQueue(fullName);
      }
      return queue;
    }
  }

  /**
   * Prefix {@link #ACCELERATOR_QUEUE} to a queue name if necessary.
   *
   * @param name the original queue name
   * @return the full queue name
   */
  private String getFullName(String name) {
    if (!name.startsWith(ACCELERATOR_QUEUE + ".")
        && !name.equals(ACCELERATOR_QUEUE)) {
      name = ACCELERATOR_QUEUE + "." + name;
    }
    return name;
  }

  /**
   * Create a queue and its parent queue if necessary.
   * 
   * @param name the name of the queue to be created
   * @return the newly created queue
   */
  private SchedulableQueue createQueue(String name) {
    Stack<String> newParentQueueNames = new Stack<>();
    SchedulableParentQueue parentQueue = null;
    int curIndex = name.length();
    String curName;

    // Search the whole hierarchical queues from leaf to root until we reach one
    // that exists. A typical queue name looks like:
    // stargate.default.loopback.app or stargate.accelerator.loopback.app
    while (curIndex != -1) {
      int preIndex = curIndex;
      curIndex = name.lastIndexOf('.', curIndex - 1);
      String componentName = name.substring(curIndex + 1, preIndex);
      if (!isQueueNameValid(componentName)) {
        error("Illegal component name at offset:{} in queue:{}", curIndex + 1,
            name);
        return null;
      }
      curName = name.substring(0, curIndex);
      SchedulableQueue queue = queues.get(curName);
      if (queue == null) {
        newParentQueueNames.push(curName);
      } else {
        if (queue instanceof SchedulableParentQueue) {
          parentQueue = (SchedulableParentQueue) queue;
          break;
        } else {
          return null;
        }
      }
    }

    // Construct those parent queues that doesn't exist downwards
    SchedulableParentQueue newParentQueue;
    while (!newParentQueueNames.isEmpty()) {
      curName = newParentQueueNames.pop();
      newParentQueue =
          new SchedulableParentQueue(curName, parentQueue, scheduler);
      if (parentQueue != null) {
        parentQueue.addChildQueue(newParentQueue);
      }
      queues.put(newParentQueue.getName(), newParentQueue);
      parentQueue = newParentQueue;
    }

    // Hook the new leaf queue up to parent queue
    SchedulableLeafQueue leafQueue =
        new SchedulableLeafQueue(name, parentQueue, scheduler);
    if (parentQueue != null) {
      parentQueue.addChildQueue(leafQueue);
    }

    // Cache the newly created leaf queue
    queues.put(leafQueue.getName(), leafQueue);
    leafQueues.add(leafQueue);

    return leafQueue;
  }

  /**
   * Check whether {@code queueName} is valid or not.
   *
   * @param queueName the specified queue name
   * @return {@code true} if queue name is valid, {@code false} otherwise
   */
  boolean isQueueNameValid(String queueName) {
    return !queueName.isEmpty() && queueName.equals(queueName.trim());
  }

  /** Get the root queue. */
  SchedulableQueue getRootQueue() {
    return rootQueue;
  }

  /** Get a collection of all queues. */
  Collection<SchedulableQueue> getQueues() {
    return queues.values();
  }

  /** Get a collection of all leaf queues. */
  Collection<SchedulableQueue> getLeafQueues() {
    return leafQueues;
  }

  SchedulableParentQueue getParentQueue(String name, boolean create) {
    SchedulableQueue queue = getQueue(name, create);
    if (queue instanceof SchedulableLeafQueue) {
      return null;
    }
    return (SchedulableParentQueue) queue;
  }

  /**
   * Returns true if there are no applications, running or not, in the given
   * queue or any of its descendents.
   */
  protected boolean isEmpty(SchedulableQueue queue) {
    if (queue instanceof SchedulableLeafQueue) {
      SchedulableLeafQueue leafQueue = (SchedulableLeafQueue) queue;
      boolean result = (leafQueue.getNumRunnableApps() == 0
          && leafQueue.getNumNoRunnableApps() == 0);
      return result;
    } else {
      SchedulableParentQueue parentQueue = (SchedulableParentQueue) queue;
      for (SchedulableQueue child : parentQueue.getChildQueues()) {
        if (!isEmpty(child)) {
          return false;
        }
      }
      return true;
    }
  }

  /**
   * Remove the queue if it and its descendants are all empty.
   * 
   * @param queue the queue to be removed
   * @return true if removed, false otherwise
   */
  boolean removeQueueIfEmpty(SchedulableQueue queue) {
    if (isEmpty(queue)) {
      removeQueue(queue);
      return true;
    }
    return false;
  }

  /** Remove a queue and all its descendants. */
  private void removeQueue(SchedulableQueue queue) {
    if (queue instanceof SchedulableLeafQueue) {
      leafQueues.remove(queue);
      queues.remove(queue);
      SchedulableParentQueue parentQueue = queue.getParent();
      parentQueue.removeChildQueue(queue);
    } else {
      SchedulableParentQueue parentQueue = (SchedulableParentQueue) queue;
      for (SchedulableQueue tmp : parentQueue.getChildQueues()) {
        if (isEmpty(tmp)) {
          leafQueues.remove(tmp);
          queues.remove(queue);
        }
      }
      parentQueue.removeAllChildQueue();
    }
  }
}
