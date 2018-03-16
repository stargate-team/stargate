package tsinghua.stargate.io;

import java.nio.ByteBuffer;
import java.security.AccessController;
import java.security.PrivilegedAction;

import sun.nio.ch.DirectBuffer;

/**
 * Generic buffer utility methods for creating class instances.
 *
 * @see sun.nio.ch.DirectBuffer
 * @see java.nio.ByteBuffer
 * @see java.nio.DirectByteBuffer
 */
class BufferUtils {

  private static final int TEMP_BUF_POOL_SIZE;
  private static final long MAX_CACHED_BUFFER_SIZE;
  private static ThreadLocal<BufferUtils.BufferCache> bufferCache;

  private BufferUtils() {
  }

  private static long getMaxCachedBufferSize() {
    String var0 = AccessController.doPrivileged(new PrivilegedAction<String>() {
      public String run() {
        return System.getProperty("jdk.nio.maxCachedBufferSize");
      }
    });
    if (var0 != null) {
      try {
        long var1 = Long.parseLong(var0);
        if (var1 >= 0L) {
          return var1;
        }
      } catch (NumberFormatException var3) {
        var3.printStackTrace();
      }
    }

    return 9223372036854775807L;
  }

  private static boolean isBufferTooLarge(int var0) {
    return (long) var0 > MAX_CACHED_BUFFER_SIZE;
  }

  private static boolean isBufferTooLarge(ByteBuffer var0) {
    return isBufferTooLarge(var0.capacity());
  }

  /**
   * Get a temporary direct buffer from a pool, i.e. a <tt>BufferCache</tt>
   * instance.
   *
   * @param var0 the capacity of the buffer to be allocated
   * @return a new direct buffer instance
   */
  static ByteBuffer getTemporaryDirectBuffer(int var0) {
    if (isBufferTooLarge(var0)) {
      return ByteBuffer.allocateDirect(var0);
    } else {
      BufferUtils.BufferCache var1 = bufferCache.get();
      ByteBuffer var2 = var1.get(var0);
      if (var2 != null) {
        return var2;
      } else {
        if (!var1.isEmpty()) {
          var2 = var1.removeFirst();
          free(var2);
        }

        return ByteBuffer.allocateDirect(var0);
      }
    }
  }

  static void releaseTemporaryDirectBuffer(ByteBuffer var0) {
    offerFirstTemporaryDirectBuffer(var0);
  }

  static void offerFirstTemporaryDirectBuffer(ByteBuffer var0) {
    if (isBufferTooLarge(var0)) {
      free(var0);
    } else {
      assert var0 != null;

      BufferUtils.BufferCache var1 = bufferCache.get();
      if (!var1.offerFirst(var0)) {
        free(var0);
      }
    }
  }

  static void offerLastTemporaryDirectBuffer(ByteBuffer var0) {
    if (isBufferTooLarge(var0)) {
      free(var0);
    } else {
      assert var0 != null;

      BufferUtils.BufferCache var1 = bufferCache.get();
      if (!var1.offerLast(var0)) {
        free(var0);
      }

    }
  }

  private static void free(ByteBuffer var0) {
    ((DirectBuffer) var0).cleaner().clean();
  }

  static {
    TEMP_BUF_POOL_SIZE = IOUtils.IOV_MAX;
    MAX_CACHED_BUFFER_SIZE = getMaxCachedBufferSize();
    bufferCache = new ThreadLocal<BufferUtils.BufferCache>() {
      protected BufferUtils.BufferCache initialValue() {
        return new BufferUtils.BufferCache();
      }
    };
  }

  private static class BufferCache {
    private ByteBuffer[] buffers;
    private int count;
    private int start;

    private int next(int var1) {
      return (var1 + 1) % BufferUtils.TEMP_BUF_POOL_SIZE;
    }

    BufferCache() {
      this.buffers = new ByteBuffer[BufferUtils.TEMP_BUF_POOL_SIZE];
    }

    ByteBuffer get(int var1) {
      assert !BufferUtils.isBufferTooLarge(var1);

      if (this.count == 0) {
        return null;
      } else {
        ByteBuffer[] var2 = this.buffers;
        ByteBuffer var3 = var2[this.start];
        if (var3.capacity() < var1) {
          var3 = null;
          int var4 = this.start;

          while ((var4 = this.next(var4)) != this.start) {
            ByteBuffer var5 = var2[var4];
            if (var5 == null) {
              break;
            }

            if (var5.capacity() >= var1) {
              var3 = var5;
              break;
            }
          }

          if (var3 == null) {
            return null;
          }

          var2[var4] = var2[this.start];
        }

        var2[this.start] = null;
        this.start = this.next(this.start);
        --this.count;
        var3.rewind();
        var3.limit(var1);
        return var3;
      }
    }

    boolean offerFirst(ByteBuffer var1) {
      assert !BufferUtils.isBufferTooLarge(var1);

      if (this.count >= BufferUtils.TEMP_BUF_POOL_SIZE) {
        return false;
      } else {
        this.start = (this.start + BufferUtils.TEMP_BUF_POOL_SIZE - 1)
            % BufferUtils.TEMP_BUF_POOL_SIZE;
        this.buffers[this.start] = var1;
        ++this.count;
        return true;
      }
    }

    boolean offerLast(ByteBuffer var1) {
      assert !BufferUtils.isBufferTooLarge(var1);

      if (this.count >= BufferUtils.TEMP_BUF_POOL_SIZE) {
        return false;
      } else {
        int var2 = (this.start + this.count) % BufferUtils.TEMP_BUF_POOL_SIZE;
        this.buffers[var2] = var1;
        ++this.count;
        return true;
      }
    }

    boolean isEmpty() {
      return this.count == 0;
    }

    ByteBuffer removeFirst() {
      assert this.count > 0;

      ByteBuffer var1 = this.buffers[this.start];
      this.buffers[this.start] = null;
      this.start = this.next(this.start);
      --this.count;
      return var1;
    }
  }
}
