package bridge.blockchains.ethchains;

import bridge.common.ConfigObject;
import bridge.exceptions.AssetNotFoundException;
import com.electronwill.nightconfig.core.Config;
import com.electronwill.nightconfig.core.UnmodifiableConfig;

public final class PolygonChain<K> extends EthBaseChain<K> {
  private static final UnmodifiableConfig configObject = ConfigObject.CONFIG;

  public PolygonChain(String namedAsset) throws AssetNotFoundException {
    String innerNamedAsset;
    Config temp = configObject.get("Blockchain.Polygon.asset");
    if (temp.contains(namedAsset)) {
      innerNamedAsset = namedAsset;
    } else {
      throw new AssetNotFoundException(
          String.format("Asset: %s, could not be found in the config file", namedAsset));
    }
    super.setAsset(configObject.get("Blockchain.Polygon.asset" + "." + innerNamedAsset));
    super.setNetworkNode(configObject.get("Blockchain.Polygon.node"));
    super.setNetwork(configObject.get("Blockchain.Polygon.network"));
    super.setNetworkID(configObject.get("Blockchain.Polygon.network_id"));
    super.setChainIdentifier(configObject.get("Blockchain.Polygon.chain_identifier"));
    super.init();
  }
}
