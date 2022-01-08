package bridge.blockchains.ethchains;

import bridge.common.IChainQueryService;
import com.electronwill.nightconfig.core.Config;
import java.util.List;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.http.HttpService;

@Data
class EthBaseChain<K> implements IChainQueryService {

  @Setter(AccessLevel.PROTECTED)
  Config asset;

  @Setter(AccessLevel.PROTECTED)
  private String networkNode;

  @Setter(AccessLevel.PROTECTED)
  private String network;

  @Setter(AccessLevel.PROTECTED)
  private K networkID;

  @Setter(AccessLevel.PROTECTED)
  private String chainIdentifier;

  @Setter(AccessLevel.NONE)
  private String assetName;

  @Setter(AccessLevel.NONE)
  private String assetID;

  @Setter(AccessLevel.NONE)
  private String assetTicker;

  @Setter(AccessLevel.NONE)
  private Config assetControlWallet;

  @Setter(AccessLevel.NONE)
  private double transferFee;

  @Setter(AccessLevel.NONE)
  @Getter(AccessLevel.NONE)
  private Web3j web3j;

  public void init(Config asset) {
    this.asset = asset;
    this.assetID = asset.get("asset_id");
    this.assetName = asset.get("name");
    this.assetTicker = asset.get("ticker");
    this.transferFee = asset.get("transfer_fee");
    this.assetControlWallet = asset.get("wallet");
    web3j = initWeb3j();
  }

  private Web3j initWeb3j() {
    return Web3j.build(new HttpService(this.networkNode));
  }

  @Override
  public <T> List<T> getTrxOfBlockAtHeight(int height) {
    return IChainQueryService.super.getTrxOfBlockAtHeight(height);
  }

  @Override
  public String getTrxByID(String trxID) {
    return IChainQueryService.super.getTrxByID(trxID);
  }

  @Override
  public <T> T getTrxHash(int blockHeight) {
    return IChainQueryService.super.getTrxHash(blockHeight);
  }

  @Override
  public boolean validateAddress(String address) {
    return false;
  }
}
