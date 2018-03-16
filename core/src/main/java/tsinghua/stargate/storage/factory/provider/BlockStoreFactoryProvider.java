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

package tsinghua.stargate.storage.factory.provider;

import tsinghua.stargate.conf.Configuration;
import tsinghua.stargate.conf.NameSpace;
import tsinghua.stargate.storage.factory.AlluxioFactory;
import tsinghua.stargate.storage.factory.DiskFactory;
import tsinghua.stargate.storage.factory.MemoryFactory;
import tsinghua.stargate.util.ReflectionUtils;

/**
 * A factory provider for creating block storage factory, according to a
 * user-defined or system-default configuration.
 *
 * @see MemoryFactory
 * @see DiskFactory
 * @see AlluxioFactory
 */
public class BlockStoreFactoryProvider {

  private BlockStoreFactoryProvider() {
  }

  /**
   * Get a factory for creating Memory block store.
   *
   * @param conf a configuration profiler
   * @return a specific {@link MemoryFactory MemoryFactory} implementation
   */
  public static MemoryFactory getMemoryFactory(Configuration conf) {
    String factoryName = conf.get(NameSpace.STORAGE_FACTORY_MEMORY,
        NameSpace.DEFAULT_STORAGE_FACTORY_MEMORY);
    return (MemoryFactory) getFactory(factoryName);
  }

  /**
   * Get a factory for creating Disk block store.
   *
   * @param conf a configuration profiler
   * @return a specific {@link DiskFactory DiskFactory} implementation
   */
  public static DiskFactory getDiskFactory(Configuration conf) {
    String factoryName = conf.get(NameSpace.STORAGE_FACTORY_DISK,
        NameSpace.DEFAULT_STORAGE_FACTORY_DISK);
    return (DiskFactory) getFactory(factoryName);
  }

  /**
   * Get a factory for creating Alluxio block store.
   *
   * @param conf a configuration profiler
   * @return a specific {@link AlluxioFactory AlluxioFactory} implementation
   */
  public static AlluxioFactory getAlluxioFactory(Configuration conf) {
    String factoryName = conf.get(NameSpace.STORAGE_FACTORY_ALLUXIO,
        NameSpace.DEFAULT_STORAGE_FACTORY_ALLUXIO);
    return (AlluxioFactory) getFactory(factoryName);
  }

  /**
   * Get a factory instance for the provided {@code clazzName clazzName}.
   *
   * @param clazzName the class name of factory implementation
   * @return a newly constructed factory instance
   */
  private static Object getFactory(String clazzName) {
    return ReflectionUtils.get().getFactory(clazzName);
  }
}
