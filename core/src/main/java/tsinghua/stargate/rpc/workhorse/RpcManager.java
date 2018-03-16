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

package tsinghua.stargate.rpc.workhorse;

import java.io.Closeable;
import java.io.IOException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;

import javax.net.SocketFactory;

import tsinghua.stargate.conf.Configuration;
import tsinghua.stargate.exception.StarGateIllegalArgumentException;
import tsinghua.stargate.exception.StarGateRuntimeException;
import tsinghua.stargate.rpc.RpcEngine;
import tsinghua.stargate.rpc.RpcEngine.RpcDynamicProxyHandler;
import tsinghua.stargate.rpc.thrift.ThriftRpcEngine;
import tsinghua.stargate.util.ReflectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A simple RPC mechanism.
 *
 * <p>
 * A <i>protocol</i> is a Java interface. All parameters and return types must
 * be one of:
 * <ul>
 * <li>A primitive type: <code>boolean</code>, <code>byte</code>,
 * <code>char</code>, <code>short</code>, <code>int</code>, <code>long</code>,
 * <code>float</code>, <code>double</code>, or <code>void</code>; or</li>
 * <li>A <code>String</code>; or</li>
 * <li>A <code>Writable</code>; or</li>
 * <li>An array of the above types.</li>
 *
 * <p>
 * All methods in the protocol should throw only <cod>IOException</cod>. No
 * field data of the protocol instance is transmitted.
 */
public class RpcManager {

  private static final Logger LOG = LoggerFactory.getLogger(RpcManager.class);

  private static final String RPC_ENGINE_PREFIX = "stargate.ipc.rpc.engine";
  private static final Map<Class<?>, RpcEngine> RPC_ENGINE_CACHE =
      new HashMap<>();

  private RpcManager() {
  }

  /**
   * Get a client proxy that implements the protocol, communicating with the
   * server specified by {@code address}.
   *
   * @param protocol the <code>Class</code> object of a RPC protocol
   * @param clientVersion the current version of client
   * @param address the <code>StarGateDaemon</code> location
   * @param conf a configuration profiler
   * @param factory the factory for creating sockets
   * @param rpcTimeout timeout value for each RPC, 0 means no timeout
   * @param <T> protocol type
   * @return a newly created proxy
   * @throws IOException if some I/O errors occur
   */
  public static <T> T getProxy(Class<T> protocol, long clientVersion,
      InetSocketAddress address, Configuration conf, SocketFactory factory,
      int rpcTimeout) throws IOException {
    return getProtocolProxy(protocol, clientVersion, address, conf, factory,
        rpcTimeout).getProxy();
  }

  /**
   * Get a protocol proxy that contains a proxy connection to a remote server
   * and a set of methods that are supported by the protocol and server.
   *
   * @param protocol the <code>Class</code> object of a RPC protocol
   * @param clientVersion the current version of client
   * @param address the <code>StarGateDaemon</code> location
   * @param conf a configuration profiler
   * @param factory the factory for creating sockets
   * @param rpcTimeout timeout value for each RPC, 0 means no timeout
   * @param <T> protocol type
   * @return a newly created {@code ProtocolProxy} instance
   * @throws IOException if some I/O errors occur
   */
  private static <T> ProtocolProxy<T> getProtocolProxy(Class<T> protocol,
      long clientVersion, InetSocketAddress address, Configuration conf,
      SocketFactory factory, int rpcTimeout) throws IOException {
    return getRpcEngine(protocol, conf).getProxy(protocol, clientVersion,
        address, conf, factory, rpcTimeout);
  }

  /**
   * Get the corresponding {@code RpcEngine} of a specific protocol.
   *
   * @param protocol pending to be processed by corresponding {@code RpcEngine}
   * @param conf a configuration profiler
   * @return a {@code RpcEngine} that can handle the given protocol
   */
  static synchronized RpcEngine getRpcEngine(Class<?> protocol,
      Configuration conf) {
    RpcEngine engine = RPC_ENGINE_CACHE.get(protocol);
    if (engine == null) {
      Class<?> engineImpl = conf.getClass(
          RPC_ENGINE_PREFIX + "." + protocol.getName(), ThriftRpcEngine.class);
      engine = (RpcEngine) ReflectionUtils.get().getInstance(engineImpl);
      RPC_ENGINE_CACHE.put(protocol, engine);
    }
    return engine;
  }

  /**
   * Set a protocol to use a non-default {@code RpcEngine}.
   *
   * @param conf the user-defined configuration profiler
   * @param protocol pending to be processed by corresponding {@code RpcEngine}
   * @param engine a {@code RpcEngine} that can handle the given protocol
   */
  public static void setRpcEngine(Configuration conf, Class<?> protocol,
      Class<?> engine) {
    conf.setClass(RPC_ENGINE_PREFIX + "." + protocol.getName(), engine,
        RpcEngine.class);
  }

  /**
   * Stop the proxy that implements {@code Closeable} or has associated
   * {@link RpcDynamicProxyHandler}.
   *
   * @param proxy the RPC proxy object to be stopped
   */
  public static void stopProxy(Object proxy) {
    if (proxy == null)
      throw new StarGateIllegalArgumentException(
          "Cannot close proxy since it is null");
    try {
      if (proxy instanceof Closeable) {
        ((Closeable) proxy).close();
        return;
      } else {
        InvocationHandler handler = Proxy.getInvocationHandler(proxy);
        if (handler instanceof Closeable) {
          ((Closeable) handler).close();
          return;
        }
      }
    } catch (IOException e) {
      LOG.error("Closing proxy or invocation handler caused exception", e);
      throw new StarGateRuntimeException(e);
    } catch (IllegalArgumentException e) {
      LOG.error("Call on non proxy: " + proxy.getClass(), e);
      throw new StarGateIllegalArgumentException(
          "Cannot close proxy which is not Closeable or "
              + "does not provide closeable invocation handler "
              + proxy.getClass());
    }
  }

  /**
   * Get the name of the given {@code protocol}.
   *
   * @param protocol the <code>Class</code> object of a RPC protocol
   * @return a {@code protocol} StringRep
   */
  public static String getProtocolName(Class<?> protocol) {
    if (protocol == null)
      return null;
    return protocol.getName();
  }

  public enum RpcType {
    RPC_PROTOCOL_BUFFER((short) 1), // Use PbRPCEngine
    RPC_THRIFT((short) 2); // Use ThriftRpcEngine

    private final short value;

    RpcType(short value) {
      this.value = value;
    }
  }

  public static class Builder {

    private Configuration conf;

    private InetSocketAddress address;
    private int ioQueueSize;
    private int ioThreads;
    private int workerThreads;

    private Class<?> protocol;
    private Object processor;

    public Builder(Configuration conf) {
      this.conf = conf;
    }

    public Builder setProcessor(Object processor) {
      this.processor = processor;
      return this;
    }

    public Builder setAddress(InetSocketAddress address) {
      this.address = address;
      return this;
    }

    public Builder setIOQueueSize(int ioQueueSize) {
      this.ioQueueSize = ioQueueSize;
      return this;
    }

    public Builder setIOThreads(int ioThreads) {
      this.ioThreads = ioThreads;
      return this;
    }

    public Builder setWorkerThreads(int workerThreads) {
      this.workerThreads = workerThreads;
      return this;
    }

    public Builder setProtocol(Class<?> protocol) {
      this.protocol = protocol;
      return this;
    }

    public RpcServer build() {
      return getRpcEngine(protocol, conf).getServer(conf, address, ioQueueSize,
          ioThreads, workerThreads, protocol, processor);
    }
  }
}
