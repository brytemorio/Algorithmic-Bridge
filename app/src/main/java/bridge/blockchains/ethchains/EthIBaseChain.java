package bridge.blockchains.ethchains;

import bridge.common.IBaseChain;
import com.electronwill.nightconfig.core.Config;
import java.util.List;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import org.agrona.collections.Object2ObjectHashMap;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.http.HttpService;

@Data
class EthIBaseChain<K> implements IBaseChain {

  @Setter(AccessLevel.PROTECTED)
  @Getter(AccessLevel.PROTECTED)
  Object2ObjectHashMap<String, Config> asset;

  @Setter(AccessLevel.PROTECTED)
  private String networkNode;

  @Setter(AccessLevel.PROTECTED)
  private String network;

  @Setter(AccessLevel.PROTECTED)
  private K networkID;

  @Setter(AccessLevel.PROTECTED)
  private String chainIdentifier;

  @Setter(AccessLevel.NONE)
  @Getter(AccessLevel.NONE)
  private Web3j web3j;

  public String getAssetID(String assetConfigName) {
    Assets assetInfo = assetInfoFactory(assetConfigName);
    return assetInfo.getAssetID();
  }

  public String getAssetTicker(String assetConfigName) {
    Assets assetInfo = assetInfoFactory(assetConfigName);
    return assetInfo.getAssetTicker();
  }

  public String getAssetName(String assetConfigName) {
    Assets assetInfo = assetInfoFactory(assetConfigName);
    return assetInfo.getAssetName();
  }

  public Double getAssetTransferFee(String assetConfigName) {
    Assets assetInfo = assetInfoFactory(assetConfigName);
    return assetInfo.getTransferFee();
  }

  private Assets assetInfoFactory(String assetConfigName) {
    return new Assets(asset.get(assetConfigName));
  }

  @Override
  public void init() {
    this.web3j = initWeb3j();
  }

  @SneakyThrows
  private Web3j initWeb3j() {
    return Web3j.build(new HttpService(networkNode));
  }

  @Override
  @SneakyThrows
  public Number getBlockHeight() {
    return web3j.ethBlockNumber().send().getBlockNumber();
  }

  @Override
  public <T> T getTrxOfBlockAtHeight(int height) {
    return IBaseChain.super.getTrxOfBlockAtHeight(height);
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
  class Assets {

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

    public Assets(final Config asset) {
      this.asset = asset;
      this.assetID = this.asset.get("asset_id");
      this.assetName = this.asset.get("name");
      this.assetTicker = this.asset.get("ticker");
      this.transferFee = this.asset.get("transfer_fee");
      this.assetControlWallet = this.asset.get("wallet");
    }
  }
}
