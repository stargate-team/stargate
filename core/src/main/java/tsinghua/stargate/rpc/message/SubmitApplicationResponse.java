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
import tsinghua.stargate.rpc.message.entity.ApplicationState;
import tsinghua.stargate.util.ReflectionUtils;

/**
 * The response sent by the {@code StarGateDaemon} to a client on application
 * submission. Currently, this is empty.
 *
 * @see ApplicationStarGateProtocol#submitApplication(SubmitApplicationRequest)
 */
public abstract class SubmitApplicationResponse {

  public static SubmitApplicationResponse newInstance() {
    return ReflectionUtils.get().getMsg(SubmitApplicationResponse.class);
  }

  /**
   * Get the <code>ApplicationState</code> when stargate server receive the
   * submit application
   */
  public abstract ApplicationState getApplicationState();

  /**
   * Set the <code>ApplicationState</code>
   *
   * @param type the <code>ApplicationState</code>
   */
  public abstract void setApplicationState(ApplicationState type);
}
