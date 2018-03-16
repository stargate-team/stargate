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

import java.nio.ByteBuffer;
import java.util.Map;

import tsinghua.stargate.api.AcceleratorHandler;
import tsinghua.stargate.api.AcceleratorRequest;
import tsinghua.stargate.api.RecordReader;

/**
 * Provides a convenient implementation of the AcceleratorRequest interface
 * 
 * @see AcceleratorRequest
 */

public class AcceleratorRequestImpl implements AcceleratorRequest {

  private AcceleratorHandler client;
  private Map<String, ByteBuffer> buffers;

  private int cardId;

  private int coreId;

  private RecordReader recordReader;

  public AcceleratorRequestImpl(AcceleratorHandler client,
      Map<String, ByteBuffer> buffers) {
    this.client = client;
    this.buffers = buffers;
  }

  public AcceleratorRequestImpl(int cardId, int coreId,
      Map<String, ByteBuffer> buffers, RecordReader recordReader) {
    this.cardId = cardId;
    this.coreId = coreId;
    this.buffers = buffers;
    this.recordReader = recordReader;
  }

  @Override
  public Map<String, ByteBuffer> getInputData() {
    return buffers;
  }

  @Override
  public AcceleratorHandler getClient() {
    return client;
  }

  @Override
  public int getCardId() {
    return cardId;
  }

  @Override
  public int getCoreId() {
    return coreId;
  }

  @Override
  public RecordReader getRecordReader() {
    return recordReader;
  }

}
