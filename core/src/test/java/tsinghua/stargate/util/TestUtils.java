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

package tsinghua.stargate.util;

import static org.junit.Assert.assertTrue;

import java.net.InetSocketAddress;

import org.junit.Before;
import org.junit.Test;

import tsinghua.stargate.conf.Configuration;
import tsinghua.stargate.conf.NameSpace;
import tsinghua.stargate.conf.StarGateConf;

public class TestUtils {

  private Configuration conf;

  @Before
  public void setUp() throws Exception {
    conf = new StarGateConf();
  }

  @Test
  public void testIp() throws Exception {
    InetSocketAddress socketAddress = conf.getSocketAddr(
        NameSpace.RPC_SERVER_ADDRESS, NameSpace.DEFAULT_RPC_SERVER_ADDRESS,
        NameSpace.DEFAULT_RPC_SERVER_PORT);

    System.out.println(Utils.getLocalHostLANAddress().getCanonicalHostName());
  }

  @Test
  public void testGetQueueName() {
    assertTrue(Utils.getQueueName("root", "loopback").equals("root.loopback"));
    assertTrue(Utils.getTaskWorkLoad("root_dsf_2").equals("dsf"));
  }
}
