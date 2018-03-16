package tsinghua.stargate.io;

public final class IOStatus {

  public static final int EOF = -1;
  public static final int UNAVAILABLE = -2;
  public static final int INTERRUPTED = -3;
  public static final int UNSUPPORTED = -4;
  public static final int THROWN = -5;
  public static final int UNSUPPORTED_CASE = -6;

  private IOStatus() {
  }

  public static int normalize(int var0) {
    return var0 == -2 ? 0 : var0;
  }

  public static boolean check(int var0) {
    return var0 >= -2;
  }

  public static long normalize(long var0) {
    return var0 == -2L ? 0L : var0;
  }

  public static boolean check(long var0) {
    return var0 >= -2L;
  }

  public static boolean checkAll(long var0) {
    return var0 > -1L || var0 < -6L;
  }
}
