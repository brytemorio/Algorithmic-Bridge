package bridge.common;

import java.util.Collections;
import java.util.List;

public interface IChainQueryService {
  default <T> List<T> getTrxOfBlockAtHeight(int height) {
    return Collections.emptyList();
  }

  default String getTrxByID(String trxID) {
    return null;
  }

  default <T> T getTrxHash(int blockHeight) {
    return null;
  }

  default boolean validateAddress(String Address) {
    return false;
  }
}
