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

package tsinghua.stargate.api.impl;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ByteChannel;

import tsinghua.stargate.api.AcceleratorHandler;
import tsinghua.stargate.io.FpgaChannel;

public class AcceleratorHandlerImpl implements AcceleratorHandler {

  private ByteChannel channel;

  public AcceleratorHandlerImpl(ByteChannel channel) {
    this.channel = channel;
  }

  @Override
  public void reset(int cardId, int coreId) throws IOException {
    FpgaChannel.reset(cardId);
  }

  @Override
  public int send(ByteBuffer src) throws IOException {
    return channel.write(src);
  }

  @Override
  public int receive(ByteBuffer dst) throws IOException {
    return channel.read(dst);
  }

  @Override
  public void setOff(int off) throws IOException {
    ((FpgaChannel) channel).setOff(off);
  }

  @Override
  public void setConfigOp(int cardId, Object obj) throws IOException {
    FpgaChannel.setConfigOp(cardId, obj);
  }

  @Override
  public void close() throws IOException {
    channel.close();
  }
}
