package bridge;

import bridge.blockchains.waveschains.WavesChainI;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class App {
  @SneakyThrows
  public static void main(String[] args) {
    // QnodecoinChainI qnodecoinChain = new QnodecoinChainI();
    // PolygonChainI<?> polygonChain = new PolygonChainI<>("fishfactory_p");
    WavesChainI<?> wavesChain = new WavesChainI<>("qnodecoin", "fishfactory_p");
    // BinanceSmartChainI<?> binanceSmartChain = new BinanceSmartChainI<>("qnode_defi");
    // NewBlockEventProducer producer =
    //  NewBlockEventProducer.getNewBlockEventProducer(
    //    qnodecoinChain, polygonChain, wavesChain, binanceSmartChain);
    // log.info("Starting Up Bridge...");
    // log.info(qnodecoinChain.getTransaction("test", null).toString());

    // System.out.println(wavesChain.getTrxByID("4zUvfUS7NWAquqh9yaKoVGg2ARteuVukkKJjorNKcQyQ"));

    // var result =
    //  BridgeUtils.getJsonDeserializer()
    //  .parse(wavesChain.getTrxByID("4zUvfUS7NWAquqh9yaKoVGg2ARteuVukkKJjorNKcQyQ"));

    // System.out.println(result.get("amount").toString());

    log.info(
        wavesChain
            .getTransaction("4zUvfUS7NWAquqh9yaKoVGg2ARteuVukkKJjorNKcQyQ", "qnodecoin")
            .toString());
  }
}
