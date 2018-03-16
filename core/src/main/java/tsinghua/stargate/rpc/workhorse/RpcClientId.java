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

import java.nio.ByteBuffer;
import java.util.UUID;

import com.google.common.base.Preconditions;

/**
 * A class defining a set of static helper methods to provide a conversion
 * between <code>byte[]</code> and <code>String</code> for the UUID-based
 * <code>RpcClientId</code>.
 */
public class RpcClientId {

  /** The byte array length of a <code>RpcClientId</code> should be 16 */
  private static final int BYTE_ARRAY_LENGTH = 16;

  private static final int AMOUNT_OF_SHIFT = 8;

  /** Return a clientId as byte[] */
  static byte[] getClientId() {
    UUID uuid = UUID.randomUUID();
    ByteBuffer buf = ByteBuffer.wrap(new byte[BYTE_ARRAY_LENGTH]);
    buf.putLong(uuid.getMostSignificantBits());
    buf.putLong(uuid.getLeastSignificantBits());
    return buf.array();
  }

  /**
   * Convert the <code>RpcClientId</code> from <code>String</code> to
   * <code>byte[]</code>.
   *
   * @param clientId represented as a <code>String</code>
   * @return a <code>byte[]</code> client ID
   */
  public static byte[] toByteArray(String clientId) {
    if (clientId == null || "".equals(clientId))
      return new byte[0];
    UUID uuid = UUID.fromString(clientId);
    ByteBuffer buf = ByteBuffer.wrap(new byte[BYTE_ARRAY_LENGTH]);
    buf.putLong(uuid.getMostSignificantBits());
    buf.putLong(uuid.getLeastSignificantBits());
    return buf.array();
  }

  /**
   * Convert the <code>RpcClientId</code> from <code>byte[]</code> to
   * <code>String</code>.
   *
   * @param clientId represented as a <code>byte[]</code>
   * @return a StringRep client ID
   */
  public static String toString(byte[] clientId) {
    if (clientId == null || clientId.length == 0)
      return "";
    Preconditions.checkArgument(clientId.length == BYTE_ARRAY_LENGTH);
    long msb = getMsb(clientId);
    long lsb = getLsb(clientId);
    return (new UUID(msb, lsb)).toString();
  }

  private static long getMsb(byte[] clientId) {
    long msb = 0;
    for (int i = 0; i < BYTE_ARRAY_LENGTH / 2; ++i)
      msb = (msb << AMOUNT_OF_SHIFT) | (clientId[i] & 0xff);
    return msb;
  }

  private static long getLsb(byte[] clientId) {
    long lsb = 0;
    for (int i = BYTE_ARRAY_LENGTH / 2; i < BYTE_ARRAY_LENGTH; ++i)
      lsb = (lsb << AMOUNT_OF_SHIFT) | (clientId[i] & 0xff);
    return lsb;
  }
}
