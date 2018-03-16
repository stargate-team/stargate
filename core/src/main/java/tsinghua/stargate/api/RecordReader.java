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

package tsinghua.stargate.api;

import java.io.Serializable;
import java.nio.ByteBuffer;
import java.util.Map;

import tsinghua.stargate.exception.StarGateException;

public interface RecordReader extends Serializable {

  /**
   * Read the input file into bytes
   *
   * @return the current input file bytes
   */
  ByteBuffer readBytes(String path) throws StarGateException;

  /**
   * Read the file by line and return the number of bytes per row
   *
   * @return the number of bytes per row
   */
  Map<Long, Long> getPos();

  /**
   * Returns the size in the input.
   *
   * @return the size in the input.
   */
  long getSize();
}
