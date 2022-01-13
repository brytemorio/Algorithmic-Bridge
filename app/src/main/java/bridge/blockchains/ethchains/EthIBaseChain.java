package bridge.blockchains.ethchains;

import bridge.common.IBaseChain;
import com.electronwill.nightconfig.core.Config;
import java.math.BigInteger;
import java.util.List;
import lombok.*;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.http.HttpService;

@Data
class EthIBaseChain<K> implements IBaseChain {

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

  @SneakyThrows
  private Web3j initWeb3j() {
    return Web3j.build(new HttpService(networkNode));
  }

  @Override
  @SneakyThrows
  public BigInteger getBlockHeight() {
    return web3j.ethBlockNumber().send().getBlockNumber();
  }

  @Override
  public <T> List<T> getTrxOfBlockAtHeight(int height) {
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
}
