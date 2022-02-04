package bridge.common;

import bridge.blockchains.IBaseChain;
import com.electronwill.nightconfig.core.Config;
import com.electronwill.nightconfig.core.ConfigFormat;
import com.electronwill.nightconfig.core.conversion.ObjectConverter;
import com.electronwill.nightconfig.core.io.ConfigParser;
import com.electronwill.nightconfig.json.JsonFormat;
import com.lmax.disruptor.BlockingWaitStrategy;
import com.lmax.disruptor.WaitStrategy;
import com.lmax.disruptor.YieldingWaitStrategy;
import java.io.InputStream;
import java.net.URL;
import java.util.Objects;
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

  public static Object2ObjectHashMap<String, IBaseChain> getIdentifier2ChainMapping(
      IBaseChain... chains) {
    Object2ObjectHashMap<String, IBaseChain> mapping = new Object2ObjectHashMap<>();
    checkArgsLength(chains, null);
    for (IBaseChain chain : chains) {
      mapping.put(chain.getChainIdentifier(), chain);
    }
    return mapping;
  }

  public static Config object2JsonConverter(Object object) {
    ObjectConverter objectConverter = new ObjectConverter();
    return objectConverter.toConfig(object, Config::inMemory);
  }

  public static ConfigParser<Config> getJsonDeserializer() {
    ConfigFormat<Config> jsonFormat = JsonFormat.fancyInstance();
    ConfigParser<Config> jsonParser = jsonFormat.createParser();
    return jsonParser;
  }

  public static InputStream readResourceAsStream(String filename) throws NullPointerException {
    InputStream configfileStream =
        ConfigFileObj.class.getClassLoader().getResourceAsStream(filename);
    if (configfileStream == null) throw new NullPointerException("File Not found");
    return configfileStream;
  }

  public static URL readResource(String filename) throws NullPointerException {
    URL configfileStream = ConfigFileObj.class.getClassLoader().getResource(filename);
    if (configfileStream == null) throw new NullPointerException("File Not found");
    return configfileStream;
  }

  // Assumptions: Total blockchain objects
  public static <T> WaitStrategy determineWaitStrategy(T[] eventHandlerThreads) {
    int processorCount = Runtime.getRuntime().availableProcessors();
    return (processorCount > eventHandlerThreads.length)
        ? new YieldingWaitStrategy()
        : new BlockingWaitStrategy();
  }
}
