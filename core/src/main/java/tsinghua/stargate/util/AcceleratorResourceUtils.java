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

import tsinghua.stargate.rpc.message.entity.AcceleratorResource;

public class AcceleratorResourceUtils {

  public static AcceleratorResource newInstance(String workLoad, int memory,
      int coreId, int frequency) {
    return AcceleratorResource.newInstance(workLoad, memory, coreId, frequency);
  }

  public static AcceleratorResource newInstance(String workLoad, int memory) {
    return newInstance(workLoad, memory, -1, -1);
  }

  public static AcceleratorResource newInstance(String workLoad) {
    return newInstance(workLoad, -1, -1, -1);
  }

  private static final AcceleratorResource NONE = new AcceleratorResource() {
    @Override
    public int compareTo(AcceleratorResource o) {
      int diff = 0 - o.getAcceleratorCoreMemory();
      if (diff == 0) {
        diff = 0 - o.getAcceleratorCoreMemory();
      }
      return diff;
    }

    @Override
    public String getAcceleratorWorkload() {
      return null;
    }

    @Override
    public void setAcceleratorWorkload(String workload) {
      throw new RuntimeException("NONE cannot be modified!");
    }

    @Override
    public int getAcceleratorCoreMemory() {
      return 0;
    }

    @Override
    public void setAcceleratorCoreMemory(Integer memory) {
      throw new RuntimeException("NONE cannot be modified!");
    }

    @Override
    public int getAcceleratorCoreId() {
      return 0;
    }

    @Override
    public void setAcceleratorCoreId(Integer id) {
      throw new RuntimeException("NONE cannot be modified!");
    }

    @Override
    public Integer getAcceleratorCoreFrequency() {
      return 0;
    }

    @Override
    public void setAcceleratorCoreFrequency(Integer coreFrequency) {
      throw new RuntimeException("NONE cannot be modified!");
    }
  };

  public static AcceleratorResource none() {
    return NONE;
  }

  public static AcceleratorResource clone(AcceleratorResource res) {
    return newInstance(res.getAcceleratorWorkload(),
        res.getAcceleratorCoreMemory(), res.getAcceleratorCoreId(),
        res.getAcceleratorCoreFrequency());
  }

  public static boolean lessThan(AcceleratorResource left,
      AcceleratorResource right) {
    if (!left.getAcceleratorWorkload().equals(right.getAcceleratorWorkload())) {
      return false;
    } else if (right.getAcceleratorCoreMemory() < left
        .getAcceleratorCoreMemory()) {
      return false;
    }
    return true;
  }
}
