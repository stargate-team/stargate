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

package tsinghua.stargate.io;

import java.io.Closeable;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;

/** The implementation of {@link FpgaChannel FpgaChannel}. */
public class FpgaChannelImpl extends FpgaChannel {

  private final FpgaDescriptor fd;
  private final FpgaDispatcher nd;
  private final Object parent;
  private final NativeThreadSet threads = new NativeThreadSet(2);
  private final Object lock = new Object();
  private int core;
  private int off;
  private boolean last;
  private int readTimeout;
  private int writeTimeout;
  private ModuleConfig config;

  private FpgaChannelImpl(FpgaDescriptor fd, Object parent, int core, int off,
      boolean last, int readTimeout, int writeTimeout) {
    this.fd = fd;
    this.nd = new FpgaDispatcherImpl();
    this.parent = parent;
    this.core = core;
    this.off = off;
    this.last = last;
    this.readTimeout = readTimeout;
    this.writeTimeout = writeTimeout;
  }

  // Constructor for Xilinx OpenCL FPGA
  private FpgaChannelImpl(FpgaDescriptor fd, Object parent, int core,
      ModuleConfig config) {
    this.fd = fd;
    this.nd = new FpgaDispatcherImpl();
    this.parent = parent;
    this.core = core;
    this.config = config;
  }

  public static FpgaChannel open(FpgaDescriptor fd, Object parent, int core,
      int off, boolean last, int readTimeout, int writeTimeout) {
    return new FpgaChannelImpl(fd, parent, core, off, last, readTimeout,
        writeTimeout);
  }

  public static FpgaChannel open(FpgaDescriptor fd, Object parent, int core,
      ModuleConfig config) {
    return new FpgaChannelImpl(fd, parent, core, config);
  }

  private void ensureOpen() throws IOException {
    if (!this.isOpen()) {
      throw new ClosedChannelException();
    }
  }

  @Override
  protected void implCloseChannel() throws IOException {
    this.threads.signalAndWait();

    if (this.parent != null) {
      ((Closeable) this.parent).close();
    } else {
      if (readTimeout != 0 && writeTimeout != 0) {
        this.nd.close(this.fd, 0); // Close a Riffa device.
      } else {
        this.nd.close(this.fd, 1); // Close a Xilinx OpenCL device.
      }
    }
  }

  @Override
  public int read(ByteBuffer dst) throws IOException {
    this.ensureOpen();
    synchronized (this.lock) {
      int size = 0;
      int index = -1;

      try {
        this.begin();
        index = this.threads.add();
        if (!this.isOpen()) {
          return 0;
        } else {
          if (readTimeout != 0) { // Read from a Riffa device
            size = IOUtils.read(this.fd, dst, this.nd, this.core,
                this.readTimeout);
          } else { // Read from a Xilinx OpenCL device
            size = IOUtils.read(this.fd, dst, this.nd, this.core);
          }
          return IOStatus.normalize(size);
        }
      } finally {
        this.threads.remove(index);
        this.end(size > 0);
        assert IOStatus.check(size);
      }
    }
  }

  @Override
  public int write(ByteBuffer src) throws IOException {
    this.ensureOpen();
    synchronized (this.lock) {
      int size = 0;
      int index = -1;

      byte code;
      try {
        this.begin();
        index = this.threads.add();
        if (this.isOpen()) {
          if (writeTimeout != 0) { // Write into a Riffa device
            size = IOUtils.write(this.fd, src, this.nd, this.core, this.off,
                this.last, this.writeTimeout);
          } else { // Write into a Xilinx OpenCL device
            size = IOUtils.write(this.fd, src, this.nd, this.core);
          }
          return IOStatus.normalize(size);
        }
        code = 0;
      } finally {
        this.threads.remove(index);
        this.end(size > 0);
        assert IOStatus.check(size);
      }
      return code;
    }
  }

  @Override
  public void setCore(int core) throws IOException {
    this.core = core;
  }

  @Override
  public void setOff(int off) throws IOException {
    this.off = off;
  }

  @Override
  public void setLast(boolean last) throws IOException {
    this.last = last;
  }

  @Override
  public void setReadTimeout(int readTimeout) throws IOException {
    this.readTimeout = readTimeout;
  }

  @Override
  public void setWriteTimeout(int writeTimeout) throws IOException {
    this.writeTimeout = writeTimeout;
  }
}
