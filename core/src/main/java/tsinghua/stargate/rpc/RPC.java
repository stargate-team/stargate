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

package tsinghua.stargate.rpc;

import java.net.InetSocketAddress;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import tsinghua.stargate.Log;
import tsinghua.stargate.conf.Configuration;
import tsinghua.stargate.conf.NameSpace;
import tsinghua.stargate.exception.StarGateRuntimeException;
import tsinghua.stargate.rpc.workhorse.RpcServer;

/**
 * This class is an abstraction to get a specified RPC implementation.
 *
 * <p>
 * {@code RPC} holds the only entry point for
 * {@link tsinghua.stargate.client.Client Client} to access services provided by
 * {@link tsinghua.stargate.StarGateDaemon SGD}.
 *
 * <p>
 * Client doesn't handle this class directly. On the contrary, it has to use a
 * proxy, i.e., {@link tsinghua.stargate.client.proxy.ClientProxy ClientProxy},
 * to connect ANM, thus dealing with it indirectly.
 *
 * @see tsinghua.stargate.client.proxy.ClientProxy
 * @see tsinghua.stargate.rpc.thrift.ThriftRPC
 */
public abstract class RPC extends Log {

  private static final Logger LOG = LoggerFactory.getLogger(RPC.class);

  /**
   * Construct an instance of RPC implementation. The RPC implementation can be
   * offered by user. Otherwise, StarGate provides a default implementation,
   * i.e., {@link tsinghua.stargate.rpc.thrift.ThriftRPC ThriftRPC}.
   *
   * @param conf a configuration profiler
   * @return a new instance of RPC implementation
   */
  public static RPC create(Configuration conf) {
    String clazzName = conf.get(NameSpace.RPC_IMPL, NameSpace.DEFAULT_RPC_IMPL);
    LOG.info("Creating {} for communicating with StarGateDaemon", clazzName
        .substring(clazzName.lastIndexOf(".") + 1, clazzName.length()));
    try {
      return (RPC) Class.forName(clazzName).newInstance();
    } catch (Exception e) {
      LOG.error("Failed to create {}", clazzName
          .substring(clazzName.lastIndexOf(".") + 1, clazzName.length()));
      throw new StarGateRuntimeException(
          "Failed to construct " + clazzName
              .substring(clazzName.lastIndexOf(".") + 1, clazzName.length()),
          e);
    }
  }

  /**
   * Get a proxy of the passing {@code protocol}.
   *
   * @param conf a configuration profiler
   * @param protocol a Java interface for exchanging information between RPC
   *          Client and Server
   * @param address RPC Server socket address
   * @return the RPC Client proxy
   */
  public abstract Object getProxy(Configuration conf, Class<?> protocol,
      InetSocketAddress address);

  public abstract void stopProxy(Object proxy, Configuration conf);

  /**
   * Get a configured RPC Server proxy for the passing {@code protocol}.
   *
   * @param conf a configuration profiler
   * @param address server socket address for binding
   * @param ioQueueSize queue size for per IO thread
   * @param ioThreads number of IO threads
   * @param workerThreads number of worker threads
   * @param protocol a Java interface for exchanging information between RPC
   *          Client and Server
   * @param protocolImpl an instance that implements {@code protocol}
   * @return a configured RPC Server proxy for the passing {@code protocol}
   */
  public abstract RpcServer getServer(Configuration conf,
      InetSocketAddress address, int ioQueueSize, int ioThreads,
      int workerThreads, Class<?> protocol, Object protocolImpl);
}
