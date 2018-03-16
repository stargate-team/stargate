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

package tsinghua.stargate.util;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import tsinghua.stargate.Log;
import tsinghua.stargate.conf.StarGateConf;
import tsinghua.stargate.exception.StarGateRuntimeException;
import tsinghua.stargate.rpc.factory.MessageFactory;
import tsinghua.stargate.rpc.factory.provider.MessageFactoryProvider;

/**
 * Generic reflection utility methods for getting or creating class instances.
 *
 * <p>
 * {@code ReflectionUtils ReflectionUtils} is mainly implemented by employing
 * `The Singleton Pattern`. It also employs {@link MessageFactoryProvider
 * MessageFactoryProvider} to provide a factory for creating various RPC
 * messages or entities via `The Factory Method Pattern`.
 */
public class ReflectionUtils extends Log {

  private volatile static ReflectionUtils self;

  /**
   * Cache for class constructors. Pin classes so they can't be garbage
   * collected until {@code ReflectionUtils ReflectionUtils} can be collected.
   */
  private final Map<Class<?>, Constructor<?>> CLASS_CONSTRUCTOR_CACHE =
      new ConcurrentHashMap<>();
  /**
   * An empty array of {@code Class Class} objects stands for constructor's
   * formal parameter types.
   */
  private final Class<?>[] EMPTY_PARAMETER = new Class[] {};

  /** Factory for creating RPC messages and entities. */
  private static final MessageFactory MESSAGE_FACTORY =
      MessageFactoryProvider.getMessageFactory(new StarGateConf());

  private ReflectionUtils() {
  }

  /**
   * Get the {@code ReflectionUtils ReflectionUtils} singleton by lazy
   * instantiation. Employ `double-checked locking` to reduce the use of
   * synchronization in {@code get() get()}.
   *
   * @return {@code ReflectionUtils ReflectionUtils} singleton
   */
  public static ReflectionUtils get() {
    if (self == null)
      synchronized (ReflectionUtils.class) {
        if (self == null)
          self = new ReflectionUtils();
      }
    return self;
  }

  /**
   * Get a cached or new instance as per the given {@code clazzName clazzName}
   * and {@code classLoader classLoader}.
   *
   * <p>
   * This method is targeted for creating instances of those classes that their
   * constructors have no any input parameter.
   *
   * @param clazzName a given class name
   * @param classLoader a given class loader
   * @return a new instance of the given {@code clazzName clazzName} and
   *         {@code classLoader classLoader}
   */
  public Object getInstance(String clazzName, ClassLoader classLoader) {
    try {
      Class<?> clazz = Class.forName(clazzName, true, classLoader);
      return getInstance(clazz);
    } catch (ClassNotFoundException e) {
      throw new StarGateRuntimeException(e);
    }
  }

  @SuppressWarnings("unchecked")
  public <T> T getInstance(Class<T> clazz) {
    T result;
    try {
      Constructor<T> constructor =
          (Constructor<T>) CLASS_CONSTRUCTOR_CACHE.get(clazz);
      if (constructor == null) {
        constructor = clazz.getDeclaredConstructor(EMPTY_PARAMETER);
        constructor.setAccessible(true);
        CLASS_CONSTRUCTOR_CACHE.put(clazz, constructor);
      }
      result = constructor.newInstance();
    } catch (Exception e) {
      throw new StarGateRuntimeException(e);
    }
    return result;
  }

  /**
   * Get a cached or newly created factory instance.
   *
   * <p>
   * This method is only targeted for creating instances of those factory
   * implementation classes containing a static {@code get() get()} method.
   *
   * @param factoryName a given factory name
   * @return a cached or newly created factory instance
   */
  public Object getFactory(String factoryName) {
    try {
      Class<?> clazz = Class.forName(factoryName);
      debug("Getting factory {}", clazz.getSimpleName());
      Method method = clazz.getMethod("get", null);
      method.setAccessible(true);
      return method.invoke(null, null);
    } catch (ClassNotFoundException e) {
      throw new StarGateRuntimeException(e);
    } catch (NoSuchMethodException e) {
      throw new StarGateRuntimeException(e);
    } catch (InvocationTargetException e) {
      throw new StarGateRuntimeException(e);
    } catch (IllegalAccessException e) {
      throw new StarGateRuntimeException(e);
    }
  }

  /**
   * Get a cached or newly created instance of RPC message or entity.
   *
   * <p>
   * This method employs {@code MessageFactory MessageFactory} to get a RPC
   * message or entity instance.
   *
   * @param clazz RPC message or entity class
   * @param <T> the type of protocol
   * @return a cached or newly created instance of RPC message or entity
   */
  public <T> T getMsg(Class<T> clazz) {
    return MESSAGE_FACTORY.getMsg(clazz);
  }

  public String getFullName(Class<?> clazz, String packageSuffix,
      String classSuffix) {
    String srcPackageName = getPackageName(clazz);
    String srcClazzName = getClassName(clazz);
    String dstPackageName = srcPackageName + "." + packageSuffix;
    String dstClazzName = srcClazzName + classSuffix;
    return dstPackageName + "." + dstClazzName;
  }

  public String getFullName(Class<?> clazz, String packageName,
      String classSuffix, String innerClassName) {
    return packageName + "." + getClassName(clazz) + classSuffix + "$"
        + innerClassName;
  }

  private String getPackageName(Class<?> clazz) {
    return clazz.getPackage().getName();
  }

  private String getClassName(Class<?> clazz) {
    return clazz.getSimpleName();
  }
}
