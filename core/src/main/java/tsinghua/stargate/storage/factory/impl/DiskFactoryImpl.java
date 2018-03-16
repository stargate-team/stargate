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
import tsinghua.stargate.storage.factory.DiskFactory;
import tsinghua.stargate.storage.impl.BlockStoreDiskImpl;
import tsinghua.stargate.util.ReflectionUtils;

/**
 * The implementation of {@link DiskFactory}.
 *
 * <p>
 * It is used for getting and stopping the disk block store.
 *
 * @see BlockStoreDiskImpl
 */
public class DiskFactoryImpl extends Log implements DiskFactory {

  private static final String IMPL_SUFFIX = "impl";
  private static final String DISK_IMPL_SUFFIX = "DiskImpl";

  private static final DiskFactoryImpl self = new DiskFactoryImpl();

  private ConcurrentMap<Class<?>, Constructor<?>> cache =
      new ConcurrentHashMap<>();

  private DiskFactoryImpl() {
  }

  public static DiskFactoryImpl get() {
    return DiskFactoryImpl.self;
  }

  @Override
  public Object getDisk(Class<?> blockStore, Configuration conf) {
    Constructor<?> diskConstructor = cache.get(blockStore);
    if (diskConstructor == null) {
      Class<?> clazz;
      try {
        clazz = conf.getClassByName(
            getDiskImplClassName(blockStore, IMPL_SUFFIX, DISK_IMPL_SUFFIX));
      } catch (ClassNotFoundException e) {
        throw new StarGateRuntimeException("Failed to load class: ["
            + getDiskImplClassName(blockStore, IMPL_SUFFIX, DISK_IMPL_SUFFIX)
            + "]", e);
      }
      try {
        diskConstructor = clazz.getConstructor(Configuration.class);
        diskConstructor.setAccessible(true);
        cache.putIfAbsent(blockStore, diskConstructor);
      } catch (NoSuchMethodException e) {
        error("Error in getting the constructor of `BlockStoreDiskImpl`."
            + "\nExiting...");
        throw new StarGateRuntimeException(
            "Could not find constructor with params: " + Configuration.class,
            e);
      }
    }
    try {
      return diskConstructor.newInstance(conf);
    } catch (InvocationTargetException e) {
      error("Error in constructing a `BlockStoreDiskImpl` instance, "
          + "since the underlying constructor throws an exception."
          + "\nExiting...");
      throw new StarGateRuntimeException(e);
    } catch (IllegalAccessException e) {
      error("Error in constructing a `BlockStoreDiskImpl` instance, "
          + "since this constructor object is enforcing Java language "
          + "access control and the underlying constructor is inaccessible."
          + "\nExiting...");
      throw new StarGateRuntimeException(e);
    } catch (InstantiationException e) {
      error("Error in constructing a `BlockStoreDiskImpl` instance, "
          + "since the class that declares the underlying constructor "
          + "represents an abstract class." + "\nExiting...");
      throw new StarGateRuntimeException(e);
    }
  }

  private String getDiskImplClassName(Class<?> clazz, String packageSuffix,
      String classSuffix) {
    return ReflectionUtils.get().getFullName(clazz, packageSuffix, classSuffix);
  }

  @Override
  public void stopDisk(Object blockStore) {
  }
}
