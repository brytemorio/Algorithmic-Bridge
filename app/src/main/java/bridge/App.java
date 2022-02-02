package bridge;

import bridge.blockchains.bitcoinchains.QnodecoinChainI;
import bridge.blockchains.ethchains.BinanceSmartChainI;
import bridge.blockchains.ethchains.PolygonChainI;
import bridge.blockchains.waves.WavesChainI;
import bridge.messageservice.NewBlockEventProducer;
import com.wavesplatform.wavesj.Node;
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
        NewBlockEventProducer.getNewBlockEventProducer(qnodecoinChain, polygonChain, wavesChain);
    // producer.start();

    // System.out.println(qnodecoinChain.getTrxOfBlockAtHeight(BigInteger.valueOf(1000)));

    Node test = wavesChain.getNodeObj();

    // System.out.println(object2JsonConverter(test.getBlock(23232).transactions()));
  }
}
