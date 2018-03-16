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

import tsinghua.stargate.rpc.message.entity.ApplicationId;
import tsinghua.stargate.rpc.thrift.message.entity.ApplicationIdThrift;

import com.google.common.base.Preconditions;

public class ApplicationIdThriftImpl extends ApplicationId {

  private ApplicationIdThrift thrift;

  public ApplicationIdThriftImpl() {
    thrift = new ApplicationIdThrift();
  }

  public ApplicationIdThriftImpl(ApplicationIdThrift thrift) {
    this.thrift = thrift;
  }

  @Override
  public int getId() {
    Preconditions.checkNotNull(thrift);
    return thrift.getId();
  }

  @Override
  protected void setId(int id) {
    Preconditions.checkNotNull(thrift);
    thrift.setId(id);
  }

  @Override
  public long getTimestamp() {
    Preconditions.checkNotNull(thrift);
    return thrift.getTimeStamp();
  }

  @Override
  protected void setTimestamp(long clusterTimestamp) {
    Preconditions.checkNotNull(thrift);
    thrift.setTimeStamp(clusterTimestamp);
  }

  public ApplicationIdThrift getThrift() {
    return thrift;
  }
}
