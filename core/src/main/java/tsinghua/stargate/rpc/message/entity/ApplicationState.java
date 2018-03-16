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

public enum ApplicationState {

  /** Client send new application request. */
  NEW(0),

  /** Server grant new application request. */
  GRANT(1),

  /** Server reject new application request. */
  REJECT(2),

  /** Client start submitting application. */
  SUBMIT(3),

  /** Server accept submitted application. */
  ACCEPT(4),

  /** Server accept repetitive application. */
  REPEAT(5),

  /**
   * Client fail to set accelerator workload or Server fail to assign
   * accelerator resource.
   */
  NONE(6),

  /** Application is running. */
  RUNNING(7),

  /** Application is finished. */
  FINISHED(8),

  /** Application is failed. */
  FAILED(9),

  /** Application is killed. */
  KILLED(10);

  private final int value;

  ApplicationState(int value) {
    this.value = value;
  }

  /**
   * Find the enum type by its integer value.
   *
   * @return {@code null} if the value is not found
   */
  public static ApplicationState valueOf(int value) {
    switch (value) {
    case 0:
      return NEW;
    case 1:
      return GRANT;
    case 2:
      return REJECT;
    case 3:
      return SUBMIT;
    case 4:
      return ACCEPT;
    case 5:
      return REPEAT;
    case 6:
      return NONE;
    case 7:
      return RUNNING;
    case 8:
      return FINISHED;
    case 9:
      return FAILED;
    case 10:
      return KILLED;
    default:
      return null;
    }
  }

  public int getValue() {
    return this.value;
  }
}
