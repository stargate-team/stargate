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
import tsinghua.stargate.rpc.factory.RpcServerFactory;
import tsinghua.stargate.rpc.thrift.ThriftRpcEngine;
import tsinghua.stargate.rpc.workhorse.RpcManager;
import tsinghua.stargate.rpc.workhorse.RpcServer;
import tsinghua.stargate.util.ReflectionUtils;

/**
 * This class is the implementation of {@link RpcServerFactory}.
 *
 * <p>
 * {@code RpcServerFactoryThriftImpl} is used for handling the server proxy of a
 * specified protocol, e.g. {@code ApplicationStarGateProtocolServiceThriftImpl}
 * is the server proxy of {@code ApplicationStarGateProtocol}.
 *
 * @see RpcServerFactory
 */
public class RpcServerFactoryThriftImpl extends Log
    implements RpcServerFactory {

  private static final String THRIFT_GEN_PACKAGE_NAME =
      "tsinghua.stargate.rpc.thrift";
  private static final String THRIFT_GEN_CLASS_SUFFIX = "Service";
  private static final String THRIFT_GEN_PROCESSOR = "Processor";
  private static final String THRIFT_SUFFIX = "thrift";
  private static final String SERVICE_THRIFT_IMPL_SUFFIX = "ServiceThriftImpl";

  private Configuration selfConf = new Configuration();

  private ConcurrentMap<Class<?>, Constructor<?>> serviceThriftImplCache =
      new ConcurrentHashMap<>();
  private ConcurrentMap<Class<?>, Constructor<?>> thriftProcessorCache =
      new ConcurrentHashMap<>();

  private static final RpcServerFactoryThriftImpl self =
      new RpcServerFactoryThriftImpl();

  private RpcServerFactoryThriftImpl() {
  }

  public static RpcServerFactoryThriftImpl get() {
    return RpcServerFactoryThriftImpl.self;
  }

  /**
   * Get a configured Thrift Server proxy for the passing {@code protocol}.
   * 
   * @param conf a configuration profiler
   * @param address server socket address for binding
   * @param ioQueueSize queue size for per IO thread
   * @param ioThreads number of IO threads
   * @param workerThreads number of worker threads
   * @param protocol a Java interface for exchanging information between RPC
   *          Client and Server
   * @param protocolImpl an instance that implements {@code protocol}
   * @return a configured Thrift Server proxy for the passing {@code protocol}.
   */
  @Override
  public RpcServer getServer(Configuration conf, InetSocketAddress address,
      int ioQueueSize, int ioThreads, int workerThreads, Class<?> protocol,
      Object protocolImpl) {
    // Get a Thrift service instance
    Constructor<?> constructor = serviceThriftImplCache.get(protocol);
    if (constructor == null) {
      debug("Server proxy for {} not found, creating it",
          protocol.getSimpleName());
      Class<?> clazz;
      try {
        clazz = Class.forName(getServiceThriftImplName(protocol));
      } catch (ClassNotFoundException e) {
        throw new StarGateRuntimeException("Failed to load class: ["
            + getServiceThriftImplName(protocol) + "]", e);
      }
      try {
        constructor = clazz.getConstructor(protocol);
        constructor.setAccessible(true);
        debug("Caching the newly created server proxy {}",
            clazz.getSimpleName());
        serviceThriftImplCache.putIfAbsent(protocol, constructor);
      } catch (NoSuchMethodException e) {
        throw new StarGateRuntimeException(
            "Could not find constructor with params: " + protocol, e);
      }
    }
    Object instance;
    try {
      instance = constructor.newInstance(protocolImpl);
    } catch (InvocationTargetException e) {
      throw new StarGateRuntimeException(e);
    } catch (IllegalAccessException e) {
      throw new StarGateRuntimeException(e);
    } catch (InstantiationException e) {
      throw new StarGateRuntimeException(e);
    }

    // Get a Thrift processor instance
    Class<?> thriftProtocol = instance.getClass().getInterfaces()[0];
    Constructor<?> processorConstructor = thriftProcessorCache.get(protocol);
    if (processorConstructor == null) {
      debug("Thrift service processor for {} not found, creating it",
          protocol.getSimpleName());
      Class<?> processorClazz;
      try {
        processorClazz = conf.getClassByName(getThriftProcessorName(protocol));
      } catch (ClassNotFoundException e) {
        throw new StarGateRuntimeException(
            "Failed to load class: [" + getThriftProcessorName(protocol) + "]",
            e);
      }
      try {
        processorConstructor =
            processorClazz.getConstructor(thriftProtocol.getInterfaces()[0]);
        processorConstructor.setAccessible(true);
        debug("Caching the newly created Thrift service processor {}",
            processorClazz.getSimpleName());
        thriftProcessorCache.putIfAbsent(protocol, processorConstructor);
      } catch (NoSuchMethodException e) {
        throw new StarGateRuntimeException(e);
      }
    }
    Object processorInstance;
    try {
      processorInstance = processorConstructor.newInstance(instance);
    } catch (InstantiationException e) {
      throw new StarGateRuntimeException(e);
    } catch (IllegalAccessException e) {
      throw new StarGateRuntimeException(e);
    } catch (InvocationTargetException e) {
      throw new StarGateRuntimeException(e);
    }

    return createServer(conf, address, ioQueueSize, ioThreads, workerThreads,
        thriftProtocol, processorInstance);
  }

  /**
   * Get the service classname that is auto generated by Thrift.
   *
   * @param protocol a Java interface for exchanging information between RPC
   *          Client and Server
   * @return the service classname that is auto generated by Thrift
   */
  private String getServiceThriftImplName(Class<?> protocol) {
    return ReflectionUtils.get().getFullName(protocol, THRIFT_SUFFIX,
        SERVICE_THRIFT_IMPL_SUFFIX);
  }

  /**
   * Get the processor classname that is auto generated by Thrift.
   *
   * @param protocol a Java interface for exchanging information between RPC
   *          Client and Server
   * @return the processor classname that is auto generated by Thrift
   */
  private String getThriftProcessorName(Class<?> protocol) {
    return ReflectionUtils.get().getFullName(protocol, THRIFT_GEN_PACKAGE_NAME,
        THRIFT_GEN_CLASS_SUFFIX, THRIFT_GEN_PROCESSOR);
  }

  /**
   * Create a configured Thrift Server proxy for the passing {@code protocol}.
   *
   * @param conf a configuration profiler
   * @param address server socket address for binding
   * @param ioQueueSize queue size for per IO thread
   * @param ioThreads number of IO threads
   * @param workerThreads number of worker threads
   * @param thriftProtocol the protocol that is auto generated by Thrift
   * @param thriftProcessor a Thrift processor instance
   * @return a configured Thrift Server proxy for the passing {@code protocol}
   */
  private RpcServer createServer(Configuration conf, InetSocketAddress address,
      int ioQueueSize, int ioThreads, int workerThreads,
      Class<?> thriftProtocol, Object thriftProcessor) {
    RpcManager.setRpcEngine(conf, thriftProtocol, ThriftRpcEngine.class);
    return new RpcManager.Builder(conf).setAddress(address)
        .setIOQueueSize(ioQueueSize).setIOThreads(ioThreads)
        .setWorkerThreads(workerThreads).setProtocol(thriftProtocol)
        .setProcessor(thriftProcessor).build();
  }
}
