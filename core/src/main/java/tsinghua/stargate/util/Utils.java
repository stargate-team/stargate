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

import java.net.*;
import java.util.*;

import org.apache.commons.lang.SystemUtils;
import org.apache.hadoop.security.SecurityUtil;
import org.slf4j.Logger;

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;

import tsinghua.stargate.exception.StarGateException;

public class Utils {

  private static final String ANSI_RESET = "\u001B[0m";
  private static final String ANSI_PURPLE = "\u001B[35m";

  /** Priority of shutdown hook. */
  private static final int SHUTDOWN_HOOK_PRIORITY = 0;

  public static void printPurple(String[] objs) {
    for (String o : objs) {
      System.out.println(
          ANSI_PURPLE + o + ANSI_RESET + System.getProperty("line.separator"));
    }
  }

  public static void startupShutdownMessage(final Logger LOG) {
    final String hostname = getHostname();
    LOG.info(welcomeLogo());
    if (SystemUtils.IS_OS_UNIX) {
      try {
        ShutdownHookSignal.get().register(LOG);
      } catch (Throwable t) {
        LOG.warn("Failed to register any UNIX signal loggers: ", t);
      }
    }
    ShutdownHookManager.get().addShutdownHook(new Runnable() {
      @Override
      public void run() {
        LOG.info(toStartupShutdownString(
            new String[] { byeLogo() + "   @" + hostname }));
      }
    }, SHUTDOWN_HOOK_PRIORITY);
  }

  private static String getHostname() {
    try {
      return "" + InetAddress.getLocalHost();
    } catch (UnknownHostException uhe) {
      return "" + uhe;
    }
  }

  private static String welcomeLogo() {
    return String.format("\n%s\n%s\n%s\n%s\n%s", "Welcome to",
        "          ____   __            _____         __",
        "         / __/__/_/_____ _____/ ___/ ___  __/_/__ ___",
        "        _\\ \\/__/_/__/ _ `/ __/ /___// _ \\/_/_/__/_`_/",
        "       /___/   \\_\\_/\\_,_/_/  \\__,__/\\_,_/  \\_\\_/\\___   version 0.1.0");
  }

  private static String byeLogo() {
    return String.format("\n%s\n%s\n%s\n%s\n%s", "Bye",
        "   ____   __            _____         __",
        "  / __/__/_/_____ _____/ ___/ ___  __/_/__ ___",
        " _\\ \\/__/_/__/ _ `/ __/ /___// _ \\/_/_/__/_`_/",
        "/___/   \\_\\_/\\_,_/_/  \\__,__/\\_,_/  \\_\\_/\\___");
  }

  /**
   * Return a message for logging.
   *
   * @param msg content of the message
   * @return a message for logging
   */
  private static String toStartupShutdownString(String[] msg) {
    StringBuilder b = new StringBuilder();
    for (String s : msg)
      b.append(s);
    return b.toString();
  }

  public static InetAddress getLocalHostLANAddress()
      throws SocketException, UnknownHostException {
    InetAddress candidateAddress = null;

    for (Enumeration ifaces = NetworkInterface.getNetworkInterfaces(); ifaces
        .hasMoreElements();) {
      NetworkInterface iface = (NetworkInterface) ifaces.nextElement();

      for (Enumeration inetAddrs = iface.getInetAddresses(); inetAddrs
          .hasMoreElements();) {
        InetAddress inetAddr = (InetAddress) inetAddrs.nextElement();
        if (!inetAddr.isLoopbackAddress()) {
          if (inetAddr.isSiteLocalAddress()) {
            return inetAddr;
          } else if (candidateAddress == null) {
            candidateAddress = inetAddr;
          }
        }
      }
    }
    if (candidateAddress != null) {
      return candidateAddress;
    }
    return InetAddress.getLocalHost();
  }

  /**
   * Obtain a queue name, e.g. stargate.default.workloadId.appId or
   * stargate.accelerator.workloadId.appId.
   */
  public static String getQueueName(String... args) {
    return Joiner.on(".").skipNulls().join(args);
  }

  /**
   * Obtain task workload from accelerator form accelerator task id.
   */
  public static String getTaskWorkLoad(String taskId) {
    Iterator<String> tmpIterator = Splitter.on('_').trimResults()
        .omitEmptyStrings().split(taskId).iterator();
    tmpIterator.next();
    return tmpIterator.next();
  }

