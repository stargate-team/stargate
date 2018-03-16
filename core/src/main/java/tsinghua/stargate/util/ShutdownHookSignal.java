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

import org.slf4j.Logger;

import sun.misc.Signal;
import sun.misc.SignalHandler;

/**
 * This class logs a message whenever we're about to exit on a UNIX signal. This
 * is helpful for determining the root cause of a process' exit. For example, if
 * the process exited because the system administrator ran a standard "kill,"
 * you would see 'EXITING ON SIGNAL SIGTERM' in the log.
 */
public class ShutdownHookSignal {

  private volatile static ShutdownHookSignal self;

  private ShutdownHookSignal() {
  }

  /**
   * Get the {@code ShutdownHookSignal} singleton by lazy instantiation. Employ
   * `double-checked locking` to reduce the use of synchronization in
   * {@code get()}.
   *
   * @return {@code ShutdownHookSignal} singleton
   */
  public static ShutdownHookSignal get() {
    if (self == null)
      synchronized (ShutdownHookSignal.class) {
        if (self == null)
          self = new ShutdownHookSignal();
      }
    return self;
  }

  private boolean registered = false;

  /**
   * Register some signal handlers.
   *
   * @param LOG the log4j to be used in the signal handlers.
   */
  public void register(final Logger LOG) {
    if (registered) {
      throw new IllegalStateException("Can't re-install signal handlers.");
    }
    registered = true;
    StringBuilder sb = new StringBuilder();
    sb.append("Registered UNIX signal handlers for [");
    final String SIGNALS[] = { "TERM", "HUP", "INT" };
    String separator = "";
    for (String signalName : SIGNALS) {
      try {
        new Handler(signalName, LOG);
        sb.append(separator);
        sb.append(signalName);
        separator = ", ";
      } catch (Exception e) {
        LOG.error(e.getMessage(), e.getCause());
      }
    }
    sb.append("]");
    LOG.info(sb.toString());
  }

  /** Signal handler. */
  public static class Handler implements SignalHandler {

    final private Logger LOG;
    final private SignalHandler prevHandler;

    Handler(String name, Logger LOG) {
      this.LOG = LOG;
      prevHandler = Signal.handle(new Signal(name), this);
    }

    /**
     * Handles an incoming signal.
     *
     * @param signal the incoming signal
     */
    @Override
    public void handle(Signal signal) {
      LOG.warn(
          "RECEIVED SIGNAL " + signal.getNumber() + ": SIG" + signal.getName());
      prevHandler.handle(signal);
    }
  }
}
