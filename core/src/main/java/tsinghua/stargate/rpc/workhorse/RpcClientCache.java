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

import java.util.HashMap;
import java.util.Map;

import javax.net.SocketFactory;

import tsinghua.stargate.conf.Configuration;
import tsinghua.stargate.io.RpcIO;

/** Cache clients using their own socket factories as hash keys. */
public class RpcClientCache {

  private final Map<SocketFactory, RpcClient> CACHE = new HashMap<>();

  /**
   * Construct and cache a RPC Client with the user-provided
   * {@code SocketFactory} if no cached client exists. Otherwise, increase the
   * reference count of this cached client.
   *
   * @param conf a configuration profiler
   * @param factory a user-provided <code>SocketFactory</code>
   * @param rpcRespValClass the {@code Class} object of a RPC response
   * @return a, new or cached, client
   */
  public synchronized RpcClient getClient(Configuration conf,
      SocketFactory factory, Class<? extends RpcIO<?, ?>> rpcRespValClass) {
    RpcClient client = CACHE.get(factory);
    if (client == null) {
      client = new RpcClient(conf, factory, rpcRespValClass);
      CACHE.put(factory, client);
    } else {
      client.incRefCount();
    }
    if (RpcClient.LOG.isDebugEnabled())
      RpcClient.LOG.debug("Getting RPC client out of cache: {}", client);
    return client;
  }

  /**
   * Stop a RPC client connection. Note that a RPC client is closed only when
   * its reference count becomes zero.
   *
   * @param client is being closed
   */
  public void stopClient(RpcClient client) {
    if (RpcClient.LOG.isDebugEnabled())
      RpcClient.LOG.debug("Stopping RPC client from cache: {}", client);
    synchronized (this) {
      client.decRefCount();
      if (client.isZeroRef()) {
        if (RpcClient.LOG.isDebugEnabled())
          RpcClient.LOG.debug("Removing RPC client from cache: " + client);
        CACHE.remove(client.getSocketFactory());
      }
    }
    if (client.isZeroRef()) {
      if (RpcClient.LOG.isDebugEnabled())
        RpcClient.LOG.debug(
            "Stopping RPC client actually because no more references remain: {}",
            client);
      client.stop();
    }
  }
}
