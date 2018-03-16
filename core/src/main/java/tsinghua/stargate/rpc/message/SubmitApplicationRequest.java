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
import tsinghua.stargate.rpc.message.entity.ApplicationLaunchContext;
import tsinghua.stargate.rpc.message.entity.ApplicationSubmissionContext;
import tsinghua.stargate.util.ReflectionUtils;

/**
 * The request sent by a client to submit an application to the
 * {@code StarGateDaemon ANM}.
 *
 * <p>
 * The request, via {@link ApplicationSubmissionContext
 * ApplicationSubmissionContext}, contains details such as queue,
 * {@link AcceleratorResource AcceleratorResource} required to run the
 * application, the equivalent of {@link ApplicationLaunchContext
 * ApplicationLaunchContext} for launching the submitted application etc.
 *
 * @see ApplicationStarGateProtocol#submitApplication(SubmitApplicationRequest)
 */
public abstract class SubmitApplicationRequest {

  public static SubmitApplicationRequest newInstance(
      ApplicationSubmissionContext context) {
    SubmitApplicationRequest request =
        ReflectionUtils.get().getMsg(SubmitApplicationRequest.class);
    request.setAppSubmissionContext(context);
    return request;
  }

  /**
   * Get application submission context.
   * 
   * @return application submission context
   */
  public abstract ApplicationSubmissionContext getAppSubmissionContext();

  /**
   * Set application submission context.
   * 
   * @param context application submission context
   */
  public abstract void setAppSubmissionContext(
      ApplicationSubmissionContext context);
}
