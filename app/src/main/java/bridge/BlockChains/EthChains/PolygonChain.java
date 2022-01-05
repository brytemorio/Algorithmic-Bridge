package bridge.BlockChains.EthChains;

import bridge.common.ConfigObject;
import com.electronwill.nightconfig.core.CommentedConfig;
import java.net.MalformedURLException;

public final class PolygonChain extends EthBaseChain {
  private static final CommentedConfig configObject = ConfigObject.CONFIG;

  public PolygonChain() throws MalformedURLException {
    super.setAssetName(configObject.get("Blockchain.Polygon.asset_name"));
    super.setTickerSymbol(configObject.get("Blockchain.Polygon.ticker_symbol"));
    super.setNetworkNode(configObject.get("Blockchain.Polygon.node"));
    super.setNetwork(configObject.get("Blockchain.Polygon.network"));
    super.setNetworkID(configObject.get("Blockchain.Polygon.network_id"));
    super.setControlWallet(configObject.get("Blockchain.Polygon.control_wallet"));
    super.setPrivateKey(configObject.get("Blockchain.Polygon.private_key"));
    super.setAssetId(configObject.get("Blockchain.Polygon.asset_id"));
    super.setChainIdentifier(configObject.get("Blockchain.Polygon.chain_identifier"));
    super.setTransferFee(configObject.get("Blockchain.Polygon.transfer_fee"));
    super.init();
  }
}
