package bridge.blockchains.bitcoinchains;

import bridge.common.IBaseChain;
import com.electronwill.nightconfig.core.Config;
import java.net.URL;
import java.util.List;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import wf.bitcoin.javabitcoindrpcclient.BitcoinJSONRPCClient;
import wf.bitcoin.javabitcoindrpcclient.BitcoindRpcClient;

@Data
class BitcoinIBaseChain implements IBaseChain {

  @Setter(AccessLevel.PROTECTED)
  private Config asset;

  @Setter(AccessLevel.PROTECTED)
  private String hostName;

  @Setter(AccessLevel.PROTECTED)
  private int rpcPort;

  @Setter(AccessLevel.PROTECTED)
  private String rpcPassword;

  @Setter(AccessLevel.PROTECTED)
  private String controlWalletAddress;

  @Setter(AccessLevel.PROTECTED)
  private String network;

  @Setter(AccessLevel.PROTECTED)
  private String chainIdentifier;

  @Setter(AccessLevel.PROTECTED)
  private String rpcUser;

  @Setter(AccessLevel.PROTECTED)
  private URL jasonRPCUrl;

  @Setter(AccessLevel.NONE)
  private String assetName;

  @Setter(AccessLevel.NONE)
  private String assetTicker;

  @Setter(AccessLevel.NONE)
  private double assetTransferFee;

  @Setter(AccessLevel.NONE)
  @Getter(AccessLevel.NONE)
  private BitcoinJSONRPCClient rpcClient;

  public void init() {
    this.assetName = asset.get("name");
    this.assetTicker = asset.get("ticker");
    this.assetTransferFee = asset.get("transfer_fee");
    this.rpcClient = new BitcoinJSONRPCClient(jasonRPCUrl);
  }

  @Override
  public Integer getBlockHeight() {
    return rpcClient.getBlockCount();
  }

  @Override
  public <T> List<T> getTrxOfBlockAtHeight(int height) {
    return IBaseChain.super.getTrxOfBlockAtHeight(height);
  }

  @Override
  public String getTrxByID(String trxID) {
    String rawTrx = rpcClient.getRawTransactionHex(trxID);
    BitcoindRpcClient.RawTransaction trx = rpcClient.decodeRawTransaction(rawTrx);
    return trx.toString();
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
