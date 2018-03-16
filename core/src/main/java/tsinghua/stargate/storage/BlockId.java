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

package tsinghua.stargate.storage;

import java.util.Arrays;

import tsinghua.stargate.rpc.message.entity.ApplicationId;

public class BlockId {

  public static final String blockIdStrPrefix = "Block_";

  private ApplicationId anmAppId;

  private String userAppId;

  private String taskId;

  private String[] paths;

  public BlockId(ApplicationId anmAppId, String userAppId, String taskId,
      String... paths) {
    this.anmAppId = anmAppId;
    this.userAppId = userAppId;
    this.taskId = taskId;
    this.paths = paths;
  }

  public String getUserAppId() {
    return userAppId;
  }

  public String getTaskId() {
    return taskId;
  }

  public void setTaskId(String taskId) {
    this.taskId = taskId;
  }

  public String[] getPaths() {
    return paths;
  }

  public void setPaths(String[] paths) {
    this.paths = paths;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    BlockId blockId = (BlockId) o;

    if (anmAppId != null ? !anmAppId.equals(blockId.anmAppId)
        : blockId.anmAppId != null) {
      return false;
    }
    if (getUserAppId() != null ? !getUserAppId().equals(blockId.getUserAppId())
        : blockId.getUserAppId() != null) {
      return false;
    }
    if (getTaskId() != null ? !getTaskId().equals(blockId.getTaskId())
        : blockId.getTaskId() != null) {
      return false;
    }
    // Probably incorrect - comparing Object[] arrays with Arrays.equals
    return Arrays.equals(getPaths(), blockId.getPaths());

  }

  @Override
  public int hashCode() {
    int result = anmAppId != null ? anmAppId.hashCode() : 0;
    result =
        31 * result + (getUserAppId() != null ? getUserAppId().hashCode() : 0);
    result = 31 * result + (getTaskId() != null ? getTaskId().hashCode() : 0);
    result = 31 * result + Arrays.hashCode(getPaths());
    return result;
  }

  @Override
  public String toString() {
    return blockIdStrPrefix + taskId;
  }
}
