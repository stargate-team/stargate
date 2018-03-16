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

package tsinghua.stargate.rpc.message.entity;

import tsinghua.stargate.util.ReflectionUtils;

/**
 * This class models a set of computer accelerator resources in one node.
 *
 * <p>
 * Currently its hardware resource include <em>FPGA</em>, <em>GPU</em> and
 * <em>ASIC</em>. it models <em> accelerator workload</em>, <em>accelerator
 * workload numbers</em> and <em>accelerator hardware id</em>.
 *
 * <p>
 * The unit for accelerator workload is often machine learning algorithm like
 * logistical regression,K-means,Decision tree and so on.if the hardware is FPGA
 * or GPU,means the accelerator workload can be reprogrammable.but we try to
 * avoid frequent reprogramming of the same FPGA device, because it typically
 * takes long time to program an FPGA with a given bitstream.
 *
 * <p>
 * The unit for accelerator core is fpga work frequency,considering the
 * parallelism of hardware, 12 core are divided into fpga. Every core has
 * specify clock frequency range from 5M to 250M ,each core keeps independent
 * can have own memory and computational logic.The unit for accelerator core
 * memory is the space by fpga designer allocated.
 */
public abstract class AcceleratorResource
    implements Comparable<AcceleratorResource> {

  public static AcceleratorResource newInstance(String workload, int memory,
      int coreId, int coreFrequency) {
    AcceleratorResource resource =
        ReflectionUtils.get().getMsg(AcceleratorResource.class);
    resource.setAcceleratorWorkload(workload);
    resource.setAcceleratorCoreMemory(memory);
    resource.setAcceleratorCoreId(coreId);
    resource.setAcceleratorCoreFrequency(coreFrequency);
    return resource;
  }

  /**
   * Get name of the accelerator resource eg.K-means
   *
   * @return <em> name of logistical accelerator </em> of the resource
   */
  public abstract String getAcceleratorWorkload();

  /**
   * Set name of the accelerator resource.
   *
   * @param workload <em>name of logistical accelerator </em> of the resource
   */
  public abstract void setAcceleratorWorkload(String workload);

  /**
   * Get the fpga core memory of the accelerator work as bits
   *
   * @return <em>channel</em>of the accelerator workload
   */
  public abstract int getAcceleratorCoreMemory();

  /**
   * Set the core allocated memory of the accelerator work as bits.
   */
  public abstract void setAcceleratorCoreMemory(Integer memory);

  /**
   * Get the fpga core id
   */
  public abstract int getAcceleratorCoreId();

  /**
   * Set the fpga core id
   */
  public abstract void setAcceleratorCoreId(Integer id);

  /**
   * Get the fpga core work frequency
   */
  public abstract Integer getAcceleratorCoreFrequency();

  /**
   * Set the fpga core work frequency
   */
  public abstract void setAcceleratorCoreFrequency(Integer coreFrequency);

  @Override
  public int hashCode() {
    final int prime = 263167;
    int result = 3571;
    result = 939769357 + getAcceleratorCoreMemory();
    result = prime * result + getAcceleratorWorkload().hashCode();
    result = prime * result + getAcceleratorCoreFrequency().hashCode();
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (!(obj instanceof AcceleratorResource)) {
      return false;
    }
    AcceleratorResource other = (AcceleratorResource) obj;
    if ((getAcceleratorCoreMemory() != other.getAcceleratorCoreMemory())
        || (!(getAcceleratorWorkload().equals(other.getAcceleratorWorkload())))
        || (getAcceleratorCoreId() != other.getAcceleratorCoreId())
        || getAcceleratorCoreFrequency() != other
            .getAcceleratorCoreFrequency()) {
      return false;
    }
    return true;
  }

  @Override
  public String toString() {
    return "<workload:" + getAcceleratorWorkload() + ", core maxMemory:"
        + getAcceleratorCoreMemory() + ", core id:" + getAcceleratorCoreId()
        + ", core frequency:" + getAcceleratorCoreFrequency() + ">";
  }

}
