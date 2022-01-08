package bridge.blockchains.ethchains;

import bridge.common.ConfigObject;
import bridge.exceptions.AssetNotFoundException;
import com.electronwill.nightconfig.core.Config;
import com.electronwill.nightconfig.core.UnmodifiableConfig;

public final class BinanceSmartChain<K> extends EthBaseChain<K> {
  private static final UnmodifiableConfig configObject = ConfigObject.CONFIG;

  public BinanceSmartChain(String namedAsset) throws AssetNotFoundException {
    String innerNamedAsset;
    Config temp = configObject.get("Blockchain.BinanceSmartChain.asset");
    if (temp.contains(namedAsset)) {
      innerNamedAsset = namedAsset;
    } else {
      throw new AssetNotFoundException(
          String.format("Asset: %s, could not be found in the config file", namedAsset));
    }
    super.setAsset(configObject.get("Blockchain.BinanceSmartChain.asset" + "." + innerNamedAsset));
    super.setNetworkNode(configObject.get("Blockchain.BinanceSmartChain.node"));
    super.setNetwork(configObject.get("Blockchain.BinanceSmartChain.network"));
    super.setNetworkID(configObject.get("Blockchain.BinanceSmartChain.network_id"));
    super.setChainIdentifier(configObject.get("Blockchain.BinanceSmartChain.chain_identifier"));
    super.init();
  }
}
