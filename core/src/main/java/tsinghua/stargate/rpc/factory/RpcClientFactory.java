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
import tsinghua.stargate.rpc.factory.impl.thrift.RpcClientFactoryThriftImpl;

/**
 * A factory for creating RPC Client.
 *
 * <p>
 * The distinction among various RPC Client factory implementations depends on
 * RPC engine, i.e., different RPC Clients employ different RPC engines to
 * communicate with their associated RPC Servers.
 *
 * <p>
 * A RPC engine can support its all well-designed protocols. Although different
 * RPC engines can support the same functional protocol, we strongly recommend
 * that different RPC engines only have their specified protocol families.
 * 
 * @see RpcClientFactoryThriftImpl
 * @see RpcServerFactory
 */
public interface RpcClientFactory {

  /**
   * Get a new client proxy of the given {@code protocol protocol}.
   *
   * <p>
   * The constructor for creating the {@code protocol protocol}'s client proxy
   * may come from a cache or a new one that would be finally added into the
   * cache. Note: The constructor of the same {@code protocol protocol} are
   * reused.
   * 
   * @param protocol a Java interface for exchanging information between RPC
   *          Client and Server.
   * @param version identifies compatible client-server pairs
   * @param address identifies the remote server's location
   * @param conf a configuration profiler
   * @return a new client proxy of the given {@code protocol protocol}
   */
  Object getClient(Class<?> protocol, long version, InetSocketAddress address,
      Configuration conf);

  /**
   * Stop the given client proxy of a protocol.
   * 
   * @param proxy the client proxy of a protocol
   */
  void stopClient(Object proxy);
}
