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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;

import tsinghua.stargate.rpc.message.entity.AcceleratorResource;
import tsinghua.stargate.util.AcceleratorResourceUtils;

public class TestAcceleratorResources {

  private AcceleratorResources resources;

  @Before
  public void setUp() {
    resources = new AcceleratorResourcesImpl();
  }

  @Test(timeout = 3000)
  public void testResources() {
    AcceleratorResource resource1 =
        AcceleratorResourceUtils.newInstance("logistic", 65536);
    AcceleratorResource resource2 =
        AcceleratorResourceUtils.newInstance("kmeans", 65536);
    resources.add(resource1, resource2);

    AcceleratorResources tmpResource1 = new AcceleratorResourcesImpl();
    AcceleratorResources tmpResources2 = new AcceleratorResourcesImpl();
    assertFalse(tmpResource1.contain(resources));

    AcceleratorResource[] resourceArray =
        resources.getAllResources().toArray(new AcceleratorResource[0]);
    System.out.println(Arrays.toString(resourceArray));
    System.out.println(resources.getNum());
    System.out.println(resources.getWorkloads());

    tmpResource1.add(AcceleratorResourceUtils.clone(resource1));
    assertTrue(resources.contain(tmpResource1));

    AcceleratorResource resource4 =
        AcceleratorResourceUtils.newInstance("loopback", 65536);
    tmpResources2.add(resource4);
    assertFalse(resources.contain(tmpResources2));
  }
}
