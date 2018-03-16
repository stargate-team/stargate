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

package tsinghua.stargate.client.proxy;

import java.io.IOException;
import java.net.InetSocketAddress;

import tsinghua.stargate.conf.Configuration;
import tsinghua.stargate.conf.StarGateConf;
import tsinghua.stargate.rpc.RPC;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A proxy used for communicating with {@code StarGateDaemon SGD}.
 *
 * <p>
 * {@code Proxy Proxy} is the only entry point to ANM, which means that any
 * clients that want to use SGD services must create their own proxies extending
 * this class.
 */
public abstract class Proxy {

  private static final Logger LOG = LoggerFactory.getLogger(Proxy.class);

  protected Proxy() {
  }

  /**
   * Verify if the passed protocol is supported.
   *
   * @param protocol to be verified
   */
  protected abstract void verifyProtocol(Class<?> protocol);

  /**
   * Create a proxy for the specified protocol, i.e., a RPC Client proxy.
   *
   * @param conf a configuration profiler
   * @param protocol a Java interface for exchanging information between RPC
   *          Client and Server.
   * @param proxy an instance of {@code Proxy Proxy} inheritor
   * @param <T> the type of the passing protocol
   * @return a client proxy for the protocol
   * @throws IOException if some I/O errors occur
   */
  protected static <T> T createProxy(final Configuration conf,
      final Class<T> protocol, Proxy proxy) throws IOException {
    StarGateConf sgc = (conf instanceof StarGateConf)
        ? (StarGateConf) conf : new StarGateConf(conf);
    InetSocketAddress anmAddress = proxy.getRemoteAddress(sgc, protocol);
    LOG.info("Creating {} for StarGateDaemon@{}:{}",
        proxy.getClass().getSimpleName(), anmAddress.getHostName(),
        anmAddress.getPort());
    return getProxy(conf, protocol, anmAddress);
  }

  /**
   * Get a client proxy implementation which is specified for this protocol. The
   * client proxy can manipulate the underlying RPC mechanism directly.
   *
   * @param conf a configuration profiler
   * @param protocol a Java interface for exchanging information between RPC
   *          Client and Server.
   * @param anmAddress identifies the location of SGD
   * @param <T> the type of protocol
   * @return a ANM proxy carrying a specified protocol
   */
  @SuppressWarnings("unchecked")
  private static <T> T getProxy(final Configuration conf,
      final Class<T> protocol, final InetSocketAddress anmAddress) {
    return (T) RPC.create(conf).getProxy(conf, protocol, anmAddress);
  }

  /**
   * Get the socket address of SGD.
   * 
   * @param conf a configuration profiler
   * @param protocol a Java interface for exchanging information between RPC
   *          Client and Server.
   * @return the socket address of SGD
   * @throws IOException if some I/O errors occur
   */
  protected InetSocketAddress getRemoteAddress(StarGateConf conf,
      Class<?> protocol) throws IOException {
    throw new UnsupportedOperationException(
        "This method should be invoked " + "from an instance of ClientProxy");
  }
}
