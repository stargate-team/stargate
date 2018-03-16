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
import tsinghua.stargate.rpc.message.entity.AcceleratorResource;
import tsinghua.stargate.rpc.message.entity.ApplicationId;
import tsinghua.stargate.rpc.message.entity.ApplicationState;
import tsinghua.stargate.util.ReflectionUtils;

/**
 * The response sent by the {@code StarGateDaemon ANM} to the client for a
 * request to get a new {@link ApplicationId} for submitting an application.
 * 
 * @see ApplicationStarGateProtocol#getNewApplication(GetNewApplicationRequest)
 */
public abstract class GetNewApplicationResponse {

  public static GetNewApplicationResponse newInstance(ApplicationId appId,
      AcceleratorResource maxCapability, ApplicationState applicationState) {
    GetNewApplicationResponse response =
        ReflectionUtils.get().getMsg(GetNewApplicationResponse.class);
    response.setApplicationId(appId);
    response.setMaxResourceCapability(maxCapability);
    response.setApplicationState(applicationState);
    return response;
  }

  /**
   * Get the <em>new</em> {@code ApplicationId} allocated by the
   * {@code StarGateDaemon}.
   * 
   * @return <em>new</em> {@code ApplicationId} allocated by the ANM
   */
  public abstract ApplicationId getApplicationId();

  public abstract void setApplicationId(ApplicationId applicationId);

  /**
   * Get the maximum capability for any {@link AcceleratorResource} allocated by
   * the ANM in this node.
   * 
   * @return maximum capability of allocated resources in this node
   */
  public abstract AcceleratorResource getMaxResourceCapability();

  public abstract void setMaxResourceCapability(AcceleratorResource capability);

  public abstract ApplicationState getApplicationState();

  public abstract void setApplicationState(ApplicationState type);
}
