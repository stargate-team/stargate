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

package tsinghua.stargate.serializer;

import java.io.*;
import java.nio.ByteBuffer;

import tsinghua.stargate.util.ArrayUtils;

@SuppressWarnings("unchecked")
public class JavaSerializer implements Serializer {

  private static final int DEFAULT_COUNTER_RESET = 100;

  private ClassLoader defaultClassLoader =
      Thread.currentThread().getContextClassLoader();

  @Override
  public ByteBuffer serialize(Object obj) throws IOException {
    ByteArrayOutputStream bos = new ByteArrayOutputStream();
    SerializationStream out = getSerializationStream(bos);
    out.writeObject(obj);
    out.close();
    return ByteBuffer.wrap(bos.toByteArray());
  }

  @Override
  public Object deserialize(ByteBuffer buffer)
      throws IOException, ClassNotFoundException {
    ByteArrayInputStream bis =
        new ByteArrayInputStream(ArrayUtils.buffer2Bytes(buffer));
    DeserializationStream in = getDeserializationStream(bis);
    return in.readObject();
  }

  @Override
  public Object deserialize(ByteBuffer buffer, ClassLoader loader)
      throws IOException, ClassNotFoundException {
    ByteArrayInputStream bis =
        new ByteArrayInputStream(ArrayUtils.buffer2Bytes(buffer));
    DeserializationStream in = getDeserializationStream(bis, loader);
    return in.readObject();
  }

  @Override
  public SerializationStream getSerializationStream(OutputStream os)
      throws IOException {
    return new JavaSerializationStream(os, DEFAULT_COUNTER_RESET);
  }

  @Override
  public DeserializationStream getDeserializationStream(InputStream is)
      throws IOException {
    return new JavaDeserializationStream(is, this.defaultClassLoader);
  }

  @Override
  public DeserializationStream getDeserializationStream(InputStream is,
      ClassLoader loader) throws IOException {
    return new JavaDeserializationStream(is, loader);
  }
}
