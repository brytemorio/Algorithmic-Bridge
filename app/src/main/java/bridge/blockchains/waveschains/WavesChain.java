package bridge.blockchains.waveschains;

import bridge.blockchains.Asset;
import bridge.common.BridgeUtils;
import bridge.common.ConfigFileObj;
import bridge.exceptions.BridgeExceptions.AssetNotFoundException;
import bridge.services.storagservice.AssetStorageService;
import bridge.services.storagservice.ConfigurationStorageService;
import bridge.services.storagservice.DataObjects;
import com.electronwill.nightconfig.core.Config;
import com.electronwill.nightconfig.core.UnmodifiableConfig;
import lombok.extern.slf4j.Slf4j;
import org.agrona.collections.Object2ObjectHashMap;

import java.util.ArrayList;
import java.util.Objects;

@Slf4j
public final class WavesChain<K> extends WavesIBaseChain<K>
{

  private static final UnmodifiableConfig configObject = ConfigFileObj.CONFIG;


  public WavesChain(String... assetConfigName) throws AssetNotFoundException, RuntimeException
  {
    BridgeUtils.checkArgsLength(assetConfigName,
        "At least one token name should be passed to " + getClass().getSimpleName() + " " +
            "constructor");
    String[] assetConfigName1 = Objects.requireNonNull(assetConfigName); //NO need for this check
    ArrayList<Asset> assetList = new ArrayList<>();
    Config assets = configObject.get("Blockchain.Waves.assets");
    for (String configName : assetConfigName1)
    {
      if (!assets.contains(configName))
      {
        throw new AssetNotFoundException(
            String.format("Asset: %s, could not be found in the config file", configName));
      }
      assetList.add(new Asset(configObject.get("Blockchain.Waves.assets" + "." + configName)));
    }

    var wavesConfiguration = new DataObjects.ConfigurationStorage();
    wavesConfiguration.setChainName("waves");
    wavesConfiguration.setNode(configObject.get("Blockchain.Waves.node"));
    wavesConfiguration.setNetwork(configObject.get("Blockchain.Waves.network"));
    wavesConfiguration.setNetworkId(configObject.get("Blockchain.Waves.network_id"));
    wavesConfiguration.setChainIdentifier(configObject.get("Blockchain.Waves.chain_identifier"));
    wavesConfiguration.setGatewayAddress(configObject.get("Blockchain.Waves.gateway_address"));
    wavesConfiguration.setAssets(assetList);
    new ConfigurationStorageService().saveConfiguration(wavesConfiguration);

    super.init();
  }
}
