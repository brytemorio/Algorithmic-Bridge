package bridge.blockchains.waves;

import bridge.common.BridgeUtils;
import bridge.common.IBaseChain;
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
@Data
class WavesIBaseChain<K> implements IBaseChain {

  @Setter(AccessLevel.PROTECTED)
  private String networkNode;

  @Setter(AccessLevel.PROTECTED)
  private String network;

  @Setter(AccessLevel.PROTECTED)
  private K networkID;

  @Setter(AccessLevel.PROTECTED)
  private String chainIdentifier;

  @Setter(AccessLevel.PROTECTED)
  @Getter(AccessLevel.PROTECTED)
  private Object2ObjectHashMap<String, Config> asset;

  @Setter(AccessLevel.PROTECTED)
  Object2ObjectHashMap<String, Object> chain2IdentifierMapping;

  @Setter(AccessLevel.NONE)
  @Getter(AccessLevel.NONE)
  private Node rpcClient;

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
  public Integer getBlockHeight() {
    return rpcClient.getHeight();
  }

  @Override
  @SneakyThrows
  public ArrayList<String> getTrxOfBlockAtHeight(BigInteger height) {
    Block block = rpcClient.getBlock(height.intValue());
    ArrayList<String> trxId = new ArrayList<>();
    for (var tt : block.transactions()) {
      Config parsed = BridgeUtils.getJsonDeserializer().parse((tt.tx().toJson()));
      trxId.add(parsed.get("id").toString());
    }
    return trxId;
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
