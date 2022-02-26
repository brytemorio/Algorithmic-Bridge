package bridge.blockchains.waves;

import bridge.blockchains.IBaseChain;
import bridge.common.BridgeUtils;
import com.electronwill.nightconfig.core.Config;
import com.wavesplatform.wavesj.Block;
import com.wavesplatform.wavesj.Node;
import java.math.BigInteger;
import java.util.ArrayList;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import org.agrona.collections.Object2ObjectHashMap;

@SuppressWarnings("unchecked")
class WavesIBaseChain<K> implements IBaseChain {

  @Setter(AccessLevel.PROTECTED)
  @Getter
  private String networkNode;

  @Getter
  @Setter(AccessLevel.PROTECTED)
  private String network;

  @Getter
  @Setter(AccessLevel.PROTECTED)
  private K networkID;

  @Getter
  @Setter(AccessLevel.PROTECTED)
  private String chainIdentifier;

  @Setter(AccessLevel.PROTECTED)
  @Getter(AccessLevel.PROTECTED)
  private Object2ObjectHashMap<String, Config> asset;

  @Getter
  @Setter(AccessLevel.PROTECTED)
  Object2ObjectHashMap<String, Object> chain2IdentifierMapping;

  private Node rpcClient;

  private BigInteger previousBlockHeight = BigInteger.ZERO;

  public String getAssetID(String assetConfigName) {
    WavesAssets assetInfo = assetInfoFactory(assetConfigName);
    return assetInfo.getAssetID();
  }

  public String getAssetTicker(String assetConfigName) {
    WavesAssets assetInfo = assetInfoFactory(assetConfigName);
    return assetInfo.getAssetTicker();
  }

  public String getAssetName(String assetConfigName) {
    WavesAssets assetInfo = assetInfoFactory(assetConfigName);
    return assetInfo.getAssetName();
  }

  public Double getAssetTransferFee(String assetConfigName) {
    WavesAssets assetInfo = assetInfoFactory(assetConfigName);
    return assetInfo.getTransferFee();
  }

  private WavesAssets assetInfoFactory(String assetConfigName) {
    return new WavesAssets(asset.get(assetConfigName));
  }

  protected void init() {
    this.rpcClient = getNodeObj();
  }

  @SneakyThrows
  public Node getNodeObj() {
    return new Node(networkNode);
  }

  @Override
  @SneakyThrows
  public BigInteger getBlockHeight() {
    boolean noNewblockFound = true;
    while (noNewblockFound) {
      Integer height = rpcClient.getHeight();
      BigInteger currentBlockHeight = BigInteger.valueOf(height.intValue());
      if (currentBlockHeight.compareTo(this.previousBlockHeight) > 0) {
        previousBlockHeight = currentBlockHeight;
        noNewblockFound = false;
      }
    }
    return previousBlockHeight;
  }

  @Override
  @SneakyThrows
  public ArrayList<String> getTrxIdsByBlockHeight(BigInteger height) {
    Block block = rpcClient.getBlock(height.intValue());
    ArrayList<String> trxIDs = new ArrayList<>();
    for (var eachTrxDS : block.transactions()) {
      Config trx = BridgeUtils.getJsonDeserializer().parse(eachTrxDS.tx().toJson());
      trxIDs.add(trx.get("id"));
    }
    return trxIDs;
  }

  @Override
  public String getTrxByID(String trxID) {
    return null;
  }

  @Override
  public <T> ArrayList<String> getTrxHash(T blockHeight) {
    return new ArrayList<>();
  }

  @Override
  public boolean validateAddress(String address) {
    return false;
  }

  @Data
  class WavesAssets {

    @Setter(AccessLevel.NONE)
    private String assetID;

    @Setter(AccessLevel.NONE)
    private String assetName;

    @Setter(AccessLevel.NONE)
    private String assetTicker;

    @Setter(AccessLevel.NONE)
    private Double transferFee;

    @Setter(AccessLevel.NONE)
    private Config assetControlWallet;

    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    private Config asset;

    public WavesAssets(final Config asset) {
      this.asset = asset;
      this.assetID = this.asset.get("asset_id");
      this.assetName = this.asset.get("name");
      this.assetTicker = this.asset.get("ticker");
      this.transferFee = this.asset.get("transfer_fee");
      this.assetControlWallet = this.asset.get("wallet");
    }
  }
}
