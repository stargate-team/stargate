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

import java.text.NumberFormat;

import tsinghua.stargate.util.ReflectionUtils;

/**
 * This class represents the <em>globally unique</em> identifier for an
 * application in a node.
 *
 * <p>
 * The globally unique nature of the identifier is achieved by using the
 * <em>node timestamp</em> i.e. start-time of the {@code StarGateDaemon} along
 * with a monotonically increasing counter for the application.
 */
public abstract class ApplicationId implements Comparable<ApplicationId> {

  private static final String appIdStrPrefix = "sga_";

  public static ApplicationId newInstance(long nodeTimestamp, int id) {
    ApplicationId accId = ReflectionUtils.get().getMsg(ApplicationId.class);
    accId.setTimestamp(nodeTimestamp);
    accId.setId(id);
    return accId;
  }

  static final ThreadLocal<NumberFormat> appIdFormat =
      new ThreadLocal<NumberFormat>() {
        @Override
        public NumberFormat initialValue() {
          NumberFormat fmt = NumberFormat.getInstance();
          fmt.setGroupingUsed(false);
          fmt.setMinimumIntegerDigits(4);
          return fmt;
        }
      };

  /**
   * Get the short integer identifier of the {@code ApplicationId} which is
   * unique for all applications started by a particular instance of the
   * {@code StarGateDaemon}.
   *
   * @return short integer identifier of the {@code ApplicationId}
   */
  public abstract int getId();

  protected abstract void setId(int id);

  /**
   * Get the <em>start time</em> of the {@code StarGateDaemon} which is used to
   * generate globally unique {@code ApplicationId}.
   *
   * @return <em>start time</em> of the {@code StarGateDaemon}
   */
  public abstract long getTimestamp();

  protected abstract void setTimestamp(long timestamp);

  @Override
  public int compareTo(ApplicationId other) {
    if (this.getTimestamp() - other.getTimestamp() == 0) {
      return this.getId() - other.getId();
    } else {
      return this.getTimestamp() > other.getTimestamp() ? 1
          : this.getTimestamp() < other.getTimestamp() ? -1 : 0;
    }
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    ApplicationId other = (ApplicationId) obj;
    if (this.getTimestamp() != other.getTimestamp())
      return false;
    if (this.getId() != other.getId())
      return false;
    return true;
  }

  @Override
  public int hashCode() {
    final int prime = 371237;
    int result = 6521;
    long time = getTimestamp();
    result = prime * result + (int) (time ^ (time >>> 32));
    result = prime * result + getId();
    return result;
  }

  @Override
  public String toString() {
    return appIdStrPrefix + this.getTimestamp() + "_"
        + appIdFormat.get().format(getId());
  }
}
