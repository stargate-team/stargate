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

package tsinghua.stargate.util;

import java.util.concurrent.*;

import com.google.common.util.concurrent.ThreadFactoryBuilder;

/**
 * Thread-related utility methods used by StarGate. `ThreadUtils` class is
 * implemented by employing `The Singleton Pattern`.
 */
public class ThreadUtils {

  private volatile static ThreadUtils executor;

  private ThreadUtils() {
  }

  /**
   * Create only one executor by lazy instantiation. Employ `double-checked
   * locking` to reduce the use of synchronization in
   * <code>getExecutor()</code>.
   *
   * @return the only instance of <code>ThreadUtils</code>
   */
  public static ThreadUtils getExecutor() {
    if (executor == null)
      synchronized (ThreadUtils.class) {
        if (executor == null)
          executor = new ThreadUtils();
      }
    return executor;
  }

  /**
   * Create a cached thread pool whose max number of threads is
   * `maxThreadNumber`. Thread names are formatted as prefix-ID, where ID is a
   * unique, sequentially assigned integer. Thread keep alive time is 60 seconds
   * by default.
   *
   * @param prefix the prefix of a thread name
   * @param maxThreadNumber the max number of threads to create before queuing
   *          the tasks
   * @return a configured {@link ThreadPoolExecutor} instance with keep alive
   *         time of 60 seconds.
   */
  public ThreadPoolExecutor newDaemonCachedThreadPool(String prefix,
      int maxThreadNumber) {
    return newDaemonCachedThreadPool(prefix, maxThreadNumber, 60);
  }

  /**
   * Create a cached thread pool whose max number of threads is
   * `maxThreadNumber`. Thread names are formatted as prefix-ID, where ID is a
   * unique, sequentially assigned integer.
   *
   * @param prefix the prefix of a thread name
   * @param maxThreadNumber the max number of threads to create before queuing
   *          the tasks
   * @param keepAliveSeconds the keep alive time of threads
   * @return a configured {@link ThreadPoolExecutor} instance
   */
  private ThreadPoolExecutor newDaemonCachedThreadPool(String prefix,
      int maxThreadNumber, int keepAliveSeconds) {
    ThreadFactory threadFactory = getDaemonThreadFactory(prefix);
    ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(
        maxThreadNumber, maxThreadNumber, (long) keepAliveSeconds,
        TimeUnit.SECONDS, new LinkedBlockingDeque<>(), threadFactory);
    threadPoolExecutor.allowCoreThreadTimeOut(true);
    return threadPoolExecutor;
  }

  /**
   * Create a thread factory that names threads with a prefix and also sets the
   * threads to daemon.
   *
   * @param prefix the prefix of a thread name
   * @return a {@link ThreadFactory} instance
   */
  private ThreadFactory getDaemonThreadFactory(String prefix) {
    return new ThreadFactoryBuilder().setDaemon(true)
        .setNameFormat(prefix + "-%d").build();
  }

  /**
   * Creates a new {@code ThreadPoolExecutor} with the given initial parameters.
   *
   * @param prefix the prefix of custom thread name
   * @param poolSize the number of threads to keep and the maximum number of
   *          threads to allow in the pool
   * @param keepAliveTime when the number of threads is greater than the core,
   *          this is the maximum time that excess idle threads will wait for
   *          new tasks before terminating.
   * @param unit the time unit for the {@code keepAliveTime} argument
   * @return a configured {@link ThreadPoolExecutor} instance
   */
  public ThreadPoolExecutor newCachedThreadPool(String prefix, int poolSize,
      long keepAliveTime, TimeUnit unit) {
    ThreadFactory threadFactory = getThreadFactory(prefix);
    return new ThreadPoolExecutor(poolSize, poolSize, keepAliveTime, unit,
        new LinkedBlockingQueue<>(), threadFactory);
  }

  /**
   * Create a thread factory that names threads with a prefix.
   *
   * @param prefix the prefix of a thread name
   * @return a {@link ThreadFactory} instance
   */
  private ThreadFactory getThreadFactory(String prefix) {
    return new ThreadFactoryBuilder().setNameFormat(prefix + "-%d").build();
  }
}
