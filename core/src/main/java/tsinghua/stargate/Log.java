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

package tsinghua.stargate;

import java.net.URL;

import org.apache.log4j.LogManager;
import org.apache.log4j.PropertyConfigurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.impl.StaticLoggerBinder;

/**
 * Abstract class which can be extended by any class that want to log data.
 *
 * <p>
 * Creates a SLF4J logger for the class and allows logging messages at different
 * levels using methods that only evaluate parameters lazily if the log level is
 * enabled.
 */
public abstract class Log {

  private transient Logger logger;

  private static volatile boolean initialized = false;
  private static final Object LOCK = new Object();

  protected Logger log() {
    if (logger == null) {
      initLog();
      logger = LoggerFactory.getLogger(this.getClass().getName());
    }
    return logger;
  }

  private void initLog() {
    if (!initialized) {
      synchronized (LOCK) {
        if (!initialized) {
          doInit();
        }
      }
    }
  }

  private void doInit() {
    String binder =
        StaticLoggerBinder.getSingleton().getLoggerFactoryClassStr();
    boolean isLog4j = "org.slf4j.impl.Log4jLoggerFactory".equals(binder);
    if (isLog4j) {
      boolean initialized =
          LogManager.getRootLogger().getAllAppenders().hasMoreElements();
      if (!initialized) {
        String defaultLogProps = "log4j-default.properties";
        URL url = this.getClass().getClassLoader().getResource(defaultLogProps);
        if (null != url) {
          PropertyConfigurator.configure(url);
          System.err.println(
              "Using StarGate's default log4j profile: " + defaultLogProps);
        } else {
          System.err.println("StarGate was unable to load" + defaultLogProps);
        }
      }
    }
    initialized = true;
    log();
  }

  protected void info(String msg, Object... obj) {
    if (log().isInfoEnabled())
      log().info(msg, obj);
  }

  protected void debug(String msg, Object... obj) {
    if (log().isDebugEnabled())
      log().debug(msg, obj);
  }

  protected void warn(String msg, Object... obj) {
    if (log().isWarnEnabled())
      log().warn(msg, obj);
  }

  protected void error(String msg, Object... obj) {
    if (log().isErrorEnabled())
      log().error(msg, obj);
  }

  protected void trace(String msg, Object... obj) {
    if (log().isTraceEnabled())
      log().trace(msg, obj);
  }

  protected void info(String msg, Throwable throwable) {
    if (log().isInfoEnabled())
      log().info(msg, throwable);
  }

  protected void debug(String msg, Throwable throwable) {
    if (log().isDebugEnabled())
      log().debug(msg, throwable);
  }

  protected void warn(String msg, Throwable throwable) {
    if (log().isWarnEnabled())
      log().warn(msg, throwable);
  }

  protected void error(String msg, Throwable throwable) {
    if (log().isErrorEnabled())
      log().error(msg, throwable);
  }
}
