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

public class SchedulableParentQueue extends SchedulableQueue {

  private final List<SchedulableQueue> childQueues = new ArrayList<>();
  private ReadWriteLock rwLock = new ReentrantReadWriteLock();
  private Lock readLock = rwLock.readLock();
  private Lock writeLock = rwLock.writeLock();

  private Comparator<Schedulable> comparator = new FairShareComparator();

  SchedulableParentQueue(String name, SchedulableParentQueue queue,
      FairScheduler scheduler) {
    super(name, queue, scheduler);
  }

  void addChildQueue(SchedulableQueue child) {
    writeLock.lock();
    try {
      childQueues.add(child);
    } finally {
      writeLock.unlock();
    }
  }

  void removeChildQueue(SchedulableQueue child) {
    writeLock.lock();
    try {
      childQueues.remove(child);
    } finally {
      writeLock.unlock();
    }
  }

  void removeAllChildQueue() {
    writeLock.lock();
    try {
      childQueues.clear();
    } finally {
      writeLock.unlock();
    }
  }

  // -- Schedulable machinery --

  @Override
  public long getStartTime() {
    long totalStartTime = 0;
    readLock.lock();
    try {
      if (childQueues.size() == 0) {
        return totalStartTime;
      }
      for (SchedulableQueue queue : childQueues) {
        totalStartTime += queue.getStartTime();
      }
    } finally {
      readLock.unlock();
    }
    return totalStartTime;
  }

  @Override
  public long getSubmissionDuration() {
    long totalDuration = 0;
    readLock.lock();
    try {
      for (SchedulableQueue queue : childQueues) {
        totalDuration += queue.getSubmissionDuration();
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
      for (SchedulableQueue queue : childQueues) {
        asks.addAll(queue.askResources().getAllResources());
      }
    } finally {
      readLock.unlock();
    }
    return asks;
  }

  @Override
  public void assignResources() {
    // Hold the write lock when shuffling and sorting child queues.
    writeLock.lock();
    try {
      Collections.shuffle(childQueues);
      childQueues.sort(comparator);
    } finally {
      writeLock.unlock();
    }

    // Hold the read lock when shuffling and sorting child queues.
    readLock.lock();
    try {
      for (SchedulableQueue childQueue : childQueues) {
        childQueue.assignResources();
      }
    } finally {
      readLock.unlock();
    }
  }

  List<SchedulableQueue> getChildQueues() {
    readLock.lock();
    try {
      return Collections.unmodifiableList(childQueues);
    } finally {
      readLock.unlock();
    }
  }
}