  /**
   * Create an InetSocketAddress from the given target string and default port.
   * If the string cannot be parsed correctly, the <code>configName</code>
   * parameter is used as part of the exception message, allowing the user to
   * better diagnose the misconfiguration.
   *
   * @param target a string of either "host" or "host:port"
   * @param defaultPort the default port if <code>target</code> does not include
   *          a port number
   * @param configName the name of the configuration from which
   *          <code>target</code> was loaded. This is used in the exception
   *          message in the case that parsing fails.
   */
  public static InetSocketAddress createSocketAddr(String target,
      int defaultPort, String configName) {
    String helpText = "";
    if (configName != null) {
      helpText = " (configuration property '" + configName + "')";
    }
    if (target == null) {
      throw new IllegalArgumentException(
          "Target address cannot be null." + helpText);
    }
    boolean hasScheme = target.contains("://");
    URI uri;
    try {
      uri = hasScheme ? URI.create(target)
          : URI.create("dummyscheme://" + target);
    } catch (IllegalArgumentException e) {
      throw new IllegalArgumentException(
          "Does not contain a valid host:port authority: " + target + helpText);
    }

    String host = uri.getHost();
    int port = uri.getPort();
    if (port == -1) {
      port = defaultPort;
    }
    String path = uri.getPath();

    if ((host == null) || (port < 0)
        || (!hasScheme && path != null && !path.isEmpty())) {
      throw new IllegalArgumentException(
          "Does not contain a valid host:port authority: " + target + helpText);
    }
    return createSocketAddrForHost(host, port);
  }

  /**
   * Create a socket address with the given host and port. The hostname might be
   * replaced with another host that was set via The value of
   * hadoop.security.token.service.use_ip will determine whether the standard
   * java host resolver is used, or if the fully qualified resolver is used.
   * 
   * @param host the hostname or IP use to instantiate the object
   * @param port the port number
   * @return InetSocketAddress
   */
  private static InetSocketAddress createSocketAddrForHost(String host,
      int port) {
    String staticHost = getStaticResolution(host);
    String resolveHost = (staticHost != null) ? staticHost : host;

    InetSocketAddress addr;
    try {
      InetAddress iaddr = SecurityUtil.getByName(resolveHost);
      // if there is a static entry for the host, make the returned
      // address look like the original given host
      if (staticHost != null) {
        iaddr = InetAddress.getByAddress(host, iaddr.getAddress());
      }
      addr = new InetSocketAddress(iaddr, port);
    } catch (UnknownHostException e) {
      addr = InetSocketAddress.createUnresolved(host, port);
    }
    return addr;
  }

  private static Map<String, String> hostToResolved = new HashMap<>();

  /**
   * Retrieves the resolved name for the passed host. The resolved name must
   * have been set earlier using
   * 
   * @param host the hostname
   * @return the resolution
   */
  private static String getStaticResolution(String host) {
    synchronized (hostToResolved) {
      return hostToResolved.get(host);
    }
  }

  /**
   * Splits a comma separated value <code>String</code>, trimming leading and
   * trailing whitespace on each value.
   *
   * @param str a comma separated <String> with values
   * @return an array of <code>String</code> values
   */
  public static String[] getTrimmedStrings(String str) {
    final String[] emptyStringArray = {};
    if (null == str || str.trim().isEmpty()) {
      return emptyStringArray;
    }
    return str.trim().split("\\s*,\\s*");
  }

  /**
   * Get a suitable array size since the underlying Intel-FPGA requires.
   * 
   * @param length real array length
   * @return expected array length
   */
  public static int getFillSize(int length) {
    return (int) Math.ceil(length / 8.0D) * 8;
  }

  /** Convert a quantity in bytes to a human-readable string, e.g., "1.0 GB". */
  public static String bytes2String(long size) {
    long TB = 1L << 40;
    long GB = 1L << 30;
    long MB = 1L << 20;
    long KB = 1L << 10;

    double value;
    String unit;
    if (size >= TB) {
      value = size * 1.0 / TB;
      unit = "TB";
    } else if (size >= GB) {
      value = size * 1.0 / GB;
      unit = "GB";
    } else if (size >= MB) {
      value = size * 1.0 / MB;
      unit = "MB";
    } else if (size >= KB) {
      value = size * 1.0 / KB;
      unit = "KB";
    } else {
      value = size * 1.0;
      unit = "B";
    }

    return String.format(Locale.US, "%.1f %s", value, unit);
  }

  public static String getClassName(Class clazz) {
    String className = clazz.getCanonicalName();
    int index = className.lastIndexOf(".");
    StringBuilder sb = new StringBuilder(className);
    sb.replace(index, index + 1, "$");
    return sb.toString();
  }

  public static Map<String, String> getExampleJar() {
    Map<String, String> jars = new HashMap<>();

    try {
      AppUtils.addJar(jars, PathUtils.exampleJar());
    } catch (StarGateException e) {
      e.printStackTrace();
    }

    return jars;
  }
}
