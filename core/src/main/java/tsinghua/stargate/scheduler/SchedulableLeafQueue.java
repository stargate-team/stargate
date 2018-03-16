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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import tsinghua.stargate.exception.StarGateRuntimeException;
import tsinghua.stargate.rpc.message.entity.AcceleratorResource;

public class SchedulableLeafQueue extends SchedulableQueue {

  private final List<SchedulableTask> runningTasks = new ArrayList<>();
  private final List<SchedulableTask> pendingTasks = new ArrayList<>();

  // Get a lock with fair distribution for accelerator task queue updates
  private final ReadWriteLock rwLock = new ReentrantReadWriteLock(true);
  private final Lock readLock = rwLock.readLock();
  private final Lock writeLock = rwLock.writeLock();

  private long startTime = 0;

  private Comparator<Schedulable> fairShareComparator =
      new FairShareComparator();

  private Comparator<String> cardResourceComparator =
      new CoreResourceComparator();

  SchedulableLeafQueue(String name, SchedulableParentQueue queue,
      FairScheduler scheduler) {
    super(name, queue, scheduler);
  }

  @Override
  void assignResources() {
    if (scheduler.getNodeAccelerators().isEmpty()) {
      throw new StarGateRuntimeException(
          "None accelerator exists, please make sure accelerators have been installed");
    }

    List<String> acceleratorIds =
        new ArrayList<>(scheduler.getNodeAccelerators().keySet());

    /*
     * Sort the accelerator card by function space available on them, so that we
     * offer accelerator task on emptier cards first, facilitating an even
     * spread. This requires holding the scheduler lock, so that the space
     * available on a node doesn't change during the sort.
     */
    synchronized (this) {
      Collections.shuffle(acceleratorIds);
      acceleratorIds.sort(cardResourceComparator);
    }

    for (String s : acceleratorIds) {
      assignAcceleratorResource(s);
    }
  }

  public int getNumRunnableApps() {
    readLock.lock();
    try {
      return runningTasks.size();
    } finally {
      readLock.unlock();
    }
  }

  public int getNumNoRunnableApps() {
    readLock.lock();
    try {
      return pendingTasks.size();
    } finally {
      readLock.unlock();
    }
  }

  /**
   * Sort accelerators by available resources.
   */
  private class CoreResourceComparator implements Comparator<String> {

    @Override
    public int compare(String s1, String s2) {
      int size1 =
          scheduler.getNodeAccelerators().get(s1).getTotalResources().size();
      int size2 =
          scheduler.getNodeAccelerators().get(s2).getTotalResources().size();
      return size1 - size2;
    }
  }

  // -- Schedulable machinery --

  /**
   * 
   * @return the start time of task
   */
  @Override
  public long getStartTime() {
    return startTime;
  }

  @Override
  public long getSubmissionDuration() {
    long totalDuration = 0;
    readLock.lock();
    try {
      if (runningTasks.size() == 0) {
        return totalDuration;
      }

      for (SchedulableTask task : runningTasks) {
        totalDuration += task.getSubmissionDuration();
      }
    } finally {
      readLock.unlock();
    }

    return totalDuration;
  }

  @Override
  public AcceleratorResources askResources() {
    AcceleratorResources asks = new AcceleratorResourcesImpl();
    readLock.lock();
    try {
      for (SchedulableTask task : runningTasks) {
        asks.add(task.getAllocatedResource());
      }
      for (SchedulableTask task : pendingTasks) {
        asks.add(task.getAllocatedResource());
      }
    } finally {
      readLock.unlock();
    }
    return asks;
  }

  @Override
  public AcceleratorResource assignAcceleratorResource(String cardId) {
    AcceleratorResource assigned = null;

    if (!assignAcceleratorCardPreCheck(cardId)) {
      return null;
    }

    // Hold the write lock when sorting childQueues
    writeLock.lock();
    try {
      Collections.shuffle(runningTasks);
      Collections.sort(runningTasks, fairShareComparator);
    } finally {
      writeLock.unlock();
    }

    readLock.lock();
    try {
      for (SchedulableTask task : runningTasks) {
        assigned = task.assignAcceleratorResource(cardId);
      }
    } finally {
      readLock.unlock();
    }

    return assigned;
  }

  // -- Instance methods --

  public List<SchedulableTask> getAcceleratorTask(boolean running) {
    readLock.lock();
    try {
      if (running) {
        return runningTasks;
      } else {
        return pendingTasks;
      }

    } finally {
      readLock.unlock();
    }
  }

  void addAcceleratorTask(SchedulableTask task, boolean running) {
    writeLock.lock();
    try {
      if (running) {
        runningTasks.add(task);
      } else {
        pendingTasks.add(task);
      }

      if (startTime == 0) {
        startTime = System.currentTimeMillis();
      }

    } finally {
      writeLock.unlock();
    }
  }

  /**
   * Removes the given app from this queue.
   * 
   * @return whether or not the app was runnable
   */
  boolean removeRunningTask(SchedulableTask task) {
    boolean runnable;

    // Remove app from runnable/nonRunnable list while holding the write lock
    writeLock.lock();
    try {
      runnable = runningTasks.remove(task);
      if (!runnable) {
        // removeNonRunnableApp acquires the write lock again, which is fine
        if (!removePendingTask(task)) {
          throw new IllegalStateException("Given task to remove " + task
              + " does not exist in queue " + this);
        }
      }
    } finally {
      writeLock.unlock();
    }

    return runnable;
  }

  /**
   * Removes the given task if it is non-runnable and belongs to this queue
   * 
   * @return true if the task is removed, false otherwise
   */
  private boolean removePendingTask(SchedulableTask task) {
    writeLock.lock();
    try {
      return pendingTasks.remove(task);
    } finally {
      writeLock.unlock();
    }
  }
}
