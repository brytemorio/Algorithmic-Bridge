package bridge.blockchains.ethchains;

import bridge.common.ConfigFileObj;
import bridge.exceptions.Chain.AssetNotFoundException;
import com.electronwill.nightconfig.core.Config;
import com.electronwill.nightconfig.core.UnmodifiableConfig;
import java.util.Objects;
import org.agrona.collections.Object2ObjectHashMap;

public final class PolygonChainI<K> extends EthIBaseChain<K> {
  private static final UnmodifiableConfig configObject = ConfigFileObj.CONFIG;
  private String[] namedAssets;

  public PolygonChainI(final String... namedAssets) throws AssetNotFoundException {
    this.namedAssets =
        Objects.requireNonNull(
            namedAssets,
            "Cannot construct object "
                + PolygonChainI.class.getName()
                + " without the required parameter(s)");
    // ArrayList<Config> assets = new ArrayList<>();
    Object2ObjectHashMap<String, Config> assets = new Object2ObjectHashMap<>();
    Config assetList = configObject.get("Blockchain.Polygon.asset");
    for (String name : this.namedAssets) {
      if (!assetList.contains(name)) {
        throw new AssetNotFoundException(
            String.format("Asset: %s, could not be found in the config file", name));
      }

      assets.put(name, configObject.get("Blockchain.Polygon.asset" + "." + name));
      // assets.add(configObject.get("Blockchain.Polygon.asset" + "." + name));
    }

    super.setAsset(assets);
    super.setNetworkNode(configObject.get("Blockchain.Polygon.node"));
    super.setNetwork(configObject.get("Blockchain.Polygon.network"));
    super.setNetworkID(configObject.get("Blockchain.Polygon.network_id"));
    super.setChainIdentifier(configObject.get("Blockchain.Polygon.chain_identifier"));
    super.init();
  }
}
