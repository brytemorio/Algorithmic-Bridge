package bridge.blockchains.waves;

import bridge.blockchains.ethchains.PolygonChainI;
import bridge.common.ConfigFileObj;
import bridge.exceptions.Chain.AssetNotFoundException;
import com.electronwill.nightconfig.core.Config;
import com.electronwill.nightconfig.core.UnmodifiableConfig;
import java.util.Objects;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.agrona.collections.Object2ObjectHashMap;

@Data
public final class WavesChainI<K> extends WavesIBaseChain<K> {
  @Getter(AccessLevel.NONE)
  @Setter(AccessLevel.NONE)
  private static final UnmodifiableConfig configObject = ConfigFileObj.CONFIG;

  @Getter(AccessLevel.NONE)
  @Setter(AccessLevel.NONE)
  private String[] assetConfigName;

  private WavesChainI() {}

  public WavesChainI(String... assetConfigName) throws AssetNotFoundException {
    this.assetConfigName =
        Objects.requireNonNull(
            assetConfigName,
            "Cannot construct object "
                + PolygonChainI.class.getName()
                + " without the required parameter(s)");

    Object2ObjectHashMap<String, Object> chain2IdentifierMapping = new Object2ObjectHashMap<>();
    chain2IdentifierMapping.put(
        configObject.get("Blockchain.Waves.chain_identifier"), new WavesChainI<>());

    Object2ObjectHashMap<String, Config> assets = new Object2ObjectHashMap<>();
    Config assetList = configObject.get("Blockchain.Waves.asset");

    for (String configName : this.assetConfigName) {
      if (!assetList.contains(configName)) {
        throw new AssetNotFoundException(
            String.format("Asset: %s, could not be found in the config file", configName));
      }
      assets.put(configName, configObject.get("Blockchain.Waves.asset" + "." + assetConfigName));
    }
    super.setAsset(assets);
    super.setNetworkNode(configObject.get("Blockchain.Waves.node"));
    super.setNetwork(configObject.get("Blockchain.Waves.network"));
    super.setNetworkID(configObject.get("Blockchain.Waves.network_id"));
    super.setChainIdentifier(configObject.get("Blockchain.Waves.chain_identifier"));
    super.setChain2IdentifierMapping(chain2IdentifierMapping);
    super.init();
  }
}
