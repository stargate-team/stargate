package tsinghua.stargate.io;

public class NativeThread {

  public NativeThread() {
  }

  public static native long current();

  public static native void signal(long var0);

  private static native void init();

  static {
    IOUtils.load();
    init();
  }
}
