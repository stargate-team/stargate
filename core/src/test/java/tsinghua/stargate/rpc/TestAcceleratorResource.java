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

package tsinghua.stargate.rpc;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import tsinghua.stargate.rpc.message.entity.AcceleratorResource;
import tsinghua.stargate.util.AcceleratorResourceUtils;

public class TestAcceleratorResource {

  @Test(timeout = 3000)
  public void testResource() {
    AcceleratorResource resource1 =
        AcceleratorResourceUtils.newInstance("logistic", -1);
    AcceleratorResource resource2 =
        AcceleratorResourceUtils.newInstance("logistic", 65536);
    AcceleratorResource resource3 =
        AcceleratorResourceUtils.newInstance("logistic", 65536);
    AcceleratorResource resource4 =
        AcceleratorResourceUtils.newInstance("kmeans", 65536);
    AcceleratorResource resource5 =
        AcceleratorResourceUtils.newInstance("kmeans", 1024);

    assertFalse(resource1.equals(resource2));
    assertFalse(resource1.equals(resource3));
    assertFalse(resource2.equals(resource3));
    assertFalse(resource3.equals(resource4));
    assertFalse(resource4.equals(resource5));
    assertTrue(AcceleratorResourceUtils.lessThan(resource5, resource4));
  }
}
