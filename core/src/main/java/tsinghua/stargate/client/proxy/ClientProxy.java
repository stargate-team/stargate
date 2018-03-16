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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;

import tsinghua.stargate.StarGateDaemon;
import tsinghua.stargate.conf.Configuration;
import tsinghua.stargate.conf.NameSpace;
import tsinghua.stargate.conf.StarGateConf;
import tsinghua.stargate.rpc.ApplicationStarGateProtocol;

/**
 * A RPC Client proxy used for bridging the gap between
 * {@link tsinghua.stargate.client.Client Client} and {@link StarGateDaemon
 * SGD}.
 */
public class ClientProxy extends Proxy {

  private static final Logger LOG = LoggerFactory.getLogger(ClientProxy.class);

  private static final ClientProxy SELF = new ClientProxy();

  private ClientProxy() {
    super();
  }

  /**
   * Verify if the passed protocol is supported.
   * 
   * @param protocol to be verified
   */
  @Override
  protected void verifyProtocol(Class<?> protocol) {
    Preconditions.checkArgument(
        protocol.isAssignableFrom(ApplicationStarGateProtocol.class),
        "StarGateDaemon does not support this protocol");
  }

  /**
   * Create a proxy for the specified protocol, i.e. a RPC Client Proxy. This
   * proxy will be employed by Client to communicate with SGD.
   *
   * @param conf a configuration profiler
   * @param protocol a Java interface for exchanging information between RPC
   *          Client and Server.
   * @param <T> the type of protocol
   * @return a ANM proxy carrying a specified protocol, which will be employed
   *         by Client
   * @throws IOException if some I/O errors occur
   */
  public static <T> T createProxy(final Configuration conf,
      final Class<T> protocol) throws IOException {
    return createProxy(conf, protocol, SELF);
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
  @Override
  protected InetSocketAddress getRemoteAddress(StarGateConf conf,
      Class<?> protocol) throws IOException {
    if (protocol == ApplicationStarGateProtocol.class) {
      return conf.getSocketAddr(NameSpace.RPC_SERVER_ADDRESS,
          NameSpace.DEFAULT_RPC_SERVER_ADDRESS,
          NameSpace.DEFAULT_RPC_SERVER_PORT);
    } else {
      String msg = "Unsupported protocol found when creating the proxy "
          + "connection to StarGateDaemon: "
          + ((protocol != null) ? protocol.getClass().getName() : "null");
      LOG.error(msg);
      throw new IllegalStateException(msg);
    }
  }
}
