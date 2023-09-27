package bridge.services.storagservice;

import java.util.Map;

public class StorageServiceUtils
{
  private StorageServiceUtils(){};

  public static <K, V> K getKey(Map<K, V> hashMapObj)
  {
    K key = null;
    for (var iter : hashMapObj.keySet())
    {
      key = iter;
    }
    return key;
  }
}
