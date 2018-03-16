package tsinghua.stargate.io;

class NativeThreadSet {

  private long[] elts;
  private int used = 0;
  private boolean waitingToEmpty;

  NativeThreadSet(int var1) {
    this.elts = new long[var1];
  }

  int add() {
    long var1 = NativeThread.current();
    if (var1 == 0L) {
      var1 = -1L;
    }

    synchronized (this) {
      int var4 = 0;
      int var5;
      if (this.used >= this.elts.length) {
        var5 = this.elts.length;
        int var6 = var5 * 2;
        long[] var7 = new long[var6];
        System.arraycopy(this.elts, 0, var7, 0, var5);
        this.elts = var7;
        var4 = var5;
      }

      for (var5 = var4; var5 < this.elts.length; ++var5) {
        if (this.elts[var5] == 0L) {
          this.elts[var5] = var1;
          ++this.used;
          return var5;
        }
      }

      assert false;

      return -1;
    }
  }

  void remove(int var1) {
    synchronized (this) {
      this.elts[var1] = 0L;
      --this.used;
      if (this.used == 0 && this.waitingToEmpty) {
        this.notifyAll();
      }
    }
  }

  synchronized void signalAndWait() {
    boolean var1 = false;

    while (this.used > 0) {
      int var2 = this.used;
      int var3 = this.elts.length;

      for (int var4 = 0; var4 < var3; ++var4) {
        long var5 = this.elts[var4];
        if (var5 != 0L) {
          if (var5 != -1L) {
            NativeThread.signal(var5);
          }

          --var2;
          if (var2 == 0) {
            break;
          }
        }
      }

      this.waitingToEmpty = true;

      try {
        this.wait(50L);
      } catch (InterruptedException var10) {
        var1 = true;
      } finally {
        this.waitingToEmpty = false;
      }
    }

    if (var1) {
      Thread.currentThread().interrupt();
    }
  }
}
