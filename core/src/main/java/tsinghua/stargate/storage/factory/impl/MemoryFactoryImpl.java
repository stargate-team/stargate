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

package tsinghua.stargate.storage.factory.impl;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import tsinghua.stargate.Log;
import tsinghua.stargate.conf.Configuration;
import tsinghua.stargate.exception.StarGateRuntimeException;
import tsinghua.stargate.storage.factory.MemoryFactory;
import tsinghua.stargate.storage.impl.BlockStoreMemoryImpl;
import tsinghua.stargate.util.ReflectionUtils;

/**
 * The implementation of {@link MemoryFactory}.
 *
 * <p>
 * It is used for getting and stopping the memory block store.
 *
 * @see BlockStoreMemoryImpl
 */
public class MemoryFactoryImpl extends Log implements MemoryFactory {

  private static final String IMPL_SUFFIX = "impl";
  private static final String MEMORY_IMPL_SUFFIX = "MemoryImpl";

  private static final MemoryFactoryImpl self = new MemoryFactoryImpl();

  private Configuration selfConf = new Configuration();

  private ConcurrentMap<Class<?>, Constructor<?>> cache =
      new ConcurrentHashMap<>();

  private MemoryFactoryImpl() {
  }

  public static MemoryFactoryImpl get() {
    return MemoryFactoryImpl.self;
  }

  @Override
  public Object getMemory(Class<?> blockStore, Configuration conf) {
    Constructor<?> memoryConstructor = cache.get(blockStore);
    if (memoryConstructor == null) {
      Class<?> clazz;
      try {
        clazz = selfConf.getClassByName(getMemoryImplClassName(blockStore,
            IMPL_SUFFIX, MEMORY_IMPL_SUFFIX));
      } catch (ClassNotFoundException e) {
        throw new StarGateRuntimeException(
            "Failed to load class: [" + getMemoryImplClassName(blockStore,
                IMPL_SUFFIX, MEMORY_IMPL_SUFFIX) + "]",
            e);
      }
      try {
        memoryConstructor = clazz.getConstructor();
        memoryConstructor.setAccessible(true);
        cache.putIfAbsent(blockStore, memoryConstructor);
      } catch (NoSuchMethodException e) {
        error("Error in getting the constructor of `BlockStoreMemoryImpl`."
            + "\nExiting...");
        throw new StarGateRuntimeException(
            "Could not find constructor with params: " + Configuration.class,
            e);
      }
    }

    try {
      return memoryConstructor.newInstance();
    } catch (InvocationTargetException e) {
      error("Error in constructing a `BlockStoreMemoryImpl` instance, "
          + "since the underlying constructor throws an exception."
          + "\nExiting...");
      throw new StarGateRuntimeException(e);
    } catch (IllegalAccessException e) {
      error("Error in constructing a `BlockStoreMemoryImpl` instance, "
          + "since this constructor object is enforcing Java language "
          + "access control and the underlying constructor is inaccessible."
          + "\nExiting...");
      throw new StarGateRuntimeException(e);
    } catch (InstantiationException e) {
      error("Error in constructing a `BlockStoreMemoryImpl` instance, "
          + "since the class that declares the underlying constructor "
          + "represents an abstract class." + "\nExiting...");
      throw new StarGateRuntimeException(e);
    }
  }

  private String getMemoryImplClassName(Class<?> clazz, String packageSuffix,
      String classSuffix) {
    return ReflectionUtils.get().getFullName(clazz, packageSuffix, classSuffix);
  }

  @Override
  public void stopMemory(Object blockStore) {
  }
}
