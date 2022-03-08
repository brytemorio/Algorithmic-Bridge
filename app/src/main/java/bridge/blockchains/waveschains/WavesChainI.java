package bridge.blockchains.waveschains;

import bridge.common.BridgeUtils;
import bridge.common.ConfigFileObj;
import bridge.exceptions.BridgeExceptions.AssetNotFoundException;
import com.electronwill.nightconfig.core.Config;
import com.electronwill.nightconfig.core.UnmodifiableConfig;
import java.util.Objects;
import org.agrona.collections.Object2ObjectHashMap;

public final class WavesChainI<K> extends WavesIBaseChain<K> {

  private static final UnmodifiableConfig configObject = ConfigFileObj.CONFIG;

  private String[] assetConfigName;

  public WavesChainI(String... assetConfigName) throws AssetNotFoundException, RuntimeException {
    BridgeUtils.checkArgsLength(
        assetConfigName,
        "Atleast one token name should be passed to "
            + getClass().getSimpleName()
            + " constructor");
    this.assetConfigName = Objects.requireNonNull(assetConfigName);
    Object2ObjectHashMap<String, Config> assets = new Object2ObjectHashMap<>();
    Config assetList = configObject.get("Blockchain.Waves.asset");
    for (String configName : this.assetConfigName) {
      if (!assetList.contains(configName)) {
        throw new AssetNotFoundException(
            String.format("Asset: %s, could not be found in the config file", configName));
      }
      assets.put(configName, configObject.get("Blockchain.Waves.asset" + "." + configName));
    }
    super.setAsset(assets);
    super.setNetworkNode(configObject.get("Blockchain.Waves.node"));
    super.setNetwork(configObject.get("Blockchain.Waves.network"));
    super.setNetworkID(configObject.get("Blockchain.Waves.network_id"));
    super.setChainIdentifier(configObject.get("Blockchain.Waves.chain_identifier"));
    super.init();
  }
}
