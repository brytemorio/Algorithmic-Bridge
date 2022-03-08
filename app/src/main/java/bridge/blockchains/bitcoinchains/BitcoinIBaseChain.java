package bridge.blockchains.bitcoinchains;

import java.math.BigInteger;
import java.net.URL;
import java.util.ArrayList;
import org.agrona.collections.Object2ObjectHashMap;
import com.electronwill.nightconfig.core.Config;
import bridge.blockchains.IBaseChain;
import bridge.common.TransactionModels;
import bridge.common.TransactionModels.TransactionReceiver;
import bridge.common.TransactionModels.TransactionSender;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import wf.bitcoin.javabitcoindrpcclient.BitcoinJSONRPCClient;
import wf.bitcoin.javabitcoindrpcclient.BitcoindRpcClient.Block;
import wf.bitcoin.javabitcoindrpcclient.BitcoindRpcClient.RawTransaction;

@SuppressWarnings("unchecked")
class BitcoinIBaseChain implements IBaseChain {

  @Getter
  @Setter(AccessLevel.PROTECTED)
  private Config asset;

  @Getter
  @Setter(AccessLevel.PROTECTED)
  private String hostName;

  @Getter
  @Setter(AccessLevel.PROTECTED)
  private int rpcPort;

  @Getter
  @Setter(AccessLevel.PROTECTED)
  private String rpcPassword;

  @Getter
  @Setter(AccessLevel.PROTECTED)
  private String controlWalletAddress;

  @Getter
  @Setter(AccessLevel.PROTECTED)
  private String network;

  @Getter
  @Setter(AccessLevel.PROTECTED)
  private String chainIdentifier;

  @Getter
  @Setter(AccessLevel.PROTECTED)
  private String rpcUser;

  @Getter
  @Setter(AccessLevel.PROTECTED)
  private URL jasonRPCUrl;

  @Getter private String assetName;

  @Getter private String assetTicker;

  @Getter private double assetTransferFee;

  @Getter
  @Setter(AccessLevel.PROTECTED)
  Object2ObjectHashMap<String, Object> chain2IdentifierMapping;

  private BitcoinJSONRPCClient rpcClient;

  private BigInteger previousBlockHeight = BigInteger.ZERO;

  protected void init() {
    this.assetName = asset.get("name");
    this.assetTicker = asset.get("ticker");
    this.assetTransferFee = asset.get("transfer_fee");
    this.rpcClient = new BitcoinJSONRPCClient(jasonRPCUrl);
  }

  // Todo: Complete this function
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
    return rpcClient.validateAddress(address).isValid();
  }

  @Override
  public TransactionModels.Transaction getTransaction(String trxId, String assetName) {
    RawTransaction decodedTrx = getTrxByID(trxId);
    return new TransactionModels.Transaction(
        trxId, getTrxReceivers(decodedTrx), getTrxSenders(decodedTrx));
  }

  // ====================== Helper Functions=====================/

  private ArrayList<TransactionReceiver> getTrxReceivers(RawTransaction decodedTrx) {
    ArrayList<TransactionReceiver> receivers = new ArrayList<>();
    for (var iter : decodedTrx.vOut()) {
      if (iter.scriptPubKey().addresses().isEmpty()) continue;
      iter.scriptPubKey()
          .addresses()
          .forEach(
              receiverAddress ->
                  receivers.add(
                      new TransactionReceiver(receiverAddress, iter.value().doubleValue())));
    }
    return receivers;
  }

  private ArrayList<TransactionSender> getTrxSenders(RawTransaction decodedTrx) {
    ArrayList<TransactionSender> senders = new ArrayList<>();
    decodedTrx.vIn().parallelStream()
        .forEach(
            vin -> {
              if (vin.txid().isEmpty() || vin.txid().isBlank() || vin.vout() == null) return;
              RawTransaction vInTrx = getTrxByID(vin.txid());
              var vinAddresses = vInTrx.vOut().get(vin.vout()).scriptPubKey().addresses();
              if (vinAddresses.isEmpty()) return;
              vinAddresses.parallelStream()
                  .forEach(
                      address -> {
                        var trxSenders = new TransactionSender(address);
                        if (!senders.contains(trxSenders)) senders.add(trxSenders);
                      });
            });

    return senders;
  }
}
