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

package tsinghua.stargate.rpc.workhorse;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import javax.net.SocketFactory;

import org.apache.hadoop.ipc.RemoteException;
import org.apache.hadoop.net.NetUtils;
import org.apache.hadoop.util.Time;
import org.apache.thrift.protocol.TCompactProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TFramedTransport;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransport;
import org.apache.thrift.transport.TTransportException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import tsinghua.stargate.conf.Configuration;
import tsinghua.stargate.conf.NameSpace;
import tsinghua.stargate.exception.StarGateRuntimeException;
import tsinghua.stargate.io.RpcIO;
import tsinghua.stargate.rpc.thrift.ApplicationStarGateProtocolService;
import tsinghua.stargate.rpc.thrift.ThriftRpcEngine;
import tsinghua.stargate.rpc.thrift.ApplicationStarGateProtocolService.Iface;
import tsinghua.stargate.util.ThreadUtils;

/**
 * A client for an RPC service which runs on a Thrift transport or a socket and
 * is defined by a RPC request (i.e. the parameter {@code Class} object) and a
 * RPC response (i.e. the returned result {@code Class} object). In a word, RPC
 * calls input a single {@link RpcIO} as a parameter, and output a {@code RpcIO}
 * as a result.
 */
public class RpcClient {

  static final Logger LOG = LoggerFactory.getLogger(RpcClient.class);

  /** A counter for producing all handle IDs. */
  private static final AtomicInteger callIdCounter = new AtomicInteger();
  /** A variable employed by all clients is used for storing handle IDs. */
  private static final ThreadLocal<Integer> callId = new ThreadLocal<>();
  /** A variable employed by all clients is used for storing retry counts. */
  private static final ThreadLocal<Integer> retryCount = new ThreadLocal<>();
  /** The only {@code Executor} factory for all clients. */
  private final static ClientExecutorFactory CLIENT_EXECUTOR_FACTORY =
      new ClientExecutorFactory();
  /** A {@code RpcClientId} uniquely identifies a {@code Client} instance. */
  private final byte[] id;
  /** {@code Client}'s configuration profiler. */
  private final Configuration conf;
  /** The max time used for creating a connection. */
  private final int connectionTimeout;
  /** A cache for storing connections. */
  private final Hashtable<ConnectionId, Connection> CONNECTION_CACHE =
      new Hashtable<>();
  /**
   * Executor on which RPC calls' parameters are sent. Deferring the sending of
   * parameters to a separate thread isolates them from thread interruptions in
   * the calling code.
   */
  private final ExecutorService rpcRequestDispatcher;
  /** {@code Client}'s reference count. */
  private int refCount = 1;
  /** Whether {@code Client} is running or not. */
  private AtomicBoolean running = new AtomicBoolean(true);
  /**
   * A {@code SocketFactory} for creating sockets to thriftConnect Hadoop-Yarn.
   */
  private SocketFactory socketFactory;
  /** The {@code Class} object of the value within a RPC response. */
  private Class<? extends RpcIO<?, ?>> rpcResponseValueClass;

  /**
   * Construct a RPC client whose values are of the given {@link RpcIO} class.
   *
   * @param conf a user-defined or system-default configuration
   * @param socketFactory a user-defined or system-default socket factory
   * @param rpcRespValClass a {@code Class} object of RPC response values
   */
  public RpcClient(Configuration conf, SocketFactory socketFactory,
      Class<? extends RpcIO<?, ?>> rpcRespValClass) {
    this.id = RpcClientId.getClientId();
    this.conf = conf;
    this.socketFactory = socketFactory;
    this.rpcResponseValueClass = rpcRespValClass;
    this.connectionTimeout =
        conf.getInt(NameSpace.RPC_CLIENT_CONNECTION_TIMEOUT,
            NameSpace.DEFAULT_RPC_CLIENT_CONNECTION_TIMEOUT);
    this.rpcRequestDispatcher = CLIENT_EXECUTOR_FACTORY.refAndGetInstance();
  }

  /**
   * Set the ping interval value in <code>StarGateConfiguration</code>.
   *
   * @param conf the user-defined configuration profiler
   * @param pingInterval how often sends a ping
   */
  public static void setPingInterval(Configuration conf, int pingInterval) {
    conf.setInt(NameSpace.RPC_CLIENT_PING_INTERVAL, pingInterval);
  }

