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

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import tsinghua.stargate.rpc.message.entity.AcceleratorResource;
import tsinghua.stargate.task.Task;
import tsinghua.stargate.task.TaskImpl;

public class TestSchedulableTask {

  @Test
  public void testCoreAssignment() {
    Task task = mock(TaskImpl.class);
    when(task.getWorkload()).thenReturn("Kmeans");
    SchedulableTask schedulableTask =
        new SchedulableTask(null, null, null, task);

    AcceleratorResource resource1 =
        AcceleratorResource.newInstance("Kmeans", 128, -1, 100);
    AcceleratorResource resource2 =
        AcceleratorResource.newInstance("Kmeans", 128, -1, 200);
    AcceleratorResource resource3 =
        AcceleratorResource.newInstance("Kmeans", 128, -1, 150);

    List<AcceleratorResource> resourceList = new ArrayList<>();
    resourceList.add(resource1);
    resourceList.add(resource2);
    resourceList.add(resource3);

    AcceleratorResource assignResource =
        schedulableTask.assignCore(resourceList, "Kmeans");
    assertTrue(assignResource.getAcceleratorCoreFrequency() == 200);
    System.out.println(resourceList);
  }
}
