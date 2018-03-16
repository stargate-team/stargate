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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;

/**
 * It is legal to create multiple serialization or deserialization streams from
 * the same {@code Serializer} as long as those streams are all used within the
 * same thread.
 */
public interface Serializer {

  ByteBuffer serialize(Object obj) throws IOException;

  Object deserialize(ByteBuffer buffer)
      throws IOException, ClassNotFoundException;

  Object deserialize(ByteBuffer buffer, ClassLoader loader)
      throws IOException, ClassNotFoundException;

  SerializationStream getSerializationStream(OutputStream os)
      throws IOException;

  DeserializationStream getDeserializationStream(InputStream is)
      throws IOException;

  DeserializationStream getDeserializationStream(InputStream is,
      ClassLoader loader) throws IOException;
}
