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

import tsinghua.stargate.scheduler.AcceleratorResources;
import tsinghua.stargate.scheduler.AcceleratorResourcesImpl;
import tsinghua.stargate.scheduler.SchedulableAccelerator;

public class AcceleratorReport {

  private final AcceleratorResources used = new AcceleratorResourcesImpl();
  private final AcceleratorResources total = new AcceleratorResourcesImpl();
  private int num = -1;

  public AcceleratorReport(SchedulableAccelerator accelerator) {
    this.used.addAll(accelerator.getUsedResources());
    this.total.addAll(accelerator.getTotalResources());
    this.num = accelerator.getNumTasks();
  }

  public AcceleratorResources getUsed() {
    return used;
  }

  public AcceleratorResources getTotal() {
    return total;
  }

  public int getNum() {
    return num;
  }
}
