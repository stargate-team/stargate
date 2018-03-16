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

import tsinghua.stargate.rpc.ApplicationStarGateProtocol;
import tsinghua.stargate.rpc.message.SubmitApplicationRequest;
import tsinghua.stargate.util.ReflectionUtils;

/**
 * <p>
 * <code>ApplicationSubmissionContext</code> represents all of the information
 * needed by the <code>StarGateDaemon</code> to launch the application.
 * </p>
 *
 * <p>
 * It includes details such as:
 * <li>message type to communication</li>
 * <li>{@link ApplicationId} assigned by stargate</li>
 * <li>{@link ApplicationLaunchContext} of the application is executed.</li>
 * </p>
 *
 * @see ApplicationStarGateProtocol#submitApplication(SubmitApplicationRequest)
 */
public abstract class ApplicationSubmissionContext {

  public static ApplicationSubmissionContext newInstance(
      ApplicationId applicationId,
      ApplicationLaunchContext applicationLaunchContext) {
    ApplicationSubmissionContext applicationSubmissionContext =
        ReflectionUtils.get().getInstance(ApplicationSubmissionContext.class);
    applicationSubmissionContext.setApplicationState(ApplicationState.SUBMIT);
    applicationSubmissionContext.setApplicationId(applicationId);
    applicationSubmissionContext
        .setApplicationLaunchContext(applicationLaunchContext);
    return applicationSubmissionContext;
  }

  /**
   * Get <code>ApplicationState<code> to communication
   *
   * @return <code>ApplicationState<code> to communication
   */
  public abstract ApplicationState getApplicationState();

  /**
   * Set <code>ApplicationState<code> to communication
   *
   * @param type <code>ApplicationState<code> to communication
   */
  public abstract void setApplicationState(ApplicationState type);

  /**
   * Get <code>ApplicationId</code> of the submitted application
   *
   * @return <code>ApplicationId</code> of the submitted application
   */
  public abstract ApplicationId getApplicationId();

  /**
   * Set <code>ApplicationId</code> of the submitted application
   *
   * @param id <code>ApplicationId</code> of the submitted application
   */
  public abstract void setApplicationId(ApplicationId id);

  /**
   * Get the <code>ApplicationLaunchContext</code> to describe application
   * launched
   *
   * @return the <code>ApplicationLaunchContext</code> to describe application
   *         launched
   */
  public abstract ApplicationLaunchContext getApplicationLaunchContext();

  /**
   * Set the <code>ApplicationLaunchContext</code> to to describe application
   * launched
   *
   * @param applicationLaunchContext the <code>ApplicationLaunchContext</code>
   *          to to describe application launched
   */
  public abstract void setApplicationLaunchContext(
      ApplicationLaunchContext applicationLaunchContext);

  /**
   * Get the <code>AcceleratorResource</code> of the submitted application
   *
   * @return the <code>AcceleratorResource</code> of the submitted application
   */
  public abstract AcceleratorResource getAcceleratorResource();

  /**
   * Set the <code>AcceleratorResource</code> of the submitted application
   *
   * @param resource the <code>AcceleratorResource</code> of the submitted
   *          application
   */
  public abstract void setAcceleratorResource(AcceleratorResource resource);
}
