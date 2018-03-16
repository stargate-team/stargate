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

package tsinghua.stargate.rpc.factory.provider;

import tsinghua.stargate.conf.Configuration;
import tsinghua.stargate.conf.NameSpace;
import tsinghua.stargate.rpc.factory.RpcClientFactory;
import tsinghua.stargate.rpc.factory.RpcServerFactory;
import tsinghua.stargate.util.ReflectionUtils;

/**
 * A factory provider for creating RPC Client and Server instances, according to
 * a user-defined or system-default configuration.
 *
 * @see RpcClientFactory
 * @see RpcServerFactory
 */
public class RpcFactoryProvider {

  private RpcFactoryProvider() {
  }

  /**
   * Get a factory for creating RPC Client.
   * 
   * @param conf a configuration profiler
   * @return a specific {@link RpcClientFactory RpcClientFactory} implementation
   */
  public static RpcClientFactory getClientFactory(Configuration conf) {
    String factoryName = conf.get(NameSpace.RPC_CLIENT_FACTORY,
        NameSpace.DEFAULT_RPC_CLIENT_FACTORY);
    return (RpcClientFactory) getFactory(factoryName);
  }

  /**
   * Get a factory for creating RPC Server.
   * 
   * @param conf a configuration profiler
   * @return a specific {@link RpcServerFactory RpcServerFactory} implementation
   */
  public static RpcServerFactory getServerFactory(Configuration conf) {
    String factoryName = conf.get(NameSpace.RPC_SERVER_FACTORY,
        NameSpace.DEFAULT_RPC_SERVER_FACTORY);
    return (RpcServerFactory) ReflectionUtils.get().getFactory(factoryName);
  }

  /**
   * Get a factory instance for the provided {@code clazzName clazzName}.
   *
   * @param clazzName the class name of factory implementation
   * @return a newly constructed factory instance
   */
  private static Object getFactory(String clazzName) {
    return ReflectionUtils.get().getFactory(clazzName);
  }
}
