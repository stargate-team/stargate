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
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.lang3.tuple.ImmutablePair;

import tsinghua.stargate.Log;
import tsinghua.stargate.api.RecordReader;
import tsinghua.stargate.api.RecordWriter;
import tsinghua.stargate.conf.Configuration;
import tsinghua.stargate.exception.StarGateException;
import tsinghua.stargate.storage.factory.provider.BlockStoreFactoryProvider;
import tsinghua.stargate.storage.impl.BlockStoreAlluxioImpl;
import tsinghua.stargate.storage.impl.BlockStoreDiskImpl;
import tsinghua.stargate.storage.impl.BlockStoreMemoryImpl;

public class BlockManagerImpl extends Log implements BlockManager {

  private final Map<BlockId, BlockStoreLevel> blockIds = new HashMap<>();
  private final BlockStoreMemoryImpl memoryStore;
  private final BlockStoreDiskImpl diskStore;
  private final BlockStoreAlluxioImpl alluxioStore;
  private final Object lock = new Object();

  public BlockManagerImpl(Configuration conf) {
    memoryStore = (BlockStoreMemoryImpl) BlockStoreFactoryProvider
        .getMemoryFactory(conf).getMemory(BlockStore.class, conf);
    diskStore = (BlockStoreDiskImpl) BlockStoreFactoryProvider
        .getDiskFactory(conf).getDisk(BlockStore.class, conf);
    alluxioStore = (BlockStoreAlluxioImpl) BlockStoreFactoryProvider
        .getAlluxioFactory(conf).getAlluxio(BlockStore.class, conf);
  }

  // -- BlockManager interface --

  @Override
  public BlockStoreMemoryImpl getMemoryStore() {
    return memoryStore;
  }

  @Override
  public BlockStoreDiskImpl getDiskStore() {
    return diskStore;
  }

  @Override
  public BlockStoreAlluxioImpl getAlluxioStore() {
    return alluxioStore;
  }

  @Override
  public boolean putBlock(Block block) throws StarGateException {
    BlockId blockId = block.getBlockId();

    synchronized (lock) {
      BlockStoreLevel storeLevel = block.getStoreLevel();
      // TODO: must check assignment
      Iterator<Object> blockIter =
          (Iterator) block.getBlocks().values().iterator();
      boolean putResult = false;
      switch (storeLevel) {
      case IN_HEAP:
        putResult = memoryStore.putValues(blockId, blockIter, storeLevel);
        break;

      case DISK:
        putResult = diskStore.putValues(blockId, blockIter, storeLevel);
        break;

      case ALLUXIO:
        putResult = alluxioStore.putValues(blockId, blockIter, storeLevel);
        break;

      default:
        error("Unsupported block store level");
        break;
      }

      return putResult;
    }
  }

  @Override
  public Block getBlock(BlockId blockId, BlockStoreLevel storeLevel)
      throws StarGateException {
    Iterator<Object> blockDataIter = null;

    synchronized (lock) {
      switch (storeLevel) {
      case IN_HEAP:
        blockDataIter = memoryStore.getValues(blockId);
        break;

      case DISK:
        blockDataIter = diskStore.getValues(blockId);
        break;

      case ALLUXIO:
        blockDataIter = alluxioStore.getValues(blockId);
        break;

      default:
        error("Unsupported block store level");
        break;
      }

      if (blockDataIter == null) {
        return null;
      }

      Map<String, ByteBuffer> blocks = new HashMap<>();
      while (blockDataIter.hasNext()) {
        // TODO: must check assignment
        ImmutablePair<String, Object> blockData =
            (ImmutablePair<String, Object>) blockDataIter.next();
        blocks.put(blockData.getLeft(), (ByteBuffer) blockData.getRight());
      }
      blockIds.put(blockId, storeLevel);

      return new Block(blockId, storeLevel, blocks);
    }
  }

  @Override
  public boolean contain(BlockId blockId) throws IOException {
    return blockIds.containsKey(blockId);
  }

  @Override
  public boolean remove(BlockId blockId) throws IOException {
    boolean putResult = false;
    synchronized (lock) {
      BlockStoreLevel storeLevel = blockIds.get(blockId);
      switch (storeLevel) {
      case IN_HEAP:
        putResult = memoryStore.remove(blockId);
        break;

      case DISK:
        putResult = diskStore.remove(blockId);
        break;

      case ALLUXIO:
        putResult = alluxioStore.remove(blockId);
        break;

      default:
        error("Unsupported block store level");
        break;
      }
      blockIds.remove(blockId);
    }
    return putResult;
  }

  @Override
  public void setRecordReader(RecordReader reader, BlockStoreLevel storeLevel) {
    switch (storeLevel) {
    case DISK:
      getDiskStore().setRecordReader(reader);
      break;

    case ALLUXIO:
      getAlluxioStore().setRecordReader(reader);

    case IN_HEAP:
      getMemoryStore().setRecordReader(reader);
    }
  }

  @Override
  public RecordReader getRecordReader(BlockStoreLevel storeLevel) {
    RecordReader reader = null;
    switch (storeLevel) {
    case DISK:
      reader = getDiskStore().getRecordReader();
      break;

    case ALLUXIO:
      reader = getAlluxioStore().getRecordReader();

    case IN_HEAP:
      reader = getMemoryStore().getRecordReader();
    }
    return reader;
  }

  @Override
  public void setRecordWriter(RecordWriter writer, BlockStoreLevel storeLevel) {
    switch (storeLevel) {
    case DISK:
      getDiskStore().setRecordWriter(writer);
      break;

    case ALLUXIO:
      getAlluxioStore().setRecordWriter(writer);

    case IN_HEAP:
      getMemoryStore().setRecordWriter(writer);
    }
  }

  @Override
  public RecordWriter getRecordWriter(BlockStoreLevel storeLevel) {
    RecordWriter writer = null;
    switch (storeLevel) {
    case DISK:
      writer = getDiskStore().getRecordWriter();
      break;

    case ALLUXIO:
      writer = getAlluxioStore().getRecordWriter();

    case IN_HEAP:
      writer = getMemoryStore().getRecordWriter();
    }
    return writer;
  }

  @Override
  public void stop() {
    getDiskStore().stop();
  }
}
