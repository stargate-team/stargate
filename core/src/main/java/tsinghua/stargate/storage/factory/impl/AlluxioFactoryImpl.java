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
import tsinghua.stargate.storage.factory.AlluxioFactory;
import tsinghua.stargate.storage.impl.BlockStoreAlluxioImpl;
import tsinghua.stargate.util.ReflectionUtils;

/**
 * The implementation of {@link AlluxioFactory}.
 *
 * <p>
 * It is used for getting and stopping the alluxio block store.
 *
 * @see BlockStoreAlluxioImpl
 */
public class AlluxioFactoryImpl extends Log implements AlluxioFactory {

  private static final String IMPL_SUFFIX = "impl";
  private static final String ALLUXIO_IMPL_SUFFIX = "AlluxioImpl";

  private static final AlluxioFactoryImpl self = new AlluxioFactoryImpl();

  private Configuration selfConf = new Configuration();

  private ConcurrentMap<Class<?>, Constructor<?>> cache =
      new ConcurrentHashMap<>();

  private AlluxioFactoryImpl() {
  }

  public static AlluxioFactoryImpl get() {
    return AlluxioFactoryImpl.self;
  }

  @Override
  public Object getAlluxio(Class<?> blockStore, Configuration conf) {
    Constructor<?> alluxioConstructor = cache.get(blockStore);
    if (alluxioConstructor == null) {
      Class<?> clazz;
      try {
        clazz = selfConf.getClassByName(getAlluxioImplClassName(blockStore,
            IMPL_SUFFIX, ALLUXIO_IMPL_SUFFIX));
      } catch (ClassNotFoundException e) {
        throw new StarGateRuntimeException(
            "Failed to load class: [" + getAlluxioImplClassName(blockStore,
                IMPL_SUFFIX, ALLUXIO_IMPL_SUFFIX) + "]",
            e);
      }
      try {
        alluxioConstructor = clazz.getConstructor();
        alluxioConstructor.setAccessible(true);
        cache.putIfAbsent(blockStore, alluxioConstructor);
      } catch (NoSuchMethodException e) {
        error("Error in getting the constructor of `BlockStoreAlluxioImpl`."
            + "\nExiting...");
        throw new StarGateRuntimeException(
            "Could not find constructor with params: " + Configuration.class,
            e);
      }
    }
    try {
      return alluxioConstructor.newInstance();
    } catch (InvocationTargetException e) {
      error("Error in constructing a `BlockStoreAlluxioImpl` instance, "
          + "since the underlying constructor throws an exception."
          + "\nExiting...");
      throw new StarGateRuntimeException(e);
    } catch (IllegalAccessException e) {
      error("Error in constructing a `BlockStoreAlluxioImpl` instance, "
          + "since this constructor object is enforcing Java language "
          + "access control and the underlying constructor is inaccessible."
          + "\nExiting...");
      throw new StarGateRuntimeException(e);
    } catch (InstantiationException e) {
      error("Error in constructing a `BlockStoreAlluxioImpl` instance, "
          + "since the class that declares the underlying constructor "
          + "represents an abstract class." + "\nExiting...");
      throw new StarGateRuntimeException(e);
    }
  }

  private String getAlluxioImplClassName(Class<?> clazz, String packageSuffix,
      String classSuffix) {
    return ReflectionUtils.get().getFullName(clazz, packageSuffix, classSuffix);
  }

  @Override
  public void stopAlluxio(Object blockStore) {
  }
}
