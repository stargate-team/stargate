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

package tsinghua.stargate;

import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import tsinghua.stargate.scheduler.SchedulableAccelerator;
import tsinghua.stargate.conf.Configuration;
import tsinghua.stargate.conf.StarGateConf;
import tsinghua.stargate.exception.StarGateException;

public class TestStarGateDaemon {

  private StarGateDaemon sgd = null;

  @Before
  public void setup() throws Exception {
    Configuration conf = new StarGateConf();
    sgd = new StarGateDaemon();
    sgd.init(conf);
    sgd.start();
  }

  @Test
  public void testAcceleratorNodeManager() throws StarGateException {
    DaemonContext context = sgd.getContext();
    Map<String, SchedulableAccelerator> cards =
        context.getScheduler().getNodeAccelerators();

    for (String card : cards.keySet()) {
      System.out.println(context.getClientService().getCardReport(card));
    }
  }

  @After
  public void complete() throws Exception {
    sgd.stop();
  }
}
