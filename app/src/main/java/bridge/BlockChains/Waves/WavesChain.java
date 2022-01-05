package bridge.BlockChains.Waves;

import bridge.common.ConfigObject;
import com.electronwill.nightconfig.core.CommentedConfig;

public final class WavesChain extends WavesBaseChain {
  private static final CommentedConfig configObject = ConfigObject.CONFIG;

  public WavesChain() {
    super.setAssetName(configObject.get("Blockchain.Waves.asset_name"));
    super.setTickerSymbol(configObject.get("Blockchain.Waves.ticker_symbol"));
    super.setNode(configObject.get("Blockchain.Waves.node"));
    super.setNetwork(configObject.get("Blockchain.Waves.network"));
    super.setNetworkId(configObject.get("Blockchain.Waves.network_id"));
    super.setPrivateKey(configObject.get("Blockchain.Waves.private_key"));
    super.setPrivateKey(configObject.get("Blockchain.Waves.public_key"));
    super.setControlWallet(configObject.get("Blockchain.Waves.control_wallet"));
    super.setAssetId(configObject.get("Blockchain.Waves.asset_id"));
    super.setChainIdentifier(configObject.get("Blockchain.Waves.chain_identifier"));
    super.setTransferFee(configObject.get("Blockchain.Waves.transfer_fee"));
    super.init();
  }
}
