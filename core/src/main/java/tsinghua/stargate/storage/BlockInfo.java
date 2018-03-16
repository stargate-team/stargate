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

public class BlockInfo {

  private BlockId blockId;

  /** Whether data block has been cached. */
  private boolean isCached;

  /** Storage level of data block. */
  private BlockStoreLevel storeLevel;

  /** Size of data block. */
  private int blockSize;

  public BlockInfo(BlockId blockId, boolean isCached,
      BlockStoreLevel storeLevel, int blockSize) {
    this.blockId = blockId;
    this.isCached = isCached;
    this.storeLevel = storeLevel;
    this.blockSize = blockSize;
  }

  public BlockId getBlockId() {
    return blockId;
  }

  public boolean isCached() {
    return isCached;
  }

  public void setCached(boolean cached) {
    this.isCached = cached;
  }

  public BlockStoreLevel getStoreLevel() {
    return storeLevel;
  }

  public int getBlockSize() {
    return blockSize;
  }

  public void setBlockSize(int blockSize) {
    this.blockSize = blockSize;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }

    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    BlockInfo that = (BlockInfo) o;

    if (getBlockSize() != that.getBlockSize()) {
      return false;
    }

    if (isCached() != that.isCached()) {
      return false;
    }

    if (getBlockId() != null ? !getBlockId().equals(that.getBlockId())
        : that.getBlockId() != null) {
      return false;
    }

    return getStoreLevel() == that.getStoreLevel();
  }

  @Override
  public int hashCode() {
    int result = getBlockId() != null ? getBlockId().hashCode() : 0;
    result = 31 * result + getBlockSize();
    result = 31 * result
        + (getStoreLevel() != null ? getStoreLevel().hashCode() : 0);
    result = 31 * result + (isCached() ? 1 : 0);
    return result;
  }

  @Override
  public String toString() {
    return "BlockInfo{" + "blockId=" + blockId + ", storeLevel=" + storeLevel
        + ", blockSize=" + blockSize + '}';
  }
}
