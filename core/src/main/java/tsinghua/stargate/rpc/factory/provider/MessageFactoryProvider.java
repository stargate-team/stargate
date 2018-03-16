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

package tsinghua.stargate.rpc.factory.provider;

import tsinghua.stargate.conf.Configuration;
import tsinghua.stargate.conf.NameSpace;
import tsinghua.stargate.rpc.factory.MessageFactory;
import tsinghua.stargate.util.ReflectionUtils;

/**
 * A factory provider to create new class instances, according to a user-defined
 * or system-default configuration.
 *
 * @see MessageFactory
 */
public class MessageFactoryProvider {

  private static Configuration defaultConf;

  static {
    defaultConf = new Configuration();
  }

  /**
   * Get a factory for producing RPC messages and entities.
   *
   * @param conf a configuration profiler
   * @return a specific {@link MessageFactory MessageFactory} implementation
   */
  public static MessageFactory getMessageFactory(Configuration conf) {
    // Assuming the default configuration has the correct factory set.
    // Users can specify a particular factory by providing a configuration.
    if (conf == null)
      conf = defaultConf;
    String factoryName = conf.get(NameSpace.RPC_MESSAGE_FACTORY,
        NameSpace.DEFAULT_RPC_MESSAGE_FACTORY);
    return (MessageFactory) getFactory(factoryName);
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
