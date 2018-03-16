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

package tsinghua.stargate.util;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@code ShutdownHookManager} enables running shutdown hooks in a deterministic
 * order, higher priority first.
 *
 * <p>
 * JVM runs shutdown hooks in a non-deterministic order or in parallel. This
 * class registers a single JVM shutdown hook and runs all the shutdown hooks
 * registered to it (to this class) in order based on their priority.
 */
public class ShutdownHookManager {

  private static final Logger LOG =
      LoggerFactory.getLogger(ShutdownHookManager.class);

  private static final ShutdownHookManager MGR = new ShutdownHookManager();

  private Set<HookEntry> hooks =
      Collections.synchronizedSet(new HashSet<HookEntry>());

  private AtomicBoolean shutdownInProgress = new AtomicBoolean(false);

  static {
    Runtime.getRuntime().addShutdownHook(new Thread() {
      @Override
      public void run() {
        MGR.shutdownInProgress.set(true);
        for (Runnable hook : MGR.getShutdownHooksInOrder()) {
          try {
            hook.run();
          } catch (Throwable t) {
            LOG.error("Failed to run shutdown hook '{}'",
                hook.getClass().getSimpleName(), t);
          }
        }
      }
    });
  }

  private ShutdownHookManager() {
  }

  /**
   * Gets {@code ShutdownHookManager} singleton.
   *
   * @return {@code ShutdownHookManager} singleton
   */
  public static ShutdownHookManager get() {
    return MGR;
  }

  /**
   * Gets the list of shutdown hooks in order of execution. Note: highest
   * priority first.
   *
   * @return the list of shutdown hooks in order of execution
   */
  List<Runnable> getShutdownHooksInOrder() {
    List<HookEntry> list;
    synchronized (MGR.hooks) {
      list = new ArrayList<>(MGR.hooks);
    }
    list.sort(new Comparator<HookEntry>() {
      @Override
      public int compare(HookEntry o1, HookEntry o2) {
        return o2.priority - o1.priority;
      }
    });
    List<Runnable> ordered = new ArrayList<>();
    for (HookEntry entry : list) {
      ordered.add(entry.hook);
    }
    return ordered;
  }

  /**
   * Adds a shutdown hook with a priority, higher priority will run earlier.
   * Shutdown hooks with the same priority run in a non-deterministic order.
   *
   * @param shutdownHook a shutdown {code Runnable} to be hooked in JVM
   * @param priority priority of {@code shutdownHook}
   */
  public void addShutdownHook(Runnable shutdownHook, int priority) {
    if (shutdownHook == null) {
      throw new IllegalArgumentException("Shutdown hook cannot be null");
    }
    if (shutdownInProgress.get()) {
      throw new IllegalStateException(
          "Shutdown is in progress, cannot add a shutdown hook");
    }
    hooks.add(new HookEntry(shutdownHook, priority));
  }

  /**
   * Removes a shutdown hook.
   *
   * @param shutdownHook to be removed
   * @return {@code true} if {@code shutdownHook} is registered and removed,
   *         otherwise {@code false}
   */
  public boolean removeShutdownHook(Runnable shutdownHook) {
    if (shutdownInProgress.get()) {
      throw new IllegalStateException(
          "Shutdown is in progress, cannot remove a shutdown hook");
    }
    return hooks.remove(new HookEntry(shutdownHook, 0));
  }

  /**
   * Indicates if a shutdown hook is registered or not.
   *
   * @param shutdownHook to be checked if registered
   * @return {@code true} if {@code shutdownHook} is registered, otherwise
   *         {@code false}
   */
  public boolean hasShutdownHook(Runnable shutdownHook) {
    return hooks.contains(new HookEntry(shutdownHook, 0));
  }

  /**
   * Indicates if shutdown is in progress or not.
   *
   * @return {@code true} if the shutdown is in progress, otherwise
   *         {@code false}
   */
  public boolean isShutdownInProgress() {
    return shutdownInProgress.get();
  }

  /** Private structure to store shutdown hook and its priority. */
  public static class HookEntry {

    Runnable hook;
    int priority;

    private HookEntry(Runnable hook, int priority) {
      this.hook = hook;
      this.priority = priority;
    }

    @Override
    public int hashCode() {
      return hook.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
      boolean eq = false;
      if (obj != null) {
        if (obj instanceof HookEntry) {
          eq = (hook == ((HookEntry) obj).hook);
        }
      }
      return eq;
    }
  }
}
