package bridge.blockchains.ethchains;

import bridge.common.ConfigFileObj;
import bridge.exceptions.BridgeExceptions.AssetNotFoundException;
import bridge.exceptions.BridgeExceptions.ObjectCreationException;
import com.electronwill.nightconfig.core.Config;
import com.electronwill.nightconfig.core.UnmodifiableConfig;
import java.util.Objects;
import org.agrona.collections.Object2ObjectHashMap;

public final class PolygonChainI<K> extends EthIBaseChain<K> {

  private static final UnmodifiableConfig configObject = ConfigFileObj.CONFIG;
  private String[] assetConfigName;

  public PolygonChainI(final String... assetConfigName)
      throws AssetNotFoundException, ObjectCreationException {
    if (assetConfigName.length == 0) {
      throw new ObjectCreationException(
          "Atleast one token name should be passed to "
              + getClass().getSimpleName()
              + " constructor");
    }
    this.assetConfigName = Objects.requireNonNull(assetConfigName);

    Object2ObjectHashMap<String, Config> assets = new Object2ObjectHashMap<>();
    Config assetList = configObject.get("Blockchain.Polygon.asset");
    for (String configName : this.assetConfigName) {
      if (!assetList.contains(configName)) {
        throw new AssetNotFoundException(
            String.format("Asset: %s, could not be found in the config file", configName));
      }

      assets.put(configName, configObject.get("Blockchain.Polygon.asset" + "." + configName));
    }

    super.setAsset(assets);
    super.setNetworkNode(configObject.get("Blockchain.Polygon.node"));
    super.setNetwork(configObject.get("Blockchain.Polygon.network"));
    super.setNetworkID(configObject.get("Blockchain.Polygon.network_id"));
    super.setChainIdentifier(configObject.get("Blockchain.Polygon.chain_identifier"));
    super.init();
  }
}