  /**
   * Get the ping interval value from <code>StarGateConfiguration</code>. If not
   * set, return the default value.
   *
   * @param conf the user-defined configuration profiler
   * @return the ping interval value
   */
  public static int getPingInterval(Configuration conf) {
    return conf.getInt(NameSpace.RPC_CLIENT_PING_INTERVAL,
        NameSpace.DEFAULT_RPC_CLIENT_PING_INTERVAL);
  }

  /**
   * Return the next valid sequential handle ID by incrementing an atomic
   * counter and masking off the sign bit. Valid handle IDs are non-negative
   * integers in the range [0, 2^31 - 1]. Negative numbers are reserved for
   * special purposes. The values can overflow back to 0 and be reused. Note
   * that prior versions of the client did not mask off the sign bit, so a
   * server may still see a negative handle ID if it receives connections from
   * an old client.
   *
   * @return next handle ID
   */
  public static int nextCallId() {
    return callIdCounter.getAndIncrement() & 0x7FFFFFFF;
  }

  /**
   * Make a handle, passing {@code rpcRequest}, to the RPC server defined by
   * {@code connectionId}, returning the RPC response.
   *
   * @param rpcType the RPC type
   * @param rpcRequest contains serialized method and method parameters
   * @param connectionId the target RPC server
   * @return the RPC response
   * @throws IOException if there are network problems or if the remote code
   *           throw an exception
   */
  public RpcIO<?, ?> call(RpcManager.RpcType rpcType, RpcIO<?, ?> rpcRequest,
      ConnectionId connectionId) throws IOException {
    final Call call = createCall(rpcType, rpcRequest);
    Connection connection = getConnection(connectionId, call);
    LOG.debug("Achieve the connection: {}", connection);
    try {
      connection.sendRequest(call);
    } catch (RejectedExecutionException e) {
      throw new IOException("Connection has been closed", e);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      LOG.warn("Interrupted waiting to send rpc request to server", e);
      throw new IOException(e);
    }
    boolean interrupted = false;
    synchronized (call) {
      while (!call.rpcCompletion) {
        try {
          call.wait();
        } catch (InterruptedException e) {
          interrupted = true;
        }
      }
      if (interrupted)
        Thread.currentThread().interrupt();
      if (call.rpcError != null) {
        if (call.rpcError instanceof RemoteException) {
          call.rpcError.fillInStackTrace();
          throw call.rpcError;
        } else {
          InetSocketAddress address = connection.getServerAddress();
          throw NetUtils.wrapException(address.getHostName(), address.getPort(),
              NetUtils.getHostname(), 0, call.rpcError);
        }
      } else {
        return call.getRpcResponse();
      }
    }

  }

  Call createCall(RpcManager.RpcType rpcType, RpcIO<?, ?> rpcRequest) {
    return new Call(rpcType, rpcRequest);
  }

  /**
   * Get a connection from the pool, or create a new one and add it to the pool.
   * Connections to a given {@code ConnectionId} are reused. Add the passing
   * {@code handle} into the {@code callQueue} via the achieved connection.
   *
   * @param connectionId the identifier of an {@code Connection}
   * @param call the identifier of a RPC handle
   * @return the cached or a new {@code Connection}
   * @throws IOException
   */
  private Connection getConnection(ConnectionId connectionId, Call call)
      throws IOException {
    if (!running.get())
      throw new IOException("The RPC client has been stopped");
    Connection connection;
    do {
      synchronized (CONNECTION_CACHE) {
        connection = CONNECTION_CACHE.get(connectionId);
        if (connection == null) {
          connection = new Connection(connectionId);
          CONNECTION_CACHE.put(connectionId, connection);
        }
      }
    } while (!connection.addCall(call));
    connection.setupIO();
    return connection;
  }

  /** Increment the reference counts of a {@code Client} instance. */
  synchronized void incRefCount() {
    ++refCount;
  }

  /** Decrement the reference counts of a {@code Client} instance. */
  synchronized void decRefCount() {
    --refCount;
  }

