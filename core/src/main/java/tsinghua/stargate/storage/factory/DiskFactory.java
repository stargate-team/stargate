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

package tsinghua.stargate.storage.factory;

import tsinghua.stargate.conf.Configuration;

/**
 * A factory for creating disk block store.
 *
 * @see tsinghua.stargate.storage.factory.impl.DiskFactoryImpl
 */
public interface DiskFactory {

  /**
   * Get a disk block store.
   *
   * <p>
   * The constructor for creating the disk block store may come from a cache or
   * a new one that would be finally added into the cache. Note: The constructor
   * of the same {@code blockStore} are reused.
   *
   * @param blockStore the accelerator block store interface
   * @param conf the configuration profiler
   * @return a disk block store
   */
  Object getDisk(Class<?> blockStore, Configuration conf);

  /**
   * Stop the specified disk block store.
   *
   * @param blockStore the disk block store
   */
  void stopDisk(Object blockStore);
}
