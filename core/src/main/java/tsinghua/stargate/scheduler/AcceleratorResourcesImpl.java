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

import java.util.*;

import tsinghua.stargate.rpc.message.entity.AcceleratorResource;

public class AcceleratorResourcesImpl implements AcceleratorResources {

  private List<AcceleratorResource> resources;
  private Set<String> workloads;

  public AcceleratorResourcesImpl() {
    resources = new ArrayList<>();
    workloads = new HashSet<>();
  }

  @Override
  public int getNum() {
    return resources.size();
  }

  @Override
  public List<AcceleratorResource> getAllResources() {
    return resources;
  }

  @Override
  public Set<String> getWorkloads() {
    for (AcceleratorResource resource : resources) {
      workloads.add(resource.getAcceleratorWorkload());
    }
    return workloads;
  }

  @Override
  public boolean add(AcceleratorResource... resources) {
    for (AcceleratorResource resource : resources) {
      add(resource);
    }
    return resources.length != 0;
  }

  @Override
  public boolean addAll(Collection<AcceleratorResource> resourceCollection) {
    return resources.addAll(resourceCollection);
  }

  public boolean add(AcceleratorResource resource) {
    return resources.add(resource);
  }

  @Override
  public boolean contain(AcceleratorResources resources) {
    if (resources.getNum() == 0)
      return false;
    else {
      Set<String> result = new HashSet<>();
      result.addAll(workloads);
      result.retainAll(resources.getWorkloads());
      return result.size() != 0;
    }
  }
}
