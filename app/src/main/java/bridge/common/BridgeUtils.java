package bridge.common;

import java.util.Objects;
import lombok.SneakyThrows;
import org.agrona.collections.Object2ObjectHashMap;

public class BridgeUtils {
  private BridgeUtils() {}

  private static final Integer ZERO = 0;

  /**
   * For handling varargs parameters. Ensures at least one or more args is passed
   *
   * @param params - varags
   * @param errorMesg - The message given out on error. Null could be passed in as errorMessage in
   *     which case a default error message is used.
   */
  public static <T> void checkArgsLength(T[] params, String errorMesg) throws RuntimeException {
    String defaultErrorMsg = "Argument length is zero. One or more arguments are required";
    if (params.length == ZERO) {
      throw new RuntimeException(Objects.requireNonNullElse(errorMesg, defaultErrorMsg));
    }
  }

  @SneakyThrows
  public static Object2ObjectHashMap<String, IBaseChain> getIdentifier2ChainMapping(
      IBaseChain... chains) {
    Object2ObjectHashMap<String, IBaseChain> mapping = new Object2ObjectHashMap<>();
    checkArgsLength(chains, null);
    for (IBaseChain chain : chains) {
      mapping.put(chain.getChainIdentifier(), chain);
    }
    return mapping;
  }
}
