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

package tsinghua.stargate.conf;

import java.net.InetSocketAddress;
import java.util.concurrent.TimeUnit;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import tsinghua.stargate.app.DaemonAppEventType;

public class TestConf {

  private Configuration conf;

  @Before
  public void setUp() throws Exception {
    conf = new Configuration();
  }

  @Test
  public void testSet() throws Exception {
    conf.set("stargate version", "v1.0");
    Assert.assertEquals(conf.size(), 1);
    Assert.assertEquals(conf.get("stargate version"), "v1.0");

    conf.setInt("i", 12);
    Assert.assertEquals(conf.getInt("i", 123), 12);

    conf.setDouble("d", 112d);
    Assert.assertTrue(conf.getDouble("d", 123d) == 112d);

    conf.setFloat("f", 112f);
    Assert.assertTrue(conf.getFloat("f", 123) == 112f);

    conf.setEnum("enum", DaemonAppEventType.START);
    Assert.assertTrue(conf.getEnum("enum",
        DaemonAppEventType.KILLED) == DaemonAppEventType.START);

    conf.setBoolean("boolean", true);
    Assert.assertTrue(conf.getBoolean("boolean", false) == true);

    conf.setTimeDuration("time", 100, TimeUnit.MILLISECONDS);
    Assert.assertTrue(
        conf.getTimeDuration("time", 111, TimeUnit.MILLISECONDS) == 100);

    InetSocketAddress address = conf.getSocketAddr(NameSpace.RPC_SERVER_ADDRESS,
        NameSpace.DEFAULT_RPC_SERVER_ADDRESS,
        NameSpace.DEFAULT_RPC_SERVER_PORT);

    Assert.assertEquals(address.getAddress().getHostAddress(), "0.0.0.0");
  }

  @Test
  public void testAddDefaultResource() throws Exception {

    String file1 = "org/apache/stargate/stargate-accelerator.xml";
    conf.addDefaultResource(file1);
    Assert.assertEquals(conf.get(NameSpace.ACCELERATOR_IDS),
        "fpga-210203A03486A,fpga-210203A037A4A");

    Assert.assertTrue(conf.getInt(
        NameSpace.ACCELERATOR_CORE_FREQUENCY_PREFIX + "fpga-210203A037A4A",
        11) == 100);

  }

  @Test
  public void testAddResource() throws Exception {
    String file1 = "stargate-core.xml";
    conf.addResource(file1);
    Assert.assertEquals(conf.get("stargate.anm.address"), "localhost:8888");
  }

}
