package bridge;

import bridge.blockchains.bitcoinchains.QnodecoinChainI;
import bridge.blockchains.ethchains.BinanceSmartChainI;
import bridge.blockchains.ethchains.PolygonChainI;
import bridge.blockchains.waves.WavesChainI;
import bridge.messageservice.NewBlockEventProducer;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class App {
  @SneakyThrows
  public static void main(String[] args) {
    QnodecoinChainI qnodecoinChain = new QnodecoinChainI();
    PolygonChainI<?> polygonChain = new PolygonChainI<>("fishfactory_p");
    WavesChainI<?> wavesChain = new WavesChainI<>("qnodecoin", "fishfactory_p");
    BinanceSmartChainI<?> binanceSmartChain = new BinanceSmartChainI<>("qnode_defi");
    NewBlockEventProducer producer =
        NewBlockEventProducer.getNewBlockEventProducer(
            qnodecoinChain, polygonChain, wavesChain, binanceSmartChain);
    log.info("Starting Up Bridge...");
    // producer.start();

    System.out.println(
        qnodecoinChain.getTrxReceivers(
            qnodecoinChain.getTrxByID(
                "c529dee97ac7836fedaa0241464a1c7ee1eb9880f9c33214e1d193f599f6b8d4")));

    for (; ; ) {
      System.out.println(
          qnodecoinChain.getTrxSenders(
              qnodecoinChain.getTrxByID(
                  "c529dee97ac7836fedaa0241464a1c7ee1eb9880f9c33214e1d193f599f6b8d4")));
    }
  }
}
