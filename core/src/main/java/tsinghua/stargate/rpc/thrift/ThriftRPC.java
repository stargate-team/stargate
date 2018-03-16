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

package tsinghua.stargate.rpc.thrift;

import java.net.InetSocketAddress;

import tsinghua.stargate.StarGateDaemon;
import tsinghua.stargate.conf.Configuration;
import tsinghua.stargate.rpc.RPC;
import tsinghua.stargate.rpc.factory.RpcClientFactory;
import tsinghua.stargate.rpc.factory.RpcServerFactory;
import tsinghua.stargate.rpc.factory.provider.RpcFactoryProvider;
import tsinghua.stargate.rpc.workhorse.RpcServer;

/**
 * This class is a default RPC implementation associated with
 * {@link StarGateDaemon SGD} services.
 *
 * <p>
 * Since ANM uses Thrift as its underlying RPC mechanism, this class is an
 * integration of the underlying Thrift RPC implementation and all its supported
 * protocols. So it is always regarded as the only entry point of using Thrift
 * RPC.
 */
public class ThriftRPC extends RPC {

  /**
   * Get a Thrift client proxy of the passing {@code protocol protocol}, i.e.,
   * the client implementation of this {@code protocol protocol}.
   * 
   * @param conf a configuration profiler
   * @param protocol a Java interface for exchanging information between RPC
   *          Client and Server.
   * @param address RPC Server socket address
   * @return a Thrift client proxy for the specified {@code protocol protocol}
   * @see RpcFactoryProvider#getClientFactory(Configuration)
   * @see RpcClientFactory#getClient(Class, long, InetSocketAddress,
   *      Configuration)
   */
  @Override
  public Object getProxy(Configuration conf, Class protocol,
      InetSocketAddress address) {
    info("Creating a client proxy for protocol {}", protocol.getSimpleName());
    return RpcFactoryProvider.getClientFactory(conf).getClient(protocol, 1,
        address, conf);
  }

  @Override
  public void stopProxy(Object proxy, Configuration conf) {
    RpcFactoryProvider.getClientFactory(conf).stopClient(proxy);
  }

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
   * @see RpcFactoryProvider#getServerFactory(Configuration)
   * @see RpcServerFactory#getServer(Configuration, InetSocketAddress, int, int,
   *      int, Class, Object)
   */
  @Override
  public RpcServer getServer(Configuration conf, InetSocketAddress address,
      int ioQueueSize, int ioThreads, int workerThreads, Class<?> protocol,
      Object protocolImpl) {
    info("Creating a server proxy for protocol {}", protocol.getSimpleName());
    return RpcFactoryProvider.getServerFactory(conf).getServer(conf, address,
        ioQueueSize, ioThreads, workerThreads, protocol, protocolImpl);
  }
}
