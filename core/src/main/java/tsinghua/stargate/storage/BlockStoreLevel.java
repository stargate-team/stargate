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

package tsinghua.stargate.storage;

import tsinghua.stargate.exception.StarGateException;

/** Storage level of data block. */
public enum BlockStoreLevel {

  DISK,

  ALLUXIO,

  HDFS,

  IN_HEAP;

  /**
   * Get the enum type by its integer value.
   *
   * @return null if the value is not found.
   */
  public static BlockStoreLevel valueOf(Integer value)
      throws StarGateException {

    if (null == value) {
      throw new StarGateException("the store value is null");
    }
    switch (value) {
    case 0:
      return DISK;
    case 1:
      return ALLUXIO;
    case 2:
      return HDFS;
    case 3:
      return IN_HEAP;
    default:
      return null;
    }
  }

}