package bridge;

import static bridge.exceptions.Chain.*;

import bridge.blockchains.bitcoinchains.QnodecoinChainI;
import bridge.blockchains.ethchains.PolygonChainI;
import bridge.blockchains.waves.WavesChainI;
import bridge.common.TransactionModels.*;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class App {

  @SneakyThrows
  public static void main(String[] args) {
    QnodecoinChainI qnodecoinQ = new QnodecoinChainI();
    PolygonChainI<?> fishfactoryP = new PolygonChainI<>("fishfactory_p");
    WavesChainI<?> qnodecoinW = new WavesChainI<>("qnodecoin");
    NewBlockEventProducer producer = new NewBlockEventProducer();
    producer.start(qnodecoinW, fishfactoryP);
  }
}
