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
import java.util.ArrayList;
import java.util.List;

/**
 * Instances of the FPGA descriptor class serve as an opaque handle to the
 * underlying machine-specific structure representing an open FPGA.
 *
 * <p>
 * Applications should not create their own FPGA descriptors.
 */
public final class FpgaDescriptor {

  private long fd;

  private Closeable parent;
  private List<Closeable> otherParents;
  private boolean closed;

  /** Construct a FPGA descriptor with an invalid {@code fd}. */
  public FpgaDescriptor() {
    this(-1);
  }

  /**
   * Construct a FPGA descriptor with a given {@code fd}.
   *
   * @param fd the given FPGA descriptor
   */
  public FpgaDescriptor(long fd) {
    this.fd = fd;
  }

  public long getFD() {
    return fd;
  }

  public void setFD(long fd) {
    this.fd = fd;
  }

  /**
   * Test if this FPGA descriptor object is valid.
   *
   * @return {@code true} if the FPGA descriptor object represents a valid, open
   *         FPGA, {@code false} otherwise
   */
  public boolean valid() {
    return fd != -1;
  }

  /**
   * Attach a {@link Closeable} to this fd and handle close() on each one.
   *
   * @param c the Closeable to be attached
   */
  synchronized void attach(Closeable c) {
    if (parent == null) {
      // The first caller gets to do this
      parent = c;
    } else if (otherParents == null) {
      otherParents = new ArrayList<Closeable>();
      otherParents.add(parent);
      otherParents.add(c);
    } else {
      otherParents.add(c);
    }
  }

  /**
   * Cycle through all Closeables sharing this fd and handle close() on each
   * one.
   *
   * @param releaser the Closeable to be closed
   * @throws IOException if some I/O errors occur
   */
  @SuppressWarnings("try")
  synchronized void closeAll(Closeable releaser) throws IOException {
    if (!closed) {
      closed = true;
      IOException ioe = null;
      try (Closeable c = releaser) {
        if (otherParents != null) {
          for (Closeable referent : otherParents) {
            try {
              referent.close();
            } catch (IOException e) {
              if (ioe == null)
                ioe = e;
              else
                ioe.addSuppressed(e);
            }
          }
        }
      } catch (IOException e) {
        // If the releaser's close() throws IOException,
        // add other exceptions as suppressed.
        if (ioe != null)
          e.addSuppressed(ioe);
        ioe = e;
      } finally {
        if (ioe != null)
          throw ioe;
      }
    }
  }
}
