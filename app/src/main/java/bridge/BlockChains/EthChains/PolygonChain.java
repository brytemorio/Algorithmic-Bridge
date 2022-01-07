package bridge.BlockChains.EthChains;

import bridge.common.ConfigObject;
import com.electronwill.nightconfig.core.UnmodifiableConfig;
import java.net.MalformedURLException;

public final class PolygonChain extends EthBaseChain {
  private static final UnmodifiableConfig configObject = ConfigObject.CONFIG;

  public PolygonChain() throws MalformedURLException {
    // super.setAsset(configObject.get("Blockchain.Polygon.asset_name"));
    super.setNetworkNode(configObject.get("Blockchain.Polygon.node"));
    super.setNetwork(configObject.get("Blockchain.Polygon.network"));
    super.setNetworkID(configObject.get("Blockchain.Polygon.network_id"));
    super.setChainIdentifier(configObject.get("Blockchain.Polygon.chain_identifier"));
    // super.init();
  }
}
