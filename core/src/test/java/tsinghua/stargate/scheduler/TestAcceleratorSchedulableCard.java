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

import org.junit.Before;
import org.junit.Test;

import tsinghua.stargate.conf.Configuration;
import tsinghua.stargate.conf.StarGateConf;

public class TestAcceleratorSchedulableCard {

  private Configuration conf;

  @Before
  public void setUp() {
    conf = new StarGateConf();
  }

  @Test
  public void testAcceleratorSchedulerCard() {
    Accelerator acceleratorCard1 =
        new Accelerator(conf, "fpga-210203A03486A", 0);

    Accelerator acceleratorCard2 =
        new Accelerator(conf, "fpga-210203A037A4A", 1);

    System.out.println(acceleratorCard1.getResourceCapability());
    System.out.println(acceleratorCard2.getResourceCapability());
  }
}