  /**
   * Justify if current client has no reference.
   *
   * @return true if current client has no reference, false otherwise
   */
  synchronized boolean isZeroRef() {
    return refCount == 0;
  }

  SocketFactory getSocketFactory() {
    return socketFactory;
  }

  /**
   * Stop all threads related to this client. No further calls may be made using
   * this client.
   */
  public void stop() {
    if (LOG.isDebugEnabled())
      LOG.debug("Stopping client");
    if (!running.compareAndSet(true, false))
      return;
    // Interrupt all connections
    synchronized (CONNECTION_CACHE) {
      for (Connection connection : CONNECTION_CACHE.values())
        connection.interrupt();
    }
    // Wait until all connections are closed
    while (!CONNECTION_CACHE.isEmpty())
      try {
        Thread.sleep(100);
      } catch (InterruptedException e) {
        throw new StarGateRuntimeException(
            "Failed to stop client since interruption", e);
      }
    // This client declare exit for the global executor
    CLIENT_EXECUTOR_FACTORY.deRefAndCleanup();
  }

  /**
   * This class is only used for creating the {@code Executor} that can be
   * employed to send RPC requests.
   */
  private static class ClientExecutorFactory {

    private int executorRefCount = 0;

    private ExecutorService clientExecutor = null;

    /**
     * Get an <code>Executor</code> on which RPC calls' parameters are sent. If
     * the internal reference counter is zero, this method creates the instance
     * of <code>Executor</code>. Otherwise, it just returns the reference of
     * clientExecutor.
     *
     * @return an <code>ExecutorService</code> instance
     */
    synchronized ExecutorService refAndGetInstance() {
      if (executorRefCount == 0) {
        clientExecutor = ThreadUtils.getExecutor()
            .newDaemonCachedThreadPool("RPC client launch executor", 5);
      }
      ++executorRefCount;
      return clientExecutor;
    }

    /**
     * Cleanup the {@code clientExecutor} on which RPC calls' parameters are
     * sent. If reference counter is zero, this method discards the instance of
     * the executor. Else, this method just decrements the internal reference
     * counter.
     *
     * @return an <code>ExecutorService</code> instance if it exists. Null is
     *         returned if not
     */
    synchronized ExecutorService deRefAndCleanup() {
      --executorRefCount;
      assert (executorRefCount >= 0);
      if (executorRefCount == 0) {
        clientExecutor.shutdown();
        try {
          if (!clientExecutor.awaitTermination(1, TimeUnit.MINUTES))
            clientExecutor.shutdownNow();
        } catch (InterruptedException e) {
          LOG.error("Interrupted while waiting for clientExecutor to stop", e);
          clientExecutor.shutdownNow();
        }
        clientExecutor = null;
      }
      return clientExecutor;
    }
  }

  /**
   * Class that represents a RPC handle.
   */
  public static class Call {
    final int id; // A handle id that uniquely identifies a {@code Call}
                  // instance
    final int retry; // Retry count
    final RpcManager.RpcType rpcType; // RPC engine type
    final RpcIO<?, ?> rpcRequest; // Serialized RPC request
    RpcIO<?, ?> rpcResponse; // Serialized RPC response, null if RPC has error
    boolean rpcCompletion; // true if handle is done, false else
    IOException rpcError; // exception, null if success

    private Call(RpcManager.RpcType type, RpcIO<?, ?> request) {
      this.rpcType = type;
      this.rpcRequest = request;

      final Integer id = callId.get();
      if (id == null)
        this.id = nextCallId();
      else {
        callId.set(null);
        this.id = id;
      }

      final Integer rc = retryCount.get();
      if (rc == null)
        this.retry = 0;
      else
        this.retry = rc;
    }

    /**
     * Indicate when the handle is done and the value or error are available.
     * Notify by default.
     */
    protected synchronized void done() {
      this.rpcCompletion = true;
      notify();
    }

    public synchronized void setError(IOException error) {
      this.rpcError = error;
      done();
    }

    public synchronized RpcIO<?, ?> getRpcResponse() {
      return rpcResponse;
    }

    /**
     * Set the RPC result when there is no error. Notify the caller that the
     * handle is done.
     *
     * @param response returned value of the RPC handle
     */
    public synchronized void setRpcResponse(RpcIO<?, ?> response) {
      this.rpcResponse = response;
      done();
    }
  }

