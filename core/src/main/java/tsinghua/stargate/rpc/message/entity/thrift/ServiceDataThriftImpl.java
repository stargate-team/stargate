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

package tsinghua.stargate.rpc.message.entity.thrift;

import tsinghua.stargate.rpc.message.entity.BlockStoreType;
import tsinghua.stargate.rpc.message.entity.ServiceData;
import tsinghua.stargate.rpc.thrift.message.entity.ServiceDataThrift;
import tsinghua.stargate.rpc.thrift.message.entity.StoreTypeThrift;

import com.google.common.base.Preconditions;

public class ServiceDataThriftImpl extends ServiceData {

  private static final long serialVersionUID = -6673768564377155115L;

  private ServiceDataThrift thrift;
  private boolean reset = false;

  public ServiceDataThriftImpl() {
    thrift = new ServiceDataThrift();
  }

  public ServiceDataThriftImpl(ServiceDataThrift thrift) {
    this.thrift = thrift;
  }

  @Override
  public BlockStoreType getStoreType() {
    Preconditions.checkNotNull(thrift);
    if (thrift.getStoreType() == null) {
      return null;
    }
    return convertFromThriftFormat(thrift.getStoreType());
  }

  @Override
  public ServiceData setStoreType(BlockStoreType type) {
    Preconditions.checkNotNull(thrift);
    thrift.setStoreType(convertToThriftFormat(type));
    return this;
  }

  @Override
  public String getStorePath() {
    Preconditions.checkNotNull(thrift);
    return thrift.getPath();
  }

  @Override
  public ServiceData setStorePath(String path) {
    Preconditions.checkNotNull(thrift);
    thrift.setPath(path);
    return this;
  }

  @Override
  public long getCapacity() {
    Preconditions.checkNotNull(thrift);
    return thrift.getCapacity();
  }

  @Override
  public ServiceData setCapacity(long capacity) {
    Preconditions.checkNotNull(thrift);
    thrift.setCapacity(capacity);
    return this;
  }

  @Override
  public boolean isCached() {
    Preconditions.checkNotNull(thrift);
    return thrift.isCached();
  }

  @Override
  public ServiceData setCached(boolean cached) {
    Preconditions.checkNotNull(thrift);
    thrift.setCached(cached);
    return this;
  }

  private void maybeInitThrift() {
    if (thrift == null) {
      thrift = new ServiceDataThrift();
    }
  }

  private void mergeLocalToThrift() {
    maybeInitThrift();
    reset = false;
  }

  private BlockStoreType convertFromThriftFormat(StoreTypeThrift thrift) {
    return BlockStoreType.valueOf(thrift.getValue());
  }

  private StoreTypeThrift convertToThriftFormat(
      BlockStoreType ApplicationState) {
    return StoreTypeThrift.findByValue(ApplicationState.value());
  }

  public ServiceDataThrift getThrift() {
    if (!reset) {
      return thrift;
    } else {
      mergeLocalToThrift();
    }
    return thrift;
  }

  @Override
  public int hashCode() {
    return getThrift().hashCode();
  }

  @Override
  public boolean equals(Object other) {
    if (other == null) {
      return false;
    }
    if (other.getClass().isAssignableFrom(this.getClass())) {
      return this.getThrift().equals(this.getClass().cast(other).getThrift());
    }
    return false;
  }

  @Override
  public String toString() {
    return getThrift().toString();
  }
}
