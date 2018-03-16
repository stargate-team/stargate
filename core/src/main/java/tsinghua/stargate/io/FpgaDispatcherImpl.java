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

import static tsinghua.stargate.io.Fpga.FpgaInfo;

import java.io.IOException;

public class FpgaDispatcherImpl implements FpgaDispatcher {

  public FpgaDispatcherImpl() {
  }

  public static long open(int id) throws IOException {
    return open0(id);
  }

  // Set the configuration of a Xilinx OpenCL device.
  public static void setConfigOp(int id, ModuleConfig config)
      throws IOException {
    setDevice(id, config);
  }

  // Open a Xilinx OpenCL device.
  public static long open(int id, ModuleConfig config) throws IOException {
    return openDevice(id, config);
  }

  // -- NativeDispatcher machinery --

  public int read(FpgaDescriptor fd, int channel, long address, int len,
      long timeout) throws IOException {
    return read0(fd, channel, address, len, timeout);
  }

  public int write(FpgaDescriptor fd, int channel, long address, int len,
      int off, boolean last, long timeout) throws IOException {
    return write0(fd, channel, address, len, off, (last ? 1 : 0), timeout);
  }

  // Read from a Xilinx OpenCL device.
  public int read(FpgaDescriptor fd, int channel, long address)
      throws IOException {
    return readFromDevice(fd, channel, address);
  }

  // Write into a Xilinx OpenCL device.
  public int write(FpgaDescriptor fd, int channel, long address)
      throws IOException {
    return writeIntoDevice(fd, channel, address);
  }

  public void close(FpgaDescriptor fd, int flag) throws IOException {
    if (flag == 0) {
      close0(fd); // Close a Riffa device.
    } else {
      closeDevice(fd); // Close a Xilinx OpenCL device.
    }
  }

  // -- FpgaDispatcher machinery --

  public FpgaInfo list() {
    FpgaInfo info = new FpgaInfo();
    return listDevice(info) == 0 ? info : (list0(info) == 0 ? info : null);
  }

  public void reset(FpgaDescriptor fd) throws IOException {
    reset0(fd);
  }

  // -- Native machinery --

  static native int read0(FpgaDescriptor fd, int channel, long address, int len,
      long timeout);

  static native int write0(FpgaDescriptor fd, int channel, long address,
      int len, int off, int last, long timeout);

  static native long open0(int id);

  static native int list0(FpgaInfo info);

  static native void reset0(FpgaDescriptor fd);

  static native void close0(FpgaDescriptor fd);

  // -- Xilinx OpenCL API --

  static native void setDevice(int deviceId, ModuleConfig config);

  static native long openDevice(int deviceId, ModuleConfig config);

  static native int readFromDevice(FpgaDescriptor fd, int channel,
      long address);

  static native int writeIntoDevice(FpgaDescriptor fd, int channel,
      long address);

  static native int listDevice(FpgaInfo info);

  static native void closeDevice(FpgaDescriptor deviceId);

  static {
    IOUtils.load();
  }
}