  /**
   * A {@code ConnectionId} wraps RPC protocols and server addresses. Note: the
   * connections between RPC Client and Server are uniquely identified by
   * <li>{@code protocol} and {@code serverAddress}</li>, i.e.
   * {@code ConnectionId}.
   */
  public static class ConnectionId {

    private static final int PRIME = 16777619;
    /** The Exchanging information in this connection. */
    private final Class<?> protocol;
    /** Remote location. */
    private final InetSocketAddress serverAddress;
    /** A configuration profiler. */
    private final Configuration conf;
    /**
     * The max. no. of time interval (in millisecond) to have a successful RPC.
     * Once exceed, it means that this RPC has failed.
     */
    private final int rpcTimeout;
    /**
     * The max. no. of idle time (in millisecond) in one connection. Once
     * exceed, the connection would be culled.
     */
    private final int maxIdleTime;
    /** Disable Nagle's algorithm if true, otherwise enable. */
    private final boolean tcpNoDelay;
    /** Use ping to test if a connection keeps alive. */
    private final boolean usePing;
    /** How often sends ping to server (in millisecond). */
    private final int pingInterval;

    public ConnectionId(Class<?> protocol, InetSocketAddress address,
        Configuration conf, int rpcTimeout) {
      this.protocol = protocol;
      this.serverAddress = address;
      this.conf = conf;
      this.rpcTimeout = rpcTimeout;
      this.maxIdleTime =
          conf.getInt(NameSpace.RPC_CLIENT_CONNECTION_MAXIDLETIME,
              NameSpace.DEFAULT_RPC_CLIENT_CONNECTION_MAXIDLETIME);
      this.tcpNoDelay =
          conf.getBoolean(NameSpace.RPC_CLIENT_CONNECTION_TCPNODELAY,
              NameSpace.DEFAULT_RPC_CLIENT_CONNECTION_TCPNODELAY);
      this.usePing = conf.getBoolean(NameSpace.RPC_CLIENT_PING,
          NameSpace.DEFAULT_RPC_CLIENT_PING);
      this.pingInterval = (usePing ? RpcClient.getPingInterval(conf) : 0);
    }

    /**
     * Return a new <code>ConnectionId</code> instance.
     *
     * @param protocol the <code>Class</code> object of a RPC protocol
     * @param address remote address for connection
     * @param conf a configuration profiler
     * @param rpcTimeout timeout value for each rpc; 0 means no timeout
     * @return a newly created <code>ConnectionId</code> instance
     */
    public static ConnectionId getConnectionId(Class<?> protocol,
        InetSocketAddress address, Configuration conf, int rpcTimeout) {
      return new ConnectionId(protocol, address, conf, rpcTimeout);
    }

    private static boolean isEqual(Object a, Object b) {
      return a == null ? b == null : a.equals(b);
    }

    public Class<?> getProtocol() {
      return protocol;
    }

    public InetSocketAddress getServerAddress() {
      return serverAddress;
    }

    public int getRpcTimeout() {
      return rpcTimeout;
    }

    public int getMaxIdleTime() {
      return maxIdleTime;
    }

    public boolean isTcpNoDelay() {
      return tcpNoDelay;
    }

    public boolean isUsePing() {
      return usePing;
    }

    public int getPingInterval() {
      return pingInterval;
    }

    @Override
    public boolean equals(Object obj) {
      if (obj == this) {
        return true;
      }
      if (obj instanceof ConnectionId) {
        ConnectionId that = (ConnectionId) obj;
        return isEqual(this.protocol, that.protocol)
            && isEqual(this.serverAddress, that.serverAddress)
            && this.rpcTimeout == that.rpcTimeout
            && this.maxIdleTime == that.maxIdleTime
            && this.tcpNoDelay == that.tcpNoDelay
            && this.usePing == that.usePing
            && this.pingInterval == that.pingInterval;
      }
      return false;
    }

