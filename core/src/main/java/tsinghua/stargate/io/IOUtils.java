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
import java.security.AccessController;
import java.security.PrivilegedAction;

import sun.nio.ch.DirectBuffer;

/** Generic io utility methods for creating class instances. */
class IOUtils {

  static final int IOV_MAX;

  private IOUtils() {
  }

  // Reading from a Riffa device.
  static int read(FpgaDescriptor fd, ByteBuffer buf, NativeDispatcher nd,
      int core, long timeout) throws IOException {
    if (buf.isReadOnly()) {
      throw new IllegalArgumentException("Read-only buffer");
    } else if (buf instanceof DirectBuffer) {
      return readIntoNativeBuffer(fd, buf, nd, core, timeout);
    } else {
      ByteBuffer directBuf =
          BufferUtils.getTemporaryDirectBuffer(buf.remaining());

      int size;
      try {
        int tmpSize = readIntoNativeBuffer(fd, directBuf, nd, core, timeout);
        directBuf.flip();
        if (tmpSize > 0) {
          buf.put(directBuf);
        }

        size = tmpSize;
      } finally {
        BufferUtils.offerFirstTemporaryDirectBuffer(directBuf);
      }

      return size;
    }
  }

  private static int readIntoNativeBuffer(FpgaDescriptor fd, ByteBuffer buf,
      NativeDispatcher nd, int core, long timeout) throws IOException {
    int position = buf.position();
    int limit = buf.limit();

    assert position <= limit;

    int remaining = buf.remaining();
    if (remaining == 0) {
      return 0;
    } else {
      int size = nd.read(fd, core,
          ((DirectBuffer) buf).address() + (long) position, remaining, timeout);
      if (size > 0) {
        buf.position(position + size);
      }
      return size;
    }
  }

  // Writing into a Riffa device.
  static int write(FpgaDescriptor fd, ByteBuffer buf, NativeDispatcher nd,
      int core, int off, boolean last, long timeout) throws IOException {
    if (buf instanceof DirectBuffer) {
      return writeFromNativeBuffer(fd, buf, nd, core, off, last, timeout);
    } else {
      int position = buf.position();
      int limit = buf.limit();

      assert position <= limit;

      int remaining = buf.remaining();
      ByteBuffer directBuf = BufferUtils.getTemporaryDirectBuffer(remaining);

      int size;
      try {
        directBuf.put(buf);
        directBuf.flip();
        buf.position(position);
        int tmpSize =
            writeFromNativeBuffer(fd, directBuf, nd, core, off, last, timeout);
        if (tmpSize > 0) {
          buf.position(position + tmpSize);
        }
        size = tmpSize;
      } finally {
        BufferUtils.offerFirstTemporaryDirectBuffer(directBuf);
      }

      return size;
    }
  }

  private static int writeFromNativeBuffer(FpgaDescriptor fd, ByteBuffer buf,
      NativeDispatcher nd, int core, int off, boolean last, long timeout)
      throws IOException {
    int position = buf.position();
    int limit = buf.limit();

    assert position <= limit;

    int remaining = buf.remaining();
    if (remaining == 0) {
      return 0;
    } else {
      int size =
          nd.write(fd, core, ((DirectBuffer) buf).address() + (long) position,
              remaining, off, last, timeout);
      if (size > 0) {
        buf.position(position + size);
      }
      return size;
    }
  }

  // Reading from a Xinlinx OpenCL device.
  static int read(FpgaDescriptor fd, ByteBuffer buf, NativeDispatcher nd,
      int core) throws IOException {
    if (buf.isReadOnly()) {
      throw new IllegalArgumentException("Read-only buffer");
    } else if (buf instanceof DirectBuffer) {
      return readIntoNativeBuffer(fd, buf, nd, core);
    } else {
      ByteBuffer directBuf =
          BufferUtils.getTemporaryDirectBuffer(buf.remaining());

      int size;
      try {
        int tmpSize = readIntoNativeBuffer(fd, directBuf, nd, core);
        directBuf.flip();
        if (tmpSize > 0) {
          buf.put(directBuf);
        }

        size = tmpSize;
      } finally {
        BufferUtils.offerFirstTemporaryDirectBuffer(directBuf);
      }

      return size;
    }
  }

  private static int readIntoNativeBuffer(FpgaDescriptor fd, ByteBuffer buf,
      NativeDispatcher nd, int core) throws IOException {
    int position = buf.position();
    int limit = buf.limit();

    assert position <= limit;

    int remaining = buf.remaining();
    if (remaining == 0) {
      return 0;
    } else {
      int size = nd.read(fd, core, ((DirectBuffer) buf).address());
      if (size > 0) {
        buf.position(position + size);
      }
      return size;
    }
  }

  // Writing into a Xinlinx OpenCL device.
  static int write(FpgaDescriptor fd, ByteBuffer buf, NativeDispatcher nd,
      int core) throws IOException {
    if (buf instanceof DirectBuffer) {
      return writeFromNativeBuffer(fd, buf, nd, core);
    } else {
      int position = buf.position();
      int limit = buf.limit();

      assert position <= limit;

      int remaining = buf.remaining();
      ByteBuffer directBuf = BufferUtils.getTemporaryDirectBuffer(remaining);

      int size;
      try {
        directBuf.put(buf);
        directBuf.flip();
        buf.position(position);
        int tmpSize = writeFromNativeBuffer(fd, directBuf, nd, core);
        if (tmpSize > 0) {
          buf.position(position + tmpSize);
        }
        size = tmpSize;
      } finally {
        BufferUtils.offerFirstTemporaryDirectBuffer(directBuf);
      }

      return size;
    }
  }

  private static int writeFromNativeBuffer(FpgaDescriptor fd, ByteBuffer buf,
      NativeDispatcher nd, int core) throws IOException {
    int position = buf.position();
    int limit = buf.limit();

    assert position <= limit;

    int remaining = buf.remaining();
    if (remaining == 0) {
      return 0;
    } else {
      int size =
          nd.write(fd, core, ((DirectBuffer) buf).address() + (long) position);
      if (size > 0) {
        buf.position(position + size);
      }
      return size;
    }
  }

  static FpgaDescriptor newFD(long fdVal) {
    FpgaDescriptor fd = new FpgaDescriptor();
    setfdVal(fd, fdVal);
    return fd;
  }

  static Fpga.FpgaInfo list(FpgaDispatcher nd) throws IOException {
    return nd.list();
  }

  static void reset(FpgaDescriptor fd, FpgaDispatcher nd) throws IOException {
    nd.reset(fd);
  }

  static native long fdVal(FpgaDescriptor fd);

  static native void setfdVal(FpgaDescriptor fd, long val);

  static native int iovMax();

  static native void initIDs();

  static void load() {
  }

  static {
    AccessController.doPrivileged(new PrivilegedAction<Void>() {
      public Void run() {
//        System.loadLibrary("pci"); // Load Riffa library
         System.loadLibrary("io"); // Load Xilinx OpenCL library
        return null;
      }
    });
    initIDs();
    IOV_MAX = iovMax();
  }
}
