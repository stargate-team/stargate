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

import tsinghua.stargate.api.RecordReader;
import tsinghua.stargate.api.RecordWriter;
import tsinghua.stargate.exception.StarGateException;
import tsinghua.stargate.storage.impl.BlockStoreAlluxioImpl;
import tsinghua.stargate.storage.impl.BlockStoreDiskImpl;
import tsinghua.stargate.storage.impl.BlockStoreMemoryImpl;

public interface BlockManager {

  BlockStoreMemoryImpl getMemoryStore();

  BlockStoreDiskImpl getDiskStore();

  BlockStoreAlluxioImpl getAlluxioStore();

  /**
   * Stores and localizes specified {@code block}.
   * 
   * @param block the block to be stored
   * @return {@code true} if successful, {@code false} otherwise
   */
  boolean putBlock(Block block) throws StarGateException;

  /**
   * Retrieves and loads the block identified by {@code blockId} and
   * {@code level}.
   *
   * @param blockId the identification of block
   * @param storeLevel the store level of block
   * @return the block identified by {@code blockId} and {@code level}
   */
  Block getBlock(BlockId blockId, BlockStoreLevel storeLevel)
      throws StarGateException;

  boolean contain(BlockId blockId) throws IOException;

  boolean remove(BlockId blockId) throws IOException;

  void setRecordReader(RecordReader reader, BlockStoreLevel storeLevel);

  RecordReader getRecordReader(BlockStoreLevel storeLevel);

  void setRecordWriter(RecordWriter writer, BlockStoreLevel storeLevel);

  RecordWriter getRecordWriter(BlockStoreLevel storeLevel);

  void stop();
}
