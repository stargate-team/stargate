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

package tsinghua.stargate.rpc.factory.impl.thrift;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import tsinghua.stargate.conf.Configuration;
import tsinghua.stargate.exception.StarGateRuntimeException;
import tsinghua.stargate.rpc.factory.MessageFactory;
import tsinghua.stargate.util.ReflectionUtils;

/**
 * The singleton implementation of the {@link MessageFactory MessageFactory},
 * which mainly targets for creating those thrift-serializable protocol objects.
 */
public class MessageFactoryThriftImpl implements MessageFactory {

  private static final String IMPL_THRIFT_PACKAGE_SUFFIX = "thrift";

  private static final String IMPL_THRIFT_CLASS_SUFFIX = "ThriftImpl";

  private volatile static MessageFactoryThriftImpl self;

  private Configuration conf = new Configuration();

  /**
   * A cache used for storing the {@code Class} object and its
   * {@code Constructor} object pairs.
   */
  private ConcurrentMap<Class<?>, Constructor<?>> cache =
      new ConcurrentHashMap<>();

  private MessageFactoryThriftImpl() {
  }

  /**
   * Create only one instance by lazy instantiation. Employ `double-checked
   * locking` to reduce the use of synchronization in {@code get()}.
   *
   * @return the only instance of {@code MessageFactoryThriftImpl} at runtime
   */
  public static MessageFactoryThriftImpl get() {
    if (self == null) {
      synchronized (MessageFactoryThriftImpl.class) {
        if (self == null) {
          self = new MessageFactoryThriftImpl();
        }
      }
    }
    return self;
  }

  /**
   * Create a new instance of the given {@code Class} object via invoking the
   * default constructor. In case of searching the constructor every time, a
   * cache is employed.
   *
   * @param clazz the specific {@code Class} object
   * @return a new instance of the given {@code Class} object
   */
  @SuppressWarnings("unchecked")
  public <T> T getMsg(Class<T> clazz) {
    Constructor<?> constructor = cache.get(clazz);
    if (constructor == null) {
      Class<?> thriftClazz;
      try {
        thriftClazz = conf.getClassByName(getThriftImplClassName(clazz,
            IMPL_THRIFT_PACKAGE_SUFFIX, IMPL_THRIFT_CLASS_SUFFIX));
      } catch (ClassNotFoundException e) {
        throw new StarGateRuntimeException(
            "Failed to load class: [" + getThriftImplClassName(clazz,
                IMPL_THRIFT_PACKAGE_SUFFIX, IMPL_THRIFT_CLASS_SUFFIX) + "]",
            e);
      }
      try {
        constructor = thriftClazz.getConstructor();
        constructor.setAccessible(true);
        cache.putIfAbsent(clazz, constructor);
      } catch (NoSuchMethodException e) {
        throw new StarGateRuntimeException(
            "Could not find the default constructor of class: ["
                + getThriftImplClassName(clazz, IMPL_THRIFT_PACKAGE_SUFFIX,
                    IMPL_THRIFT_CLASS_SUFFIX)
                + "]",
            e);
      }
    }
    try {
      Object instance = constructor.newInstance();
      return (T) instance;
    } catch (InvocationTargetException e) {
      throw new StarGateRuntimeException(e);
    } catch (IllegalAccessException e) {
      throw new StarGateRuntimeException(e);
    } catch (InstantiationException e) {
      throw new StarGateRuntimeException(e);
    }
  }

  private String getThriftImplClassName(Class<?> clazz, String packageSuffix,
      String classSuffix) {
    return ReflectionUtils.get().getFullName(clazz, packageSuffix, classSuffix);
  }
}
