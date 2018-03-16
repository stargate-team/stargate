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

import static java.nio.file.StandardOpenOption.*;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SeekableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.EnumSet;
import java.util.Map;

import com.google.common.base.Preconditions;

import tsinghua.stargate.Log;
import tsinghua.stargate.api.RecordReader;
import tsinghua.stargate.api.RecordWriter;
import tsinghua.stargate.conf.Configuration;
import tsinghua.stargate.exception.StarGateException;
import tsinghua.stargate.storage.AbstractBlockStore;
import tsinghua.stargate.util.PathUtils;

/** Disk block store. */
public class BlockStoreDiskImpl extends AbstractBlockStore {

  private RecordReader recordReader = new DefaultRecordReader();
  private RecordWriter recordWriter = new DefaultRecordWriter();

  private final int SUB_DIRECTORIES = 64;
  private Configuration conf;
  private File[] localDirs;
  private File[][] subDirs;

  public BlockStoreDiskImpl(Configuration conf) {
    this.conf = conf;
    localDirs = createLocalDirs(conf);
    if (localDirs != null) {
      subDirs = createSubLocalDirs(localDirs.length);
    }
  }

  @Override
  public ByteBuffer readBytes(String path) throws StarGateException {
    return recordReader.readBytes(path);
  }

  @Override
  public void writeBytes(String path, ByteBuffer buf) throws StarGateException {
    recordWriter.writeBytes(path, buf);
  }

  public boolean exists(String path) {
    Path diskPath = Paths.get(path);
    return Files.exists(diskPath);
  }

  @Override
  public void delete(String path) throws StarGateException {
    Path diskPath = Paths.get(path);
    try {
      Files.delete(diskPath);
    } catch (IOException e) {
      error("disk delete file failed");
      throw new StarGateException(e);
    }
  }

  @Override
  public void setRecordReader(RecordReader recordReader) {
    this.recordReader = recordReader;
  }

  @Override
  public void setRecordWriter(RecordWriter recordWriter) {
    this.recordWriter = recordWriter;
  }

  @Override
  public RecordReader getRecordReader() {
    return this.recordReader;
  }

  @Override
  public RecordWriter getRecordWriter() {
    return this.recordWriter;
  }

  public String writeHashFile(String originPath, ByteBuffer buffer)
      throws StarGateException {
    String fileName = PathUtils.getFilename(originPath);
    String storeFileName = null;
    try {
      storeFileName = new File(getStoreDir(fileName), fileName).toString();
      writeBytes(storeFileName, buffer);
    } catch (IOException e) {
      error("Failed to write hash file {}", storeFileName);
      throw new StarGateException(e);
    }
    return storeFileName;
  }

  private String getStoreDir(String filename) throws IOException {
    int hash = PathUtils.nonNegativeHash(filename);
    int dirId = hash % localDirs.length;
    int subDirId = (hash / localDirs.length) % SUB_DIRECTORIES;

    // Create the subdirectory if it doesn't already exist
    File tmp;
    synchronized (subDirs[dirId]) {
      File old = subDirs[dirId][subDirId];
      if (old != null) {
        tmp = old;
      } else {
        File newDir =
            new File(localDirs[dirId], String.format("%02x", subDirId));
        if (!newDir.exists() && !newDir.mkdir()) {
          throw new IOException(
              "Failed to create local dir in " + newDir.getName());
        }
        subDirs[dirId][subDirId] = newDir;
        tmp = newDir;
      }
    }
    return tmp.toString();
  }

  private File[] createLocalDirs(Configuration conf) {
    try {
      return PathUtils.createLocalDirs(conf).toArray(new File[0]);
    } catch (IOException e) {
      error("Failed to create local dir");
    }
    return null;
  }

  private File[][] createSubLocalDirs(int length) {
    File[][] subDirs = new File[length][];
    for (int i = 0; i < subDirs.length; i++) {
      subDirs[i] = new File[SUB_DIRECTORIES];
    }
    return subDirs;
  }

  @Override
  public void stop() {
    for (File localDir : localDirs) {
      info("disk store delete tmp directory:{}", localDir.toString());
      if (localDir.isDirectory() && localDir.exists()) {
        try {
          PathUtils.deleteRecursively(localDir);
        } catch (IOException e) {
          error("Failed to create local dir {}", localDir.getAbsolutePath());
        }
      }
    }
  }

  class DefaultRecordReader extends Log implements RecordReader {
    /**
     * Get binary blocks from a disk file.
     *
     * @param path the file path in StringRep
     * @return the binary blocks
     */

    @Override
    public ByteBuffer readBytes(String path) throws StarGateException {
      Path diskPath = Paths.get(path);
      Preconditions.checkNotNull(path);
      ByteBuffer buf;
      int size;

      try (SeekableByteChannel channel = Files.newByteChannel(diskPath)) {
        size = (int) channel.size();
        buf = ByteBuffer.allocate(size);
        channel.read(buf);
        buf.flip();
      } catch (IOException e) {
        error("disk Failed to read file");
        throw new StarGateException(e);
      }

      return buf;
    }

    @Override
    public Map<Long, Long> getPos() {
      return null;
    }

    @Override
    public long getSize() {
      return 0;
    }
  }

  class DefaultRecordWriter extends Log implements RecordWriter {
    @Override
    public void writeBytes(String path, ByteBuffer buf)
        throws StarGateException {
      Path diskPath = Paths.get(path);
      Preconditions.checkNotNull(path);

      PathUtils.mkDirRecursively(new File(diskPath.getParent().toString()));

      try (WritableByteChannel channel =
          Files.newByteChannel(diskPath, EnumSet.of(CREATE, WRITE, APPEND))) {
        channel.write(buf);
      } catch (IOException e) {
        error("disk Failed to write file");
        throw new StarGateException(e);
      }
    }
  }
}
