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

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import tsinghua.stargate.DaemonContextImpl;
import tsinghua.stargate.util.Utils;

public class TestQueueManager {

  private QueueManager queueManager;

  @Before
  public void setUp() throws Exception {
    DaemonContextImpl context = new DaemonContextImpl();
    FairScheduler fairScheduler = new FairScheduler(context);
    queueManager = new QueueManager(fairScheduler);
    queueManager.init();
  }

  @Test
  public void testCreateLeafQueue() {
    SchedulableQueue queue1 = queueManager.getLeafQueue("loopback.app1", true);
    SchedulableQueue queue2 = queueManager.getLeafQueue("loopback.app2", true);

    queueManager.getLeafQueue("k-means.app1", true);
    queueManager.getLeafQueue("k-means.app2", true);

    Assert.assertNotNull(queueManager.getLeafQueues());
    Assert.assertNotNull(queueManager.getLeafQueue("loopback.app2", false));
    Assert.assertNotNull(
        queueManager.getLeafQueue("loopback.app2", false).getParent());

    assertTrue(queueManager.getLeafQueue("loopback.app2", false).getParent()
        .getChildQueues().size() == 2);

    Assert.assertNull(queueManager.getParentQueue("root.queue1", false));
    Assert.assertNull(queueManager.getLeafQueue("decisionTree.queue1", false));

    assertTrue(queueManager.getParentQueue("loopback", false).getChildQueues()
        .size() == 2);
    assertTrue(queueManager.removeQueueIfEmpty(queue1));
    assertTrue(queue2.getParent().getChildQueues().size() == 1);
  }

  @Test
  public void testGetQueueName() {
    String name = Utils.getQueueName("loopback", "app1");
    assertTrue(name.equals("loopback.app1"));
  }

  @Test
  public void testCheckQueueNodeName() {
    assertFalse(queueManager.isQueueNameValid(""));
    assertFalse(queueManager.isQueueNameValid("  "));
    assertFalse(queueManager.isQueueNameValid(" a"));
    assertFalse(queueManager.isQueueNameValid("a "));
    assertFalse(queueManager.isQueueNameValid(" a "));
    assertTrue(queueManager.isQueueNameValid("a b"));
    assertTrue(queueManager.isQueueNameValid("a"));
  }
}
