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

import java.util.concurrent.*;

import com.google.common.base.Preconditions;

import tsinghua.stargate.DaemonContext;
import tsinghua.stargate.conf.Configuration;
import tsinghua.stargate.conf.NameSpace;
import tsinghua.stargate.event.EventHandler;
import tsinghua.stargate.service.AbstractService;
import tsinghua.stargate.task.Task;
import tsinghua.stargate.task.TaskLocalizedEvent;
import tsinghua.stargate.util.ThreadUtils;
import tsinghua.stargate.util.Utils;

@SuppressWarnings("unchecked")
public class BlockManagerService extends AbstractService
    implements EventHandler<BlockFetchEvent> {

  private Configuration conf;
  private DaemonContext context;
  private BlockManager blockManager;
  private BlockFetcherThread blockFetcherThread;

  public BlockManagerService(DaemonContext context) {
    super("BlockManagerService");
    this.context = context;
  }

  @Override
  public void serviceInit(Configuration conf) throws Exception {
    info("Init service '{}'", this.getClass().getSimpleName());
    this.conf = conf;
    blockManager = new BlockManagerImpl(conf);
    blockFetcherThread = new BlockFetcherThread(conf);
  }

  @Override
  public void serviceStart() throws Exception {
    startFetchingBlock();
    info("Successfully started service '{}'", this.getClass().getSimpleName());
    super.serviceStart();
  }

  @Override
  protected void serviceStop() throws Exception {
    info("Successfully stopped service '{}'", this.getClass().getSimpleName());
    super.serviceStop();
    blockFetcherThread.interrupt();
    blockManager.stop();
  }

  private synchronized void startFetchingBlock() {
    Preconditions.checkNotNull(blockFetcherThread,
        "Thread for fetching block is null");
    blockFetcherThread.start();
  }

  public BlockManager getBlockManager() {
    return blockManager;
  }

  // -- Event handler machinery --

  @Override
  public void handle(BlockFetchEvent event) {
    switch (event.getType()) {
    case DATA:
      info("Start fetching source block (id: {}, size: {}, storeLevel: {})",
          event.getBlockInfo().getBlockId(),
          Utils.bytes2String(event.getBlockInfo().getBlockSize()),
          event.getBlockInfo().getStoreLevel());
      fetchBlock(event.getBlockInfo());
      break;
    }
  }

  void fetchBlock(BlockInfo blockInfo) {
    blockFetcherThread.fetchBlock(blockInfo);
  }

  /**
   * Thread for fetching blocks from external sources, e.g., disk, Alluxio, and
   * so on.
   */
  public class BlockFetcherThread extends Thread {

    private final ExecutorService threadPool;
    private final CompletionService<Block> fetcherService;
    // private final List<Future<Block>> pendingFetchers;

    BlockFetcherThread(Configuration conf) {
      this.threadPool = createThreadPool(conf);
      this.fetcherService = new ExecutorCompletionService<>(threadPool);
      // this.pendingFetchers = new ArrayList<>();
    }

    private ExecutorService createThreadPool(Configuration conf) {
      int nThreads = conf.getInt(NameSpace.STORAGE_THREAD_COUNT,
          NameSpace.DEFAULT_STORAGE_THREAD_COUNT);
      return ThreadUtils.getExecutor()
          .newDaemonCachedThreadPool("BlockData fetcher", nThreads);
    }

    // TODO: full fill the block size
    private void fetchBlock(BlockInfo blockInfo) {
      if (blockInfo.getBlockId() != null) {
        // synchronized (pendingFetchers) {
        // pendingFetchers.add(
        // fetcherService.submit(new BlockFetcher(blockManager, blockInfo)));
        // }
        // TODO: Test it under concurrent env after removes pendingFetchers
        fetcherService.submit(new BlockFetcher(blockManager, blockInfo));
      }
    }

    @Override
    public void run() {
      try {
        while (!Thread.currentThread().isInterrupted()) {
          Future<Block> completedFetcher = fetcherService.take();
          // pendingFetchers.remove(completedFetcher);
          localizedBlock(completedFetcher.get());
        }
      } catch (InterruptedException e) {
        warn(this.getClass().getName() + " interrupted. Returning.");
        Thread.currentThread().interrupt();
      } catch (ExecutionException e) {
        error("Failed to execute block fetcher, detail: {}", e.getMessage());
      } finally {
        threadPool.shutdownNow();
      }
    }

    private void localizedBlock(Block block) {
      BlockId blockId = block.getBlockId();
      if (blockId != null) {
        String userAppId = blockId.getUserAppId();
        String taskId = blockId.getTaskId();
        if (userAppId != null && taskId != null) {
          if (null == context.getUserApps()) {
            return;
          }
          Task task = context.getUserApps().get(userAppId).getTask(taskId);
          context.getDispatcher().getEventHandler()
              .handle(new TaskLocalizedEvent(task.getTaskId(), block, true));
        } else {
          error("Application-{}-task-{} doesn't exist", userAppId, taskId);
        }
      } else {
        error("Empty block id, please check block info");
      }
    }
  }
}
