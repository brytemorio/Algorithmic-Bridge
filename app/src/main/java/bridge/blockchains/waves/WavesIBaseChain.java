package bridge.blockchains.waves;

import bridge.common.IBaseChain;
import com.electronwill.nightconfig.core.Config;
import com.wavesplatform.wavesj.Block;
import com.wavesplatform.wavesj.Node;
import java.util.List;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import org.agrona.collections.Object2ObjectHashMap;

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

  @Override
  public void init() {
    this.rpcClient = getNodeObj();
  }

  @SneakyThrows
  private Node getNodeObj() {
    return new Node(networkNode);
  }

  @Override
  @SneakyThrows
  public Number getBlockHeight() {
    return rpcClient.getHeight();
  }

  @Override
  @SneakyThrows
  public String getNodeResponse(String nodeEndpoint) {
    String fullRPCQueryPath = networkNode + nodeEndpoint;
    return IBaseChain.super.getNodeResponse(fullRPCQueryPath);
  }

  @Override
  @SneakyThrows
  public Block getTrxOfBlockAtHeight(int height) {
    return rpcClient.getBlock(height);
  }

  @Override
  public String getTrxByID(String trxID) {
    return IBaseChain.super.getTrxByID(trxID);
  }

  @Override
  public <T> List<T> getTrxHash(int blockHeight) {
    return IBaseChain.super.getTrxHash(blockHeight);
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
