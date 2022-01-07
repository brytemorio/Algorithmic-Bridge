package bridge.BlockChains.EthChains;

import bridge.common.ConfigObject;
import com.electronwill.nightconfig.core.UnmodifiableConfig;

public final class BinanceSmartChain<T> extends EthBaseChain<T> {
  private static final UnmodifiableConfig configObject = ConfigObject.CONFIG;
  private String assetName;

  public BinanceSmartChain(String assetName) {
    this.assetName = assetName;
    String assetConfigName = configObject.get("Blockchain.BinanceSmartChain.asset." + assetName);

    super.setAsset(configObject.get("Blockchain.BinanceSmartChain.asset"));
    super.setNetworkNode(configObject.get("Blockchain.BSC.node"));
    super.setNetwork(configObject.get("Blockchain.BSC.network"));
    super.setNetworkID(configObject.get("Blockchain.BSC.network_id"));
    super.setChainIdentifier(configObject.get("Blockchain.BSC.chain_identifier"));
    // super.init((super.getAsset());
  }

  /*
  private Map<String, Map<String, String>> getAssetParams(){
    Config assetParamsFromFile = super.getAsset();
    //Map<String,  Map<String, String>> assetParams = getAssetParams().get("te");
    return assetParam
  }
   */
}
