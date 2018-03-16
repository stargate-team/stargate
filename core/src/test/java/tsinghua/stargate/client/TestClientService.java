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

package tsinghua.stargate.client;

import org.junit.Before;
import org.junit.Test;

import tsinghua.stargate.DaemonContextImpl;
import tsinghua.stargate.app.AppManager;
import tsinghua.stargate.conf.Configuration;
import tsinghua.stargate.conf.StarGateConf;

public class TestClientService {

  private ClientService clientService;

  @Before
  public void setUp() throws Exception {
    DaemonContextImpl context = new DaemonContextImpl();
    AppManager appManager = new AppManager(context, null);
    context.setAppManager(appManager);
    clientService = new ClientService(context, appManager);
  }

  @Test
  public void testClient() {
    Configuration conf = new StarGateConf();
    clientService.init(conf);
    clientService.start();
  }
}
