package bridge.blockchains.waves;

import static bridge.exceptions.Chain.*;

import bridge.common.ConfigObject;
import com.electronwill.nightconfig.core.Config;
import com.electronwill.nightconfig.core.UnmodifiableConfig;

public final class WavesChain<K> extends WavesBaseChain<K> {
  private static final UnmodifiableConfig configObject = ConfigObject.CONFIG;
  private String namedAsset;

  public WavesChain(String namedAsset) throws AssetNotFoundException {
    Config temp = configObject.get("Blockchain.Waves.asset");
    this.namedAsset = namedAsset;
    if (temp.contains(this.namedAsset)) {
      super.setAsset(configObject.get("Blockchain.Waves.asset" + "." + this.namedAsset));
    } else {
      throw new AssetNotFoundException(
          String.format("Asset: %s, could not be found in the config file", namedAsset));
    }

    super.setNetworkNode(configObject.get("Blockchain.Waves.node"));
    super.setNetwork(configObject.get("Blockchain.Waves.network"));
    super.setNetworkID(configObject.get("Blockchain.Waves.network_id"));
    super.setChainIdentifier(configObject.get("Blockchain.Waves.chain_identifier"));
    super.init();
  }
}
