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

package tsinghua.stargate.rpc.factory;

import java.net.InetSocketAddress;

import tsinghua.stargate.conf.Configuration;
import tsinghua.stargate.rpc.factory.impl.thrift.RpcServerFactoryThriftImpl;
import tsinghua.stargate.rpc.workhorse.RpcServer;

/**
 * A factory for creating RPC Server.
 *
 * @see RpcServerFactoryThriftImpl
 * @see RpcClientFactory
 */
public interface RpcServerFactory {

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
   * @return a configured RPC Server proxy for the passing {@code protocol}.
   */
  RpcServer getServer(Configuration conf, InetSocketAddress address,
      int ioQueueSize, int ioThreads, int workerThreads, Class<?> protocol,
      Object protocolImpl);
}
