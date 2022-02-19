package bridge.blockchains.bitcoinchains;

import java.math.BigInteger;
import java.net.URL;
import java.util.ArrayList;
import org.agrona.collections.Object2ObjectHashMap;
import com.electronwill.nightconfig.core.Config;
import bridge.blockchains.IBaseChain;
import bridge.common.TransactionModels.TransactionReceiver;
import bridge.common.TransactionModels.TransactionSender;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import wf.bitcoin.javabitcoindrpcclient.BitcoinJSONRPCClient;
import wf.bitcoin.javabitcoindrpcclient.BitcoindRpcClient.Block;
import wf.bitcoin.javabitcoindrpcclient.BitcoindRpcClient.RawTransaction;

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

  @Setter(AccessLevel.NONE)
  @Getter(AccessLevel.NONE)
  private BigInteger previousBlockHeight = BigInteger.ZERO;

  protected void init() {
    this.assetName = asset.get("name");
    this.assetTicker = asset.get("ticker");
    this.assetTransferFee = asset.get("transfer_fee");
    this.rpcClient = new BitcoinJSONRPCClient(jasonRPCUrl);
  }

  public void extractTrxInfo(String trxID) {
    var transaction = getTrxByID(trxID);
    ArrayList<TransactionSender> senders = new ArrayList<>();
    ArrayList<TransactionReceiver> receivers = new ArrayList<>();

    for (var iter : transaction.vOut()) {
      if (iter.scriptPubKey().addresses().isEmpty()) continue;
    }
  }

  @Override
  public BigInteger getBlockHeight() {

    boolean noNewBlockFound = true;
    while (noNewBlockFound) {
      Integer height = rpcClient.getBlockCount();
      BigInteger currentHeight = BigInteger.valueOf(height.intValue());
      if (currentHeight.compareTo(this.previousBlockHeight) > 0) {
        previousBlockHeight = currentHeight;
        noNewBlockFound = false;
      }
    }
    return previousBlockHeight;
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
  public RawTransaction getTrxByID(String trxID) {
    String rawTrx = rpcClient.getRawTransactionHex(trxID);
    return rpcClient.decodeRawTransaction(rawTrx);
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
