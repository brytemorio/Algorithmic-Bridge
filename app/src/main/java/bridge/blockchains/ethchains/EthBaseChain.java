package bridge.blockchains.ethchains;

import bridge.common.BaseChainInterface;
import com.electronwill.nightconfig.core.Config;
import java.util.List;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.http.HttpService;

@Data
class EthBaseChain<K> implements BaseChainInterface {

  @Setter(AccessLevel.PROTECTED)
  @Getter(AccessLevel.PROTECTED)
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

  public void init() {
    this.assetID = this.asset.get("asset_id");
    this.assetName = this.asset.get("name");
    this.assetTicker = this.asset.get("ticker");
    this.transferFee = this.asset.get("transfer_fee");
    this.assetControlWallet = this.asset.get("wallet");
    this.web3j = initWeb3j();
  }

  private Web3j initWeb3j() {
    return Web3j.build(new HttpService(this.networkNode));
  }

  @Override
  public <T> List<T> getTrxOfBlockAtHeight(int height) {
    return BaseChainInterface.super.getTrxOfBlockAtHeight(height);
  }

  @Override
  public String getTrxByID(String trxID) {
    return BaseChainInterface.super.getTrxByID(trxID);
  }

  @Override
  public <T> T getTrxHash(int blockHeight) {
    return BaseChainInterface.super.getTrxHash(blockHeight);
  }

  @Override
  public boolean validateAddress(String address) {
    return false;
  }
}
