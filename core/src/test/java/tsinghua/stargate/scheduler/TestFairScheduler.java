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

package tsinghua.stargate.scheduler;

import java.io.IOException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import tsinghua.stargate.StarGateDaemon;
import tsinghua.stargate.conf.Configuration;
import tsinghua.stargate.conf.StarGateConf;
import tsinghua.stargate.event.AsyncDispatcher;
import tsinghua.stargate.rpc.message.entity.ApplicationId;
import tsinghua.stargate.rpc.message.entity.Worker;
import tsinghua.stargate.task.TaskManagerImpl;

public class TestFairScheduler {

  private Configuration conf;
  private StarGateDaemon sgd;

  @Before
  public void setUp() throws Exception {
    conf = new StarGateConf();
    sgd = new StarGateDaemon();
    sgd.init(conf);
    ((AsyncDispatcher) sgd.getContext().getDispatcher()).start();
    ((TaskManagerImpl) sgd.getContext().getTaskManager()).start();
    ((FairScheduler) sgd.getContext().getScheduler()).start();
  }

  @Test
  public void testScheduler() throws IOException, InterruptedException {
    testQueueLength();
    testWeight();
    Thread.sleep(2000);
  }

  private void testWeight() throws IOException {
    ApplicationId appId1 =
        ApplicationId.newInstance(System.currentTimeMillis(), 1);
    ApplicationId appId2 =
        ApplicationId.newInstance(System.currentTimeMillis(), 2);
    sgd.getContext().getTaskManager().createTask(appId1, "userApp-1590137",
        Worker.FPGA, "loopback");
    sgd.getContext().getTaskManager().createTask(appId2, "userApp-1590137",
        Worker.FPGA, "imageCaption");
  }

  private void testQueueLength() throws IOException {
    ApplicationId appId1 =
        ApplicationId.newInstance(System.currentTimeMillis(), 1);
    ApplicationId appId2 =
        ApplicationId.newInstance(System.currentTimeMillis(), 2);
    ApplicationId appId3 =
        ApplicationId.newInstance(System.currentTimeMillis(), 3);
    sgd.getContext().getTaskManager().createTask(appId1, "userApp-1590137",
        Worker.FPGA, "loopback");
    sgd.getContext().getTaskManager().createTask(appId2, "userApp-1590137",
        Worker.FPGA, "imageCaption");
    sgd.getContext().getTaskManager().createTask(appId3, "userApp-1590137",
        Worker.FPGA, "imageCaption");
  }

  @After
  public void tearDown() throws Exception {
    if (sgd != null) {
      sgd.stop();
    }
  }
}
