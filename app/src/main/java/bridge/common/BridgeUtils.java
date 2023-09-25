package bridge.common;

import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.agrona.collections.Object2ObjectHashMap;
import com.electronwill.nightconfig.core.Config;
import com.electronwill.nightconfig.core.ConfigFormat;
import com.electronwill.nightconfig.core.conversion.ObjectConverter;
import com.electronwill.nightconfig.core.io.ConfigParser;
import com.electronwill.nightconfig.json.JsonFormat;
import com.lmax.disruptor.BlockingWaitStrategy;
import com.lmax.disruptor.WaitStrategy;
import com.lmax.disruptor.YieldingWaitStrategy;
import bridge.blockchains.IBaseChain;
import bridge.services.transactionservice.TransactionModels.MappedAddress;

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
    return jsonFormat.createParser();
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

  // Assumption: HyperThreading is enabled on the machine
  public static <T> WaitStrategy determineWaitStrategy(T[] eventHandlerThreads) {
    int processorCount = Runtime.getRuntime().availableProcessors();
    return (processorCount > eventHandlerThreads.length)
        ? new YieldingWaitStrategy()
        : new BlockingWaitStrategy();
  }

  public static Object2ObjectHashMap<String, MappedAddress> createChainName2AddressMap(
      String blockChainName, MappedAddress Address) {
    Object2ObjectHashMap<String, MappedAddress> mapping = new Object2ObjectHashMap<>();
    mapping.put(blockChainName, Address);
    return mapping;
  }

  // parameter dp => Decimal Place
  public static double roundUp(double val, int dp) {
    double multiplier = Math.pow(10, dp);
    return Math.ceil(val * multiplier) / multiplier;
  }

  public static <T> List<T> filterNulls(List<T> inputArrayList) {

    List<T> nonNullList = new ArrayList<>();

    inputArrayList.parallelStream()
        .forEach(
            value -> {
              if (value != null) nonNullList.add(value);
            });
    return nonNullList;
  }

  public static void countDownTimer(int time)
  {
    ScheduledExecutorService executorService = Executors.newScheduledThreadPool(1);
    final int[] countDowmTime = {time};

    // Use of array as a workaround for  Java's lambada finality
    // restriction;
    Runnable countDownTask = () -> {
      if(countDowmTime[0] > 0)
        countDowmTime[0] --;
      else
        executorService.shutdown();
    };

    //ScheduledFuture<?> coundownFuture =
    executorService.scheduleAtFixedRate(countDownTask, 0, 1,
        TimeUnit.SECONDS);
  }
}
