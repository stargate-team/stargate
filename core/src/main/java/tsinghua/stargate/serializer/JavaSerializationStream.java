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
import java.io.ObjectOutputStream;
import java.io.OutputStream;

public class JavaSerializationStream implements SerializationStream {

  private ObjectOutputStream oos;
  private int counterReset;
  private int count = 0;

  public JavaSerializationStream(OutputStream os, int counterReset)
      throws IOException {
    this.oos = new ObjectOutputStream(os);
    this.counterReset = counterReset;
  }

  @Override
  public SerializationStream writeObject(Object o) throws IOException {
    oos.writeObject(o);
    count++;
    if (count > counterReset) {
      oos.reset();
      count = 0;
    }
    return this;
  }

  @Override
  public void flush() throws IOException {
    oos.flush();
  }

  @Override
  public void close() throws IOException {
    oos.close();
  }
}
