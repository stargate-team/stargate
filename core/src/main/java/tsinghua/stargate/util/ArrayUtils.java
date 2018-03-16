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

package tsinghua.stargate.util;

import java.nio.ByteBuffer;

public class ArrayUtils {

  public static int bytes2Int(byte[] data) {
    ByteBuffer bytebuffer = ByteBuffer.wrap(data);
    return bytebuffer.getInt();
  }

  public static ByteBuffer bytes2Buffer(byte[] data) {
    return ByteBuffer.wrap(data);
  }

  public static byte[] buffer2Bytes(ByteBuffer buffer) {
    int length = buffer.remaining();
    byte[] bytes = new byte[length];
    buffer.get(bytes, 0, length);
    return bytes;
  }
}
