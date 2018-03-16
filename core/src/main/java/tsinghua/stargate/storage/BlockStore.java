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

import java.io.IOException;
import java.util.Iterator;

import tsinghua.stargate.exception.StarGateException;

/**
 * This class is used for persisting data between StarGate client and server.
 */
public interface BlockStore {

  /**
   * Get data from block store specified by {@code blockId}.
   *
   * @param blockId the block id
   * @return the data to be retrieved
   * @throws IOException if data doesn't exist or cannot be read
   */

  Iterator<Object> getValues(BlockId blockId) throws IOException;

  /**
   * Put data into the block store specified by {@code blockId}.
   *
   * @param blockId the block id
   * @param values the data to be put
   * @param level the block store level
   * @return {@code true} if store block successfully, {@code false} otherwise
   * @throws IOException if putting data into block fails
   */

  boolean putValues(BlockId blockId, Iterator<Object> values,
      BlockStoreLevel level) throws IOException;

  boolean remove(BlockId blockId) throws StarGateException;
}
