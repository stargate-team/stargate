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

import java.net.InetSocketAddress;

import org.apache.hadoop.io.Writable;

import tsinghua.stargate.Log;

/**
 * A server for an RPC service which runs on thrift and is defined by parameter
 * class and a value class. In other words, RPC calls take a single
 * {@link Writable} as a parameter, and return a <code>Writable</code> as their
 * value.
 */
public abstract class RpcServer extends Log {

  private InetSocketAddress bindAddress;
  private int ioQueueSize;
  private int ioThreads;
  private int workerThreads;

  private ServingThread servingThread = null;

  /** True while this server is running. */
  private volatile boolean running = true;
  private static final ThreadLocal<RpcServer> SERVER = new ThreadLocal<>();

  protected RpcServer() {
  }

  protected RpcServer(InetSocketAddress bindAddress, int ioQueueSize,
      int ioThreads, int workerThreads) {
    this.bindAddress = bindAddress;
    this.ioQueueSize = ioQueueSize;
    this.ioThreads = ioThreads;
    this.workerThreads = workerThreads;

    this.servingThread = new ServingThread();
  }

  protected abstract void serve();

  public void start() {
    info("Starting RPC Server on {}", bindAddress.getPort());
    servingThread.start();
  }

  public void stop() {
    info("Stopping RPC Server on {}", bindAddress.getPort());
    running = false;
    stopImpl();
  }

  protected abstract void stopImpl();

  public class ServingThread extends Thread {

    public ServingThread() {
      this.setName("Daemon Serving Thread");
      this.setDaemon(true);
    }

    @Override
    public void run() {
      info("Starting {}", Thread.currentThread().getName());
      SERVER.set(RpcServer.this);

      while (running) {
        serve();
      }
    }
  }
}
