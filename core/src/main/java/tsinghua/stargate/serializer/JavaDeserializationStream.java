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
import java.io.ObjectInputStream;
import java.io.ObjectStreamClass;

@SuppressWarnings("unchecked")
public class JavaDeserializationStream implements DeserializationStream {

  private ObjectInputStream ois;

  public JavaDeserializationStream(InputStream ois, final ClassLoader loader)
      throws IOException {
    this.ois = new ObjectInputStream(ois) {
      @Override
      protected Class<?> resolveClass(ObjectStreamClass desc)
          throws ClassNotFoundException {
        Class<?> clazz;
        clazz = Class.forName(desc.getName(), false, loader);
        return clazz;
      }
    };

  }

  @Override
  public Object readObject() throws IOException, ClassNotFoundException {
    return ois.readObject();
  }

  @Override
  public void close() throws IOException {
    ois.close();
  }
}
