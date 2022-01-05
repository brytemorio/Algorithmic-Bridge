package bridge.BlockChains.EthChains;

import bridge.common.ConfigObject;
import com.electronwill.nightconfig.core.CommentedConfig;

public final class BinanceSmartChain extends EthBaseChain {
  private static final CommentedConfig configObject = ConfigObject.CONFIG;

  public BinanceSmartChain() {
    super.setAssetName(configObject.get("Blockchain.BSC.asset_name"));
    super.setTickerSymbol(configObject.get("Blockchain.BSC.ticker_symbol"));
    super.setNetworkNode(configObject.get("Blockchain.BSC.node"));
    super.setNetwork(configObject.get("Blockchain.BSC.network"));
    super.setNetworkID(configObject.get("Blockchain.BSC.network_id"));
    super.setControlWallet(configObject.get("Blockchain.BSC.control_wallet"));
    super.setPrivateKey(configObject.get("Blockchain.BSC.private_key"));
    super.setAssetId(configObject.get("Blockchain.BSC.asset_id"));
    super.setChainIdentifier(configObject.get("Blockchain.BSC.chain_identifier"));
    super.setTransferFee(configObject.get("Blockchain.BSC.transfer_fee"));
    super.init();
  }
}
