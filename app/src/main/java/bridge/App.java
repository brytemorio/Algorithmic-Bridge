package bridge;

import static bridge.common.BridgeUtils.createChainName2AddressMap;

import bridge.blockchains.bitcoinchains.QnodecoinChainI;
import bridge.blockchains.ethchains.BinanceSmartChainI;
import bridge.blockchains.ethchains.PolygonChainI;
import bridge.blockchains.waves.WavesChainI;
import bridge.common.MongoStorageService;
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
    // producer.start();

    var db = new MongoStorageService();

    // db.setBlockHeightStorage(polygonChain.getChainIdentifier(), polygonChain.getBlockHeight());
    var fromAddress =
        createChainName2AddressMap(
            polygonChain.getAssetName("fishfactory_p"), polygonChain.getChainIdentifier());
    var toAddress =
        createChainName2AddressMap(
            qnodecoinChain.getAssetName(), binanceSmartChain.getChainIdentifier());
    // db.saveAddressMapping(fromAddress, toAddress);
    String address = db.getAddressFromSavedMapping(toAddress, "Fishfactory P");
    log.info(address);
  }
}
