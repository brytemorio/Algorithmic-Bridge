package bridge.blockchains.ethchains;

import java.util.Objects;
import org.agrona.collections.Object2ObjectHashMap;
import com.electronwill.nightconfig.core.Config;
import com.electronwill.nightconfig.core.UnmodifiableConfig;
import bridge.common.AssetABI;
import bridge.common.ConfigFileObj;
import bridge.exceptions.BridgeExceptions.AssetNotFoundException;
import bridge.exceptions.BridgeExceptions.ObjectCreationException;

public final class BinanceSmartChainI<K> extends EthIBaseChain<K> {
  private static final UnmodifiableConfig configObject = ConfigFileObj.CONFIG;
  private AssetABI[] assetContract;

  public BinanceSmartChainI(AssetABI... assetContracts)
      throws AssetNotFoundException, ObjectCreationException {
    if (assetContracts.length == 0) {
      throw new ObjectCreationException(
          "Atleast one token name should be passed to "
              + getClass().getSimpleName()
              + " constructor");
    }

    this.assetContract = Objects.requireNonNull(assetContracts);

    Object2ObjectHashMap<String, Config> assets = new Object2ObjectHashMap<>();
    Object2ObjectHashMap<String, AssetABI> assetABI = new Object2ObjectHashMap<>();
    Config assetList = configObject.get("Blockchain.BinanceSmartChain.asset");
    for (var assetABIContract : this.assetContract) {
      String assetName = assetABIContract.getAssetName();
      if (!assetList.contains(assetName)) {
        throw new AssetNotFoundException(
            String.format("Asset: %s, could not be found in the config file", assetName));
      }
      assets.put(
          assetName, configObject.get("Blockchain.BinanceSmartChain.asset" + "." + assetName));
      assetABI.put(assetName, assetABIContract);
    }
    super.setAssetABI(assetABI);
    super.setAsset(assets);
    super.setNetworkNode(configObject.get("Blockchain.BinanceSmartChain.node"));
    super.setNetwork(configObject.get("Blockchain.BinanceSmartChain.network"));
    super.setNetworkID(configObject.get("Blockchain.BinanceSmartChain.network_id"));
    super.setChainIdentifier(configObject.get("Blockchain.BinanceSmartChain.chain_identifier"));
    super.init();
  }
}
