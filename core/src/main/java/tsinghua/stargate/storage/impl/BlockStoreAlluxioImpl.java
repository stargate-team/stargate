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

import java.io.IOException;
import java.nio.ByteBuffer;

import com.google.common.base.Preconditions;

import alluxio.AlluxioURI;
import alluxio.client.file.FileInStream;
import alluxio.client.file.FileOutStream;
import alluxio.client.file.FileSystem;
import alluxio.client.file.options.CreateFileOptions;
import alluxio.client.file.options.OpenFileOptions;
import alluxio.exception.AlluxioException;
import tsinghua.stargate.api.RecordReader;
import tsinghua.stargate.api.RecordWriter;
import tsinghua.stargate.exception.StarGateException;
import tsinghua.stargate.storage.AbstractBlockStore;

/** Alluxio block store. */
public class BlockStoreAlluxioImpl extends AbstractBlockStore {

  /**
   * Get binary data from the Alluxio block specified by pathname.
   *
   * <p>
   * The default read type of Alluxio is <em>CACHE_PROMOTE</em>, i.e. data is
   * moved to the highest tier in the worker where the data was read. If the
   * data was not in the local Alluxio worker, a replica will be added to the
   * local Alluxio worker from remote.
   *
   * @param path the path in StringRep
   * @return the data in bytes
   */
  @Override
  public ByteBuffer readBytes(String path) throws StarGateException {
    Preconditions.checkNotNull(path);
    AlluxioURI alluxioURI = new AlluxioURI(path);
    FileSystem fs = FileSystem.Factory.get();
    byte[] bytes;
    ByteBuffer buf = null;
    OpenFileOptions options = OpenFileOptions.defaults();
    try (FileInStream in = fs.openFile(alluxioURI, options)) {
      buf = ByteBuffer.allocate((int) in.remaining());
      bytes = buf.array();
      int nBytes = in.read(bytes);
      debug("Read {} bytes from {}", nBytes, path);
    } catch (AlluxioException e) {
      error("read alluxio file {} failed");
      throw new StarGateException(e);
    } catch (IOException e) {
      error("read alluxio file {} failed");
      throw new StarGateException(e);
    }
    return buf;
  }

  /**
   * Put binary data into the Alluxio block specified by pathname.
   *
   * <p>
   * The default location policy of Alluxio is <em>LocalFirstPolicy</em>, i.e.
   * when clients write data to Alluxio, it returns the data from local firstly
   * at first. If the local worker doesn't have enough capacity, Alluxio
   * randomly picks a worker from the list of active workers. The default
   * writing type is <em>MUST_CACHE</em>, i.e. all data is written to a Alluxio
   * worker synchronously.
   *
   * @param path the path in StringRep
   * @param data the data in bytes to be put
   */
  @Override
  public void writeBytes(String path, ByteBuffer data)
      throws StarGateException {
    Preconditions.checkNotNull(path);
    AlluxioURI alluxioURI = new AlluxioURI(path);
    FileSystem fs = FileSystem.Factory.get();
    CreateFileOptions options = CreateFileOptions.defaults();
    try (FileOutStream out = fs.createFile(alluxioURI, options)) {

      byte[] bytes = new byte[data.remaining()];
      // transfer bytes from this buffer into the given destination array
      data.get(bytes, 0, bytes.length);
      out.write(bytes);
      debug("Write {} bytes to {}", bytes.length, path);
    } catch (AlluxioException e) {
      error("write alluxio file {} failed");
      throw new StarGateException(e);
    } catch (IOException e) {
      error("write alluxio file {} failed");
      throw new StarGateException(e);
    }
  }

  /**
   * judge whether the data block exist or not
   *
   * @param path data block path
   * @return whether the data block exist or not
   */
  public boolean exists(String path) throws StarGateException {
    Preconditions.checkNotNull(path);
    AlluxioURI alluxioURI = new AlluxioURI(path);
    FileSystem fs = FileSystem.Factory.get();
    CreateFileOptions options = CreateFileOptions.defaults();
    boolean exist = false;
    try {
      exist = fs.exists(alluxioURI);
    } catch (AlluxioException e) {
      error("check alluxio file {} failed");
      throw new StarGateException(e);
    } catch (IOException e) {
      error("check alluxio file {} failed");
      throw new StarGateException(e);
    }
    return exist;
  }

  /**
   * delete the data block if the block path exist
   *
   * @param path data block path
   */
  @Override
  public void delete(String path) throws StarGateException {
    Preconditions.checkNotNull(path);
    AlluxioURI alluxioURI = new AlluxioURI(path);
    FileSystem fs = FileSystem.Factory.get();
    CreateFileOptions options = CreateFileOptions.defaults();
    boolean exist = false;
    try {
      if (fs.exists(alluxioURI)) {
        fs.delete(alluxioURI);
      }
    } catch (AlluxioException e) {
      error("delete alluxio file {} failed");
      throw new StarGateException(e);
    } catch (IOException e) {
      error("delete alluxio file {} failed");
      throw new StarGateException(e);
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
