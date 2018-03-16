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

package tsinghua.stargate.storage.impl;

import java.nio.ByteBuffer;
import java.util.LinkedHashMap;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import tsinghua.stargate.api.RecordReader;
import tsinghua.stargate.api.RecordWriter;
import tsinghua.stargate.exception.StarGateException;
import tsinghua.stargate.storage.AbstractBlockStore;

/** Memory block store. */
public class BlockStoreMemoryImpl extends AbstractBlockStore {

  private static final Logger LOG =
      LoggerFactory.getLogger(BlockStoreMemoryImpl.class);

  // Get a lock with fair distribution for accelerator task queue updates
  private final ReadWriteLock rwl = new ReentrantReadWriteLock(true);
  private LinkedHashMap<String, ByteBuffer> cache =
      new LinkedHashMap(32, 0.75f, true);

  @Override
  public ByteBuffer readBytes(String path) throws StarGateException {
    ByteBuffer data = null;
    try {
      data = cache.get(path);
    } finally {
    }
    return data;
  }

  @Override
  public void writeBytes(String path, ByteBuffer data)
      throws StarGateException {
    try {
      data = cache.put(path, data);
    } finally {
    }
  }

  public boolean exists(String path) throws StarGateException {
    boolean isExist = false;
    try {
      isExist = cache.containsKey(path);
    } finally {
    }
    return isExist;
  }

  @Override
  public void delete(String path) throws StarGateException {
    try {
      cache.remove(path);
    } finally {
    }
  }

  @Override
  public void setRecordReader(RecordReader recordReader) {

  }

  @Override
  public void setRecordWriter(RecordWriter recordWriter) {

  }

  @Override
  public RecordReader getRecordReader() {
    return null;
  }

  @Override
  public RecordWriter getRecordWriter() {
    return null;
  }

  @Override
  public void stop() {

  }

}
