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

import java.util.concurrent.Callable;

import tsinghua.stargate.Log;
import tsinghua.stargate.exception.StarGateException;

public class BlockFetcher extends Log implements Callable<Block> {

  private BlockManager blockManager;
  private BlockInfo blockInfo;

  BlockFetcher(BlockManager blockManager, BlockInfo blockInfo) {
    this.blockManager = blockManager;
    this.blockInfo = blockInfo;
  }

  @Override
  public Block call() throws Exception {
    BlockId blockId = this.blockInfo.getBlockId();
    BlockStoreLevel level = this.blockInfo.getStoreLevel();

    Block currentBlock;
    try {
      currentBlock = blockManager.getBlock(blockId, level);
    } catch (Exception e) {
      error("Failed to fetch {}", blockInfo, e);
      throw new StarGateException(e);
    }

    return currentBlock;
  }
}
