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

/** A class wrapper around a protocol. */
public class ProtocolProxy<T> {

  /**
   * A Java interface used for exchanging information between RPC Client (i.e.
   * <code>Client</code>) and RPC Server (i.e.
   * <code>StarGateDaemon</code>).
   */
  private Class<T> protocol;

  /** A proxy object used for forwarding method invocation requests. */
  private T proxy;

  public ProtocolProxy(Class<T> protocol, T proxy) {
    this.protocol = protocol;
    this.proxy = proxy;
  }

  public T getProxy() {
    return proxy;
  }
}
