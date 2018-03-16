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

package tsinghua.stargate.rpc.message;

import tsinghua.stargate.rpc.ApplicationStarGateProtocol;
import tsinghua.stargate.rpc.message.entity.ApplicationId;
import tsinghua.stargate.rpc.message.entity.ApplicationState;
import tsinghua.stargate.util.ReflectionUtils;

/**
 * The request sent by clients to get a new {@link ApplicationId} for submitting
 * an application. Currently, this is empty.
 *
 * @see ApplicationStarGateProtocol#getNewApplication(GetNewApplicationRequest)
 */
public abstract class GetNewApplicationRequest {

  public static GetNewApplicationRequest newInstance() {
    GetNewApplicationRequest request =
        ReflectionUtils.get().getMsg(GetNewApplicationRequest.class);
    request.setAppState(ApplicationState.NEW);
    return request;
  }

  public abstract ApplicationState getAppState();

  public abstract void setAppState(ApplicationState type);
}
