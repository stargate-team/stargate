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

package tsinghua.stargate.storage;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Iterator;

import org.apache.commons.lang3.tuple.ImmutablePair;

import tsinghua.stargate.Log;
import tsinghua.stargate.api.RecordReader;
import tsinghua.stargate.api.RecordWriter;
import tsinghua.stargate.exception.StarGateException;

public abstract class AbstractBlockStore extends Log implements BlockStore {

  @Override
  public Iterator<Object> getValues(final BlockId blockId) {
    debug("Attempting to read block:{}", blockId);

    final Iterator<String> pathIterator =
        Arrays.asList(blockId.getPaths()).iterator();

    return new Iterator<Object>() {
      Object tmp = null;

      @Override
      public boolean hasNext() {
        return pathIterator.hasNext();
      }

      @Override
      public Object next() {
        String path = pathIterator.next();
        try {
          tmp = new ImmutablePair<String, Object>(path, readBytes(path));
        } catch (StarGateException e) {
          error("attempt to read block {} failed", blockId);
        }
        return tmp;
      }

      @Override
      public void remove() {
        throw new UnsupportedOperationException();
      }
    };
  }

  protected abstract ByteBuffer readBytes(String s) throws StarGateException;

  @Override
  public boolean putValues(BlockId blockId, Iterator<Object> values,
      BlockStoreLevel level) {
    debug("Attempting to write block:{}", blockId);

    if (values == null) {
      warn("There is no data in block {}", blockId);
      return false;
    }

    long startTime = System.currentTimeMillis();
    int index = 0;
    try {
      while (values.hasNext()) {
        writeBytes(blockId.getPaths()[index], (ByteBuffer) values.next());
        index++;
      }
    } catch (StarGateException e) {
      error("Failed to write block {}", blockId);
      return false;
    } finally {
    }

    long timeTaken = System.currentTimeMillis() - startTime;
    debug("Writing costs {} ms", timeTaken);

    return true;
  }

  protected abstract void writeBytes(String s, ByteBuffer o)
      throws StarGateException;

  @Override
  public boolean remove(BlockId blockId) {
    info("Attempting to remove the block {}", blockId);
    try {
      for (String path : blockId.getPaths()) {
        delete(path);
      }
    } catch (StarGateException e) {
      return false;
    }
    return true;
  }

  protected abstract void delete(String path) throws StarGateException;

  public abstract void setRecordReader(RecordReader recordReader);

  public abstract void setRecordWriter(RecordWriter recordWriter);

  public abstract RecordReader getRecordReader();

  public abstract RecordWriter getRecordWriter();

  public abstract void stop();
}
