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

import java.nio.ByteBuffer;
import java.util.Map;

/** A data structure for storing localized data block. */
public class Block {

  private BlockId blockId;

  private BlockStoreLevel storeLevel;

  /**
   * Service data is fetched into buffer using the store path specified by user.
   */
  private Map<String, ByteBuffer> blocks;

  public Block(BlockId blockId, BlockStoreLevel storeLevel,
      Map<String, ByteBuffer> blocks) {
    this.blockId = blockId;
    this.storeLevel = storeLevel;
    this.blocks = blocks;
  }

  public BlockId getBlockId() {
    return blockId;
  }

  public BlockStoreLevel getStoreLevel() {
    return storeLevel;
  }

  public Map<String, ByteBuffer> getBlocks() {
    return blocks;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    Block result = (Block) o;

    if (getBlocks() != null ? !getBlocks().equals(result.getBlocks())
        : result.getBlocks() != null) {
      return false;
    }
    if (getBlockId() != null ? !getBlockId().equals(result.getBlockId())
        : result.getBlockId() != null) {
      return false;
    }
    return getStoreLevel() == result.getStoreLevel();

  }

  @Override
  public int hashCode() {
    int result = getBlocks() != null ? getBlocks().hashCode() : 0;
    result = 31 * result + (getBlockId() != null ? getBlockId().hashCode() : 0);
    result = 31 * result
        + (getStoreLevel() != null ? getStoreLevel().hashCode() : 0);
    return result;
  }

  @Override
  public String toString() {
    return "Block{id:" + blockId + ", storeLevel:" + storeLevel + '}';
  }
}
