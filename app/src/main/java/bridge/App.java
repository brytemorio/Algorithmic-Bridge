package bridge;

import java.util.ArrayList;
import java.util.Arrays;

import bridge.blockchains.waveschains.WavesChainI;
import bridge.common.BridgeUtils;
import bridge.messageservice.NewBlockEventProducer;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class App
{
  @SneakyThrows
  public static void main(String[] args)
  {
    // QnodecoinChainI qnodecoinChain = new QnodecoinChainI();
    // PolygonChainI<?> polygonChain = new PolygonChainI<>("fishfactory_p");
    WavesChainI<?> wavesChain = new WavesChainI<>("qnodecoin", "fishfactory_p");
    // BinanceSmartChainI<?> binanceSmartChain = new BinanceSmartChainI<>("qnode_defi");
    NewBlockEventProducer producer = NewBlockEventProducer.getNewBlockEventProducer(wavesChain);
    log.info("Starting Up Bridge...");
    producer.start();

    // System.out.println(wavesChain.getTrxByID("4zUvfUS7NWAquqh9yaKoVGg2ARteuVukkKJjorNKcQyQ"));

    // var result =
    //  BridgeUtils.getJsonDeserializer()
    //  .parse(wavesChain.getTrxByID("4zUvfUS7NWAquqh9yaKoVGg2ARteuVukkKJjorNKcQyQ"));

    // System.out.println(result.get("amount").toString());

    ArrayList<Integer> testArray = new ArrayList<Integer>();
    testArray.addAll(Arrays.asList(1, 2, 3, 5, 6, null, 20, null, null, 59, 69));
    System.out.println(BridgeUtils.filterNulls(testArray).toString());
  }
}
