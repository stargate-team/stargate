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

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ByteChannel;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.nio.channels.spi.AbstractInterruptibleChannel;

import tsinghua.stargate.io.Fpga.FpgaInfo;

/**
 * A channel for reading, writing, and manipulating a FPGA.
 *
 * <p>
 * A FPGA channel is a {@link ByteChannel} that is connected to a FPGA. The FPGA
 * itself contains a fixed-length sequence of bytes that can be read and
 * written.
 *
 * <p>
 * A FPGA channel may own many sub-channels whose size depends upon the
 * capabilities of underlying driver and hardware implementations. Now the
 * driver supports maximum 12 sub-channels in ideal situation. However, hardware
 * resources and implementations must be considered in reality.
 */
public abstract class FpgaChannel extends AbstractInterruptibleChannel
    implements ByteChannel {

  private static ModuleConfig channelConfig;

  /** Initializes a new instance of this class. */
  protected FpgaChannel() {
  }

  /**
   * Open a FPGA, returning a FPGA channel to access the FPGA.
   *
   * @param id the FPGA card identification
   * @param core the index of sub-channel
   * @return a new FPGA channel
   * @throws IOException If an I/O error occurs
   */
  public static FpgaChannel open(int id, int core) throws IOException {
    return open(id, core, 0, true);
  }

  /**
   * Open a FPGA, returning a FPGA channel to access the FPGA.
   *
   * @param id the FPGA card identification
   * @param core the index of sub-channel
   * @param off the start offset of writing this data. Note: Only the least
   *          significant 31 bits are sent (not all 32)
   * @param last true if this transfer is the last in a sequence of transfers,
   *          false otherwise
   * @return a new FPGA channel
   * @throws IOException If an I/O error occurs
   */
  public static FpgaChannel open(int id, int core, int off, boolean last)
      throws IOException {
    return open(id, core, off, last, 25000, 25000);
  }

  /**
   * Open a FPGA, returning a FPGA channel to access the FPGA.
   *
   * @param id the FPGA card identification
   * @param core the index of sub-channel
   * @param off the start offset of writing this data. Note: Only the least
   *          significant 31 bits are sent (not all 32)
   * @param last true if this transfer is the last in a sequence of transfers,
   *          false otherwise
   * @param readTimeout the read timeout value in ms. 0 means no timeout is
   *          specified. Otherwise, the host has to wait for {@code timeout} ms
   *          before give up this connection with FPGA
   * @param writeTimeout the write timeout value in ms. 0 means no timeout is
   *          specified. Otherwise, the host has to wait for {@code timeout} ms
   *          before give up this connection with FPGA
   * @return a new FPGA channel
   * @throws IOException If an I/O error occurs
   */
  public static FpgaChannel open(int id, int core, int off, boolean last,
      int readTimeout, int writeTimeout) throws IOException {
    return ChannelFactory.newFpgaChannel(id, core, off, last, readTimeout,
        writeTimeout);
  }

  /**
   * Open a FPGA, returning a FPGA channel to access the FPGA.
   *
   * @param id the FPGA card identification
   * @param core the index of sub-channel
   * @return a new FPGA channel
   * @throws IOException If an I/O error occurs
   */
  public static FpgaChannel open(int id, int core, ModuleConfig config)
      throws IOException {
    channelConfig = config;
    return ChannelFactory.newFpgaChannel(id, core, config);
  }

  /**
   * Reads a sequence of bytes from this channel into the given buffer.
   *
   * <p>
   * This method behaves exactly as specified in the {@link ReadableByteChannel}
   * interface.
   */
  public abstract int read(ByteBuffer dst) throws IOException;

  /**
   * Writes a sequence of bytes to this channel from the given buffer.
   *
   * <p>
   * This method behaves exactly as specified by the {@link WritableByteChannel}
   * interface.
   */
  public abstract int write(ByteBuffer src) throws IOException;

  // -- Other operations --

  public static FpgaInfo getInfo() throws IOException {
    return IOUtils.list(new FpgaDispatcherImpl());
  }

  public static void reset(int id) throws IOException {
    long fdVal;
    int flag;

    if (channelConfig == null) {
      fdVal = FpgaDispatcherImpl.open(id);
      flag = 0;
    } else {
      fdVal = FpgaDispatcherImpl.open(id, channelConfig);
      flag = 1;
    }

    FpgaDescriptor fd = IOUtils.newFD(fdVal);
    FpgaDispatcher nd = new FpgaDispatcherImpl();
    IOUtils.reset(fd, nd);

    if (channelConfig == null) {
      nd.close(fd, flag);
    } else {
      nd.close(fd, flag);
    }
  }

  // Set the configuration of a Xilinx OpenCL device.
  public static void setConfigOp(int id, Object obj) throws IOException {
//    FpgaDispatcherImpl.setConfigOp(id, (ModuleConfig) obj);
  }

  public abstract void setCore(int core) throws IOException;

  public abstract void setOff(int off) throws IOException;

  public abstract void setLast(boolean last) throws IOException;

  public abstract void setReadTimeout(int readTimeout) throws IOException;

  public abstract void setWriteTimeout(int writeTimeout) throws IOException;
}
