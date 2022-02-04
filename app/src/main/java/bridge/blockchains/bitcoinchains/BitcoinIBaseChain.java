package bridge.blockchains.bitcoinchains;

import bridge.blockchains.IBaseChain;
import com.electronwill.nightconfig.core.Config;
import java.math.BigInteger;
import java.net.URL;
import java.util.ArrayList;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.agrona.collections.Object2ObjectHashMap;
import wf.bitcoin.javabitcoindrpcclient.BitcoinJSONRPCClient;
import wf.bitcoin.javabitcoindrpcclient.BitcoindRpcClient;
import wf.bitcoin.javabitcoindrpcclient.BitcoindRpcClient.Block;

@SuppressWarnings("unchecked")
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

  @Setter(AccessLevel.PROTECTED)
  Object2ObjectHashMap<String, Object> chain2IdentifierMapping;

  @Setter(AccessLevel.NONE)
  @Getter(AccessLevel.NONE)
  private BitcoinJSONRPCClient rpcClient;

  protected void init() {
    this.assetName = asset.get("name");
    this.assetTicker = asset.get("ticker");
    this.assetTransferFee = asset.get("transfer_fee");
    this.rpcClient = new BitcoinJSONRPCClient(jasonRPCUrl);
  }

  @Override
  public BigInteger getBlockHeight() {
    Integer height = rpcClient.getBlockCount();
    return BigInteger.valueOf(height.intValue());
  }

  @Override
  public ArrayList<String> getTrxIdsByBlockHeight(BigInteger height) {
    ArrayList<String> trx = new ArrayList<>();
    Block block = rpcClient.getBlock(height.intValue());
    var trxList = block.tx();
    trx.addAll(trxList);
    return trx;
  }

  @Override
  public String getTrxByID(String trxID) {
    String rawTrx = rpcClient.getRawTransactionHex(trxID);
    BitcoindRpcClient.RawTransaction trx = rpcClient.decodeRawTransaction(rawTrx);
    return trx.toString();
  }

  @Override
  public <T> ArrayList<String> getTrxHash(T blockHeight) {
    return new ArrayList<>();
  }

  @Override
  public boolean validateAddress(String address) {
    return false;
  }
}
