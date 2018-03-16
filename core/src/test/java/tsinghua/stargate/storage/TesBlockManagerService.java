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

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import tsinghua.stargate.DaemonContextImpl;
import tsinghua.stargate.conf.Configuration;
import tsinghua.stargate.conf.StarGateConf;

public class TesBlockManagerService {

  private BlockManagerService blockManagerService;
  private DaemonContextImpl context;

  @Before
  public void setUp() throws Exception {
    context = mock(DaemonContextImpl.class);
    blockManagerService = new BlockManagerService(context);
    Configuration conf = new StarGateConf();
    blockManagerService.init(conf);
    blockManagerService.start();
  }

  @Test
  public void testPullBlock() throws IOException, InterruptedException {
    when(context.getUserApps()).thenReturn(null);
    BlockId blockId1 = new BlockId(null, "default", "accelerator_loopback_0",
        "D:/tmp/tmp.txt");
    BlockInfo blockInfo1 =
        new BlockInfo(blockId1, false, BlockStoreLevel.DISK, -1);

    BlockId blockId2 = new BlockId(null, "default", "accelerator_loopback_1",
        "/stargate/test2.txt");
    BlockInfo blockInfo2 =
        new BlockInfo(blockId2, false, BlockStoreLevel.ALLUXIO, -1);
    blockManagerService.fetchBlock(blockInfo1);
    blockManagerService.fetchBlock(blockInfo2);

    // wait pull block finish
    Thread.sleep(10000);
    assertTrue(blockManagerService.getBlockManager().contain(blockId1));
    assertTrue(blockManagerService.getBlockManager().contain(blockId2));
  }

  @After
  public void tearDown() throws Exception {
    Thread.sleep(5000);
    blockManagerService.stop();
  }
}
