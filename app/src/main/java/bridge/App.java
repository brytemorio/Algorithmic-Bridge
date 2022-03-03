package bridge;

import bridge.blockchains.bitcoinchains.QnodecoinChainI;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class App {
  @SneakyThrows
  public static void main(String[] args) {
    QnodecoinChainI qnodecoinChain = new QnodecoinChainI();
    // PolygonChainI<?> polygonChain = new PolygonChainI<>("fishfactory_p");
    // WavesChainI<?> wavesChain = new WavesChainI<>("qnodecoin", "fishfactory_p");
    // BinanceSmartChainI<?> binanceSmartChain = new BinanceSmartChainI<>("qnode_defi");
    // NewBlockEventProducer producer =
    //  NewBlockEventProducer.getNewBlockEventProducer(
    //    qnodecoinChain, polygonChain, wavesChain, binanceSmartChain);
    log.info("Starting Up Bridge...");
    log.info(qnodecoinChain.getTransaction("test", null).toString());
  }
}
