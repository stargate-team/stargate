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

import java.io.Serializable;

import tsinghua.stargate.util.ReflectionUtils;

/**
 * <em>Service data</em> provided by user.
 *
 * <p>
 * Before sending tasks to accelerator for execution, StarGate Server is
 * responsible for pulling <em>service data</em> from one of supported storage
 * medias, e.g., Disk, Alluxio and so on.
 */
public abstract class ServiceData implements Serializable {

  public static ServiceData newInstance(String path) {
    return ReflectionUtils.get().getMsg(ServiceData.class).setStorePath(path);
  }

  public static ServiceData newInstance(BlockStoreType type, String path,
      long capacity, boolean cached) {
    ServiceData serviceData = ReflectionUtils.get().getMsg(ServiceData.class);
    serviceData.setStoreType(type);
    serviceData.setStorePath(path);
    serviceData.setCapacity(capacity);
    serviceData.setCached(cached);
    return serviceData;
  }

  /**
   * Get the store type of <em>service data</em>, e.g., Disk, Alluxio and so on.
   *
   * @return the store type of <em>service data</em>
   */
  public abstract BlockStoreType getStoreType();

  /**
   * Set the store type of <em>service data</em>.
   *
   * @param type the store type of <em>service data</em>
   */
  public abstract ServiceData setStoreType(BlockStoreType type);

  /**
   * Get the store path of <em>service data</em>.
   *
   * @return the store path of <em>service data</em>
   */
  public abstract String getStorePath();

  /**
   *
   * Set the store path of <em>service data</em>. A store path may be a relative
   * path in StringRep or an Alluxio path.
   *
   * @param path the store path of <em>service data</em>
   */
  public abstract ServiceData setStorePath(String path);

  /**
   * Get the capacity of <em>service data</em>.
   *
   * <p>
   * Due to the max capacity limitation, we have to know the capacity of
   * <em>service data</em>.
   *
   * @return the capacity of <em>service data</em>
   */
  public abstract long getCapacity();

  /**
   * Set the capacity of <em>service data</em>.
   *
   * @param capacity the capacity of <em>service data</em>
   */
  public abstract ServiceData setCapacity(long capacity);

  /**
   * When pulling the <em>service data</em> via the specified store type from
   * the related store path, StarGate Server can ensure whether user has
   * specified to cache this <em>service data</em> or not.
   *
   * @return {@code true} if <em>service data</em> has been specified to be
   *         cached by user, {@code false} otherwise
   */
  public abstract boolean isCached();

  /**
   * Cache data or not when pulling the <em>service data</em> by StarGate
   * Server, which is specified by user.
   *
   * @param cached {@code true} if cache data, {@code false} otherwise
   */
  public abstract ServiceData setCached(boolean cached);
}