    @Override
    public int hashCode() {
      int result = ((protocol == null) ? 0 : protocol.hashCode());
      result = PRIME * result
          + ((serverAddress == null) ? 0 : serverAddress.hashCode());
      result = PRIME * result + rpcTimeout;
      result = PRIME * result + maxIdleTime;
      result = PRIME * result + (tcpNoDelay ? 1231 : 1237);
      result = PRIME * result + (usePing ? 1231 : 1237);
      result = PRIME * result + pingInterval;
      return result;
    }

    @Override
    public String toString() {
      return serverAddress.toString();
    }
  }

  /**
   * <code>Connection</code> is a <code>Thread</code> that reads responses and
   * notifies callers. Each connection owns a socket connected to a server
   * address. Calls are multiplexed through this socket: responses may be
   * delivered out of order.
   */

  // TODO: add run method
  private class Connection extends Thread {

    private final ConnectionId connectionId;
    private final Object sendRequestLock = new Object();

    private int maxFrameLength;
    private final String thriftServiceName;

    private InetSocketAddress serverAddress;
    private int rpcTimeout;
    private int maxIdleTime;
    private boolean tcpNoDelay;
    private boolean doPing;
    private int pingInterval;
    private ByteArrayOutputStream pingRequest;
    private DataInputStream in;
    private DataOutputStream out;
    /** A queue used for storing currently active calls. */
    private Hashtable<Integer, Call> callQueue = new Hashtable<>();
    /** Indicate if the connection is closed. */
    private AtomicBoolean isConnectionClosed = new AtomicBoolean();
    /** Why connection is closed? */
    private IOException closeCause;
    /** Latest I/O activity time. */
    private AtomicLong latestIOTime = new AtomicLong();
    private TProtocol mp;
    private ApplicationStarGateProtocolService.Client thriftClient;

    Connection(ConnectionId connectionId) throws IOException {
      this.connectionId = connectionId;
      this.serverAddress = connectionId.getServerAddress();
      this.rpcTimeout = connectionId.getRpcTimeout();
      this.maxIdleTime = connectionId.getMaxIdleTime();
      this.tcpNoDelay = connectionId.isTcpNoDelay();
      this.doPing = connectionId.isUsePing();
      if (doPing) { // Construct a RPC header with the callId as the ping callId
        pingRequest = new ByteArrayOutputStream();
      }
      this.maxFrameLength =
          (int) conf.getLong(NameSpace.RPC_THRIFT_FRAME_LENGTH_MAX,
              NameSpace.DEFAULT_RPC_THRIFT_FRAME_LENGTH_MAX);
      this.thriftServiceName = connectionId.getProtocol().getSimpleName();
    }

    /**
     * Add a handle into the connection's accelerate queue and notify a
     * listener.
     *
     * @param call pending to add into {@code callQueue}
     * @return true if the handle is added, false otherwise
     */
    private synchronized boolean addCall(Call call) {
      if (isConnectionClosed.get())
        return false;
      callQueue.put(call.id, call);
      notify();
      return true;
    }

    /**
     * Prepare the necessary conditions for connecting to the remote server,
     * e.g. creating sockets, creating I/O streams, setting their hooked options
     * and so on. The constructed RPC requests are sent via output streams and
     * the RPC responses are received via input streams. Note: if connections
     * are setup using Thrift, StarGate can employ Thrift's internal connection
     * mechanism. Otherwise, StarGate makes use of sockets to setup connections.
     */
    private synchronized void setupIO() {
      if (isConnectionClosed.get() || null != thriftClient)
        return;
      try {
        // Connect to RPC server via Thrift
        thriftConnect();

        // Update last activity time
        touch();

        // Start the receiver thread after the connection has been setup
        start();
      } catch (Throwable t) {
        if (t instanceof IOException)
          markClosed((IOException) t);
        else
          markClosed(new IOException("Couldn't setup IO", t));
        close();
      }
    }

    /**
     * Connect to RPC Server via Thrift.
     *
     * @throws IOException
     */
    private synchronized void thriftConnect()
        throws IOException, TTransportException {
      TSocket socket =
          new TSocket(serverAddress.getAddress().getCanonicalHostName(),
              serverAddress.getPort());
      TTransport transport = new TFramedTransport(socket, maxFrameLength);
      transport.open();

      TProtocol protocol = new TCompactProtocol(transport);
      // mp = new TMultiplexedProtocol(protocol, thriftServiceName);

      thriftClient = new ApplicationStarGateProtocolService.Client(protocol);
    }

