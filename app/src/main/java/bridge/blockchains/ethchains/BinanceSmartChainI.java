package bridge.blockchains.ethchains;

import java.util.Objects;
import org.agrona.collections.Object2ObjectHashMap;
import com.electronwill.nightconfig.core.Config;
import com.electronwill.nightconfig.core.UnmodifiableConfig;
import bridge.common.ConfigFileObj;
import bridge.exceptions.BridgeExceptions.AssetNotFoundException;
import bridge.exceptions.BridgeExceptions.ObjectCreationException;

public final class BinanceSmartChainI<K> extends EthIBaseChain<K> {
  private static final UnmodifiableConfig configObject = ConfigFileObj.CONFIG;
  private String[] assetConfigName;

  public BinanceSmartChainI(String... assetConfigNames)
      throws AssetNotFoundException, ObjectCreationException {
    if (assetConfigNames.length == 0) {
      throw new ObjectCreationException(
          "Atleast one token name should be passed to "
              + getClass().getSimpleName()
              + " constructor");
    }

    this.assetConfigName = Objects.requireNonNull(assetConfigNames);

    Object2ObjectHashMap<String, Config> assets = new Object2ObjectHashMap<>();
    Config assetList = configObject.get("Blockchain.BinanceSmartChain.asset");
    for (var name : assetConfigName) {
      if (!assetList.contains(name)) {
        throw new AssetNotFoundException(
            String.format("Asset: %s, could not be found in the config file", name));
      }
      assets.put(name, configObject.get("Blockchain.BinanceSmartChain.asset" + "." + name));
    }
    super.setAsset(assets);
    super.setNetworkNode(configObject.get("Blockchain.BinanceSmartChain.node"));
    super.setNetwork(configObject.get("Blockchain.BinanceSmartChain.network"));
    super.setNetworkID(configObject.get("Blockchain.BinanceSmartChain.network_id"));
    super.setChainIdentifier(configObject.get("Blockchain.BinanceSmartChain.chain_identifier"));
    super.init();
  }
}
