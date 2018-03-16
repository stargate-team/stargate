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

package tsinghua.stargate.api.factory.impl;

import java.io.IOException;
import java.nio.channels.ByteChannel;

import tsinghua.stargate.api.AcceleratorHandler;
import tsinghua.stargate.api.factory.AcceleratorHandlerFactory;
import tsinghua.stargate.api.impl.AcceleratorHandlerImpl;
import tsinghua.stargate.io.ModuleConfig;
import tsinghua.stargate.util.FpgaUtils;

public class FpgaHandlerFactoryImpl implements AcceleratorHandlerFactory {

  private static final FpgaHandlerFactoryImpl self = new FpgaHandlerFactoryImpl();

  private FpgaHandlerFactoryImpl() {
  }

  public static FpgaHandlerFactoryImpl instance() {
    return FpgaHandlerFactoryImpl.self;
  }

  @Override
  synchronized public AcceleratorHandler getClient(int cardId, int coreId)
      throws IOException {
    ByteChannel channel = FpgaUtils.newByteChannel(cardId, coreId);
    return new AcceleratorHandlerImpl(channel);
  }

  @Override
  synchronized public AcceleratorHandler getClient(int cardId, int coreId,
                                                   ModuleConfig config) throws IOException {
    ByteChannel channel = FpgaUtils.newByteChannel(cardId, coreId, config);
    return new AcceleratorHandlerImpl(channel);
  }

  @Override
  public void stopClient(AcceleratorHandler client) throws IOException {
    client.close();
  }
}
