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
import tsinghua.stargate.rpc.message.entity.ApplicationId;
import tsinghua.stargate.rpc.message.entity.ApplicationState;
import tsinghua.stargate.rpc.message.entity.Worker;
import tsinghua.stargate.rpc.message.entity.thrift.AcceleratorResourceThriftImpl;
import tsinghua.stargate.rpc.message.entity.thrift.ApplicationIdThriftImpl;
import tsinghua.stargate.rpc.thrift.message.entity.AcceleratorResourceThrift;
import tsinghua.stargate.rpc.thrift.message.entity.AcceleratorWorkerTypeThrift;
import tsinghua.stargate.rpc.thrift.message.entity.ApplicationIdThrift;
import tsinghua.stargate.rpc.thrift.message.entity.ApplicationStateThrift;

/**
 * Thrift-related utility methods used by StarGate. `ThriftUtils` class is
 * implemented by employing `The Singleton Pattern`.
 */
public class ThriftUtils {

  public static Worker convertFromThriftFormat(
      AcceleratorWorkerTypeThrift thrift) {
    return Worker.valueOf(thrift.getValue());
  }

  public static AcceleratorWorkerTypeThrift convertToThriftFormat(
      Worker worker) {
    return AcceleratorWorkerTypeThrift.findByValue(worker.getValue());
  }

  public static ApplicationId convertFromThriftFormat(
      ApplicationIdThrift thrift) {
    return new ApplicationIdThriftImpl(thrift);
  }

  public static ApplicationIdThrift convertToThriftFormat(ApplicationId id) {
    return ((ApplicationIdThriftImpl) id).getThrift();
  }

  public static ApplicationState convertFromThriftFormat(
      ApplicationStateThrift thrift) {
    return ApplicationState.valueOf(thrift.getValue());
  }

  public static ApplicationStateThrift convertToThriftFormat(
      ApplicationState ApplicationState) {
    return ApplicationStateThrift.findByValue(ApplicationState.getValue());
  }

  public static AcceleratorResourceThrift convertToThriftFormat(
      AcceleratorResource acceleratorResource) {
    return ((AcceleratorResourceThriftImpl) acceleratorResource).getThrift();
  }

  public static AcceleratorResource convertFromThriftFormat(
      AcceleratorResourceThrift thrift) {
    return new AcceleratorResourceThriftImpl(thrift);
  }
}