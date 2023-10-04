package bridge.blockchains.ethchains;

import bridge.blockchains.Asset;
import bridge.common.BridgeUtils;
import bridge.common.ConfigFileObj;
import bridge.exceptions.BridgeExceptions;
import bridge.services.storagservice.ConfigurationStorageService;
import bridge.services.storagservice.DataObjects;
import com.electronwill.nightconfig.core.Config;
import com.electronwill.nightconfig.core.UnmodifiableConfig;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;

@Slf4j
public final class AvaxChain<K> extends EthIBaseChain<K>
{
  private static final UnmodifiableConfig configObject = ConfigFileObj.CONFIG;

  public AvaxChain(String... assetNamesFromConfig) throws BridgeExceptions.AssetNotFoundException,
      RuntimeException
  {
    BridgeUtils.checkArgsLength(assetNamesFromConfig,
        "At least one token name should be passed to " + getClass().getSimpleName() + " " + "constructor");
    String[] assetNames = assetNamesFromConfig;
    Config assets = configObject.get("Blockchain.Avax.assets");
    ArrayList<Asset> assetList = new ArrayList<>();
    for (String assetNameI : assetNames)
    {
      if (!assets.contains(assetNameI))
      {
        throw new BridgeExceptions.AssetNotFoundException(
            String.format("Asset: %s, could not be found in the config file", assetNameI));
      }
      assetList.add(new Asset(configObject.get("Blockchain.Avax.assets" + "." + assetNameI)));
    }

    var avaxConfig = new DataObjects.ConfigurationStorage();
    avaxConfig.setChainIdentifier("avax");
    avaxConfig.setNode(configObject.get("Blockchain.Avax.node"));
    avaxConfig.setNetwork(configObject.get("Blockchain.Avax.network"));
    avaxConfig.setNetworkId(configObject.get("Blockchain.Avax.network_id"));
    avaxConfig.setChainIdentifier(configObject.get("Blockchain.Avax.chain_identifier"));
    avaxConfig.setGatewayAddress(configObject.get("Blockchain.Avax.gateway_address"));
    avaxConfig.setAssets(assetList);

    ConfigurationStorageService configurationStorageService =
        new ConfigurationStorageService();
    configurationStorageService.saveConfiguration(avaxConfig);
    super.setEthChainConfig(configurationStorageService.getConfiguration("avax"));
    super.init();
  }

}