    /**
     * Initiate a RPC handle by sending the RPC request to the remote server.
     * NOTE: this is not called from the {@code Connection} thread, but by the
     * {@code rpcRequestDispatcher} executor.
     *
     * @param call an object wrapping RPC requests
     * @throws InterruptedException
     * @throws IOException
     */
    public void sendRequest(final Call call)
        throws InterruptedException, IOException {
      if (isConnectionClosed.get())
        return;

      // Serialize the {@code handle} to be sent, which is done from the actual
      // caller thread, rather than the {@code rpcRequestDispatcher} thread so
      // that if the serialization throws an error, it is reported properly.
      // This also parallelizes the serialization.
      // Format of a handle on the wire (length of rest below):
      // 1) {@code RpcRequestHeader}
      // 2) {@code RpcRequest}
      synchronized (sendRequestLock) {
        LOG.debug("Sending RPC requests...");
        Future<?> sendFuture = rpcRequestDispatcher.submit(new Runnable() {
          @Override
          public void run() {
            try {
              synchronized (call) {
                if (isConnectionClosed.get())
                  return;
                LOG.debug("Set handle: {}", call);
                ((ThriftRpcEngine.RpcRequestThrift) call.rpcRequest)
                    .setCall(call);
                ((RpcIO<Iface, Iface>) call.rpcRequest).write(thriftClient);
              }
            } catch (IOException e) {
              // Exception at this point would leave the connection in an
              // unrecoverable state (e.g. half a handle left on the wire). So,
              // close the connection, killing any outstanding calls.
              markClosed(e);
            } finally {

            }
          }
        });

        try {
          sendFuture.get();
        } catch (ExecutionException e) {
          Throwable cause = e.getCause();
          // Cause should only be a {@code RuntimeException} as the {@code
          // Runnable} above catches {@code IOException}.
          if (cause instanceof RuntimeException) {
            throw (RuntimeException) cause;
          } else {
            throw new RuntimeException("Unexpected checked exception", cause);
          }
        }
      }
    }

    private synchronized boolean updateAddress() throws IOException {
      // Do a fresh lookup with the old host name.
      InetSocketAddress curAddress = NetUtils.createSocketAddrForHost(
          serverAddress.getHostName(), serverAddress.getPort());
      if (!serverAddress.equals(curAddress)) {
        LOG.warn("Address change detected. Old: " + serverAddress.toString()
            + " New: " + curAddress.toString());
        serverAddress = curAddress;
        return true;
      }
      return false;
    }

    private void touch() {
      latestIOTime.set(Time.now());
    }

    private InetSocketAddress getServerAddress() {
      return serverAddress;
    }

    private synchronized void markClosed(IOException e) {
      if (isConnectionClosed.compareAndSet(false, true)) {
        closeCause = e;
        notifyAll();
      }
    }

    private synchronized void close() {
      if (!isConnectionClosed.get()) {
        LOG.error("The connection is not in the closed state");
        return;
      }

      // Remove current connection out of the connection cache
      synchronized (CONNECTION_CACHE) {
        if (this == CONNECTION_CACHE.get(connectionId))
          CONNECTION_CACHE.remove(connectionId);
      }

      // Clean up handle queue
      if (closeCause == null) {
        if (!callQueue.isEmpty()) {
          LOG.warn(
              "A connection is closed for no cause and handle queue is not empty");
          closeCause = new IOException("Unexpected closed connection");
          windupCalls();
        }
      } else {
        windupCalls();
      }
      disconnect();
    }

    /**
     * Cleanup the {@link #callQueue} and notify its all elements the close
     * reason and then mark them as done.
     */
    private void windupCalls() {
      Iterator<Entry<Integer, Call>> it = callQueue.entrySet().iterator();
      while (it.hasNext()) {
        Call call = it.next().getValue();
        it.remove();
        call.setError(closeCause);
      }
    }

    private void disconnect() {
      if (thriftClient.getInputProtocol() != null) {
        thriftClient.getInputProtocol().getTransport().close();
      }

      if (thriftClient.getOutputProtocol() != null) {
        thriftClient.getOutputProtocol().getTransport().close();
      }
    }
  }
}
