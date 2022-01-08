package bridge.utils;

import java.lang.reflect.Field;
import sun.misc.Unsafe;

public class Serializer<T> {
  private static final Unsafe unsafe;

  static {
    try {
      Field field = Unsafe.class.getDeclaredField("theUnsafe");
      field.setAccessible(true);
      unsafe = (Unsafe) field.get(null);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  private final T byteOffset = null;
}
