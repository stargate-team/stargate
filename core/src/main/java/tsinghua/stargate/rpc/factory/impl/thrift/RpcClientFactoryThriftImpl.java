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

package tsinghua.stargate.rpc.factory.impl.thrift;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.InetSocketAddress;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import tsinghua.stargate.Log;
import tsinghua.stargate.conf.Configuration;
import tsinghua.stargate.exception.StarGateRuntimeException;
import tsinghua.stargate.rpc.ApplicationStarGateProtocol;
import tsinghua.stargate.rpc.factory.RpcClientFactory;
import tsinghua.stargate.rpc.thrift.ApplicationStarGateProtocolClientThriftImpl;
import tsinghua.stargate.rpc.workhorse.RpcManager;
import tsinghua.stargate.util.ReflectionUtils;

/**
 * This class is the implementation of {@link RpcClientFactory
 * RpcClientFactory}.
 * 
 * <p>
 * {@code RpcClientFactoryThriftImpl RpcClientFactoryThriftImpl} is used for
 * creating different Thrift client proxy for different protocols, e.g.,
 * {@link ApplicationStarGateProtocolClientThriftImpl
 * ApplicationStarGateProtocolClientThriftImpl} is the client proxy of
 * {@link ApplicationStarGateProtocol ApplicationStarGateProtocol}.
 * 
 * @see RpcClientFactory
 */
public class RpcClientFactoryThriftImpl extends Log
    implements RpcClientFactory {

  private static final String IMPL_THRIFT_CLIENT_SUFFIX = "thrift";
  private static final String CLIENT_THRIFT_IMPL_SUFFIX = "ClientThriftImpl";

  private final Configuration selfConf = new Configuration();

  private final ConcurrentMap<Class<?>, Constructor<?>> cache =
      new ConcurrentHashMap<>();

  private static final RpcClientFactoryThriftImpl self =
      new RpcClientFactoryThriftImpl();

  private RpcClientFactoryThriftImpl() {
  }

  public static RpcClientFactoryThriftImpl get() {
    return RpcClientFactoryThriftImpl.self;
  }

  public Object getClient(Class<?> protocol, long version,
      InetSocketAddress address, Configuration conf) {
    Constructor<?> constructor = cache.get(protocol);
    if (constructor == null) {
      debug("Client proxy for {} not found, creating it",
          protocol.getSimpleName());
      Class<?> protocolThriftImplClazz;
      try {
        protocolThriftImplClazz =
            selfConf.getClassByName(getThriftImplClassName(protocol,
                IMPL_THRIFT_CLIENT_SUFFIX, CLIENT_THRIFT_IMPL_SUFFIX));
      } catch (ClassNotFoundException e) {
        throw new StarGateRuntimeException(
            "Failed to load class: [" + getThriftImplClassName(protocol,
                IMPL_THRIFT_CLIENT_SUFFIX, CLIENT_THRIFT_IMPL_SUFFIX) + "]",
            e);
      }
      try {
        constructor = protocolThriftImplClazz.getConstructor(long.class,
            InetSocketAddress.class, Configuration.class);
        constructor.setAccessible(true);
        debug("Caching newly created client proxy {}",
            protocolThriftImplClazz.getSimpleName());
        cache.putIfAbsent(protocol, constructor);
      } catch (NoSuchMethodException e) {
        throw new StarGateRuntimeException(
            "Could not find constructor with params: " + long.class + ", "
                + InetSocketAddress.class + ", " + Configuration.class,
            e);
      }
    }
    try {
      return constructor.newInstance(version, address, conf);
    } catch (InvocationTargetException e) {
      throw new StarGateRuntimeException(e);
    } catch (IllegalAccessException e) {
      throw new StarGateRuntimeException(e);
    } catch (InstantiationException e) {
      throw new StarGateRuntimeException(e);
    }
  }

  public void stopClient(Object proxy) {
    RpcManager.stopProxy(proxy);
  }

  private String getThriftImplClassName(Class<?> clazz, String packageSuffix,
      String classSuffix) {
    return ReflectionUtils.get().getFullName(clazz, packageSuffix, classSuffix);
  }
}
