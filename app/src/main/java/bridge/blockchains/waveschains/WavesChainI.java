package bridge.blockchains.waveschains;

import bridge.blockchains.Asset;
import bridge.common.BridgeUtils;
import bridge.common.ConfigFileObj;
import bridge.exceptions.BridgeExceptions.AssetNotFoundException;
import bridge.services.storagservice.AssetStorageService;
import bridge.services.storagservice.DataObjects;
import com.electronwill.nightconfig.core.Config;
import com.electronwill.nightconfig.core.UnmodifiableConfig;
import org.agrona.collections.Object2ObjectHashMap;

import java.util.ArrayList;
import java.util.Objects;

public final class WavesChainI<K> extends WavesIBaseChain<K>
{

  private static final UnmodifiableConfig configObject = ConfigFileObj.CONFIG;


  public WavesChainI(String... assetConfigName) throws AssetNotFoundException, RuntimeException
  {
    BridgeUtils.checkArgsLength(assetConfigName,
        "Atleast one token name should be passed to " + getClass().getSimpleName() + " constructor");
    String[] assetConfigName1 = Objects.requireNonNull(assetConfigName); //NO need for this check
    Object2ObjectHashMap<String, ArrayList<Asset>> wavesAssets = new Object2ObjectHashMap<>();
    ArrayList<Asset> assetList = new ArrayList<>();
    Config assets = configObject.get("Blockchain.Waves.assets");
    for (String configName : assetConfigName1)
    {
      if (!assets.contains(configName))
      {
        throw new AssetNotFoundException(
            String.format("Asset: %s, could not be found in the config file", configName));
      }
      assetList.add(new Asset(configObject.get("Blockchain.Waves.asset" + "." + configName)));
    }
    wavesAssets.put("waves", assetList);
    new AssetStorageService().saveAssetsToStorage(new DataObjects.AssetStorage(wavesAssets));

    super.setNetworkNode(configObject.get("Blockchain.Waves.node"));
    super.setWavesBridgeAddress(configObject.get("Blockchain.Waves.gateway_address"));
    super.setNetwork(configObject.get("Blockchain.Waves.network"));
    super.setNetworkID(configObject.get("Blockchain.Waves.network_id"));
    super.setChainIdentifier(configObject.get("Blockchain.Waves.chain_identifier"));
    super.init();
  }
}
