package bridge.blockchains.waveschains;

import java.math.BigInteger;
import java.util.ArrayList;
import org.agrona.collections.Object2ObjectHashMap;
import com.electronwill.nightconfig.core.Config;
import com.wavesplatform.transactions.account.Address;
import com.wavesplatform.transactions.common.Id;
import com.wavesplatform.wavesj.Block;
import com.wavesplatform.wavesj.Node;
import com.wavesplatform.wavesj.info.TransactionInfo;
import bridge.blockchains.IBaseChain;
import bridge.common.BridgeUtils;
import bridge.common.TransactionModels.Transaction;
import bridge.common.TransactionModels.TransactionReceiver;
import bridge.common.TransactionModels.TransactionSender;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;

@SuppressWarnings("unchecked")
class WavesIBaseChain<K> implements IBaseChain {

  @Setter(AccessLevel.PROTECTED)
  @Getter
  private String networkNode;

  @Getter
  @Setter(AccessLevel.PROTECTED)
  private String network;

  @Getter
  @Setter(AccessLevel.PROTECTED)
  private K networkID;

  @Getter
  @Setter(AccessLevel.PROTECTED)
  private String chainIdentifier;

  @Setter(AccessLevel.PROTECTED)
  @Getter(AccessLevel.PROTECTED)
  private Object2ObjectHashMap<String, Config> asset;

  @Getter
  @Setter(AccessLevel.PROTECTED)
  Object2ObjectHashMap<String, Object> chain2IdentifierMapping;

  private Node wavesRpcClient;

  private BigInteger previousBlockHeight = BigInteger.ZERO;

  protected void init() {
    this.wavesRpcClient = getNodeObj();
  }

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

  public double getAssetTransferFee(String assetConfigName) {
    WavesAssets assetInfo = assetInfoFactory(assetConfigName);
    return assetInfo.getTransferFee();
  }

  public int getAssetDecimals(String assetConfigName) {
    WavesAssets assetInfo = assetInfoFactory(assetConfigName);
    return assetInfo.getDecimals();
  }

  @SneakyThrows
  public Node getNodeObj() {
    return new Node(networkNode);
  }

  @Override
  @SneakyThrows
  public BigInteger getBlockHeight() {
    boolean noNewblockFound = true;
    while (noNewblockFound) {
      Integer height = wavesRpcClient.getHeight();
      BigInteger currentBlockHeight = BigInteger.valueOf(height.intValue());
      if (currentBlockHeight.compareTo(this.previousBlockHeight) > 0) {
        previousBlockHeight = currentBlockHeight;
        noNewblockFound = false;
      }
    }
    return previousBlockHeight;
  }

  @Override
  @SneakyThrows
  public ArrayList<String> getTrxIdsByBlockHeight(BigInteger height) {
    Block block = wavesRpcClient.getBlock(height.intValue());
    ArrayList<String> trxIDs = new ArrayList<>();

    for (var eachTrxDS : block.transactions()) {
      Config trx = BridgeUtils.getJsonDeserializer().parse(eachTrxDS.tx().toJson());
      trxIDs.add(trx.get("id"));
    }
    return trxIDs;
  }

  @Override
  @SneakyThrows
  public String getTrxByID(String trxID) {
    TransactionInfo trxInfo = wavesRpcClient.getTransactionInfo(Id.as(trxID));
    return trxInfo.tx().toJson();
  }

  @Override
  public boolean validateAddress(String address) {
    return Address.isValid(address);
  }

  @Override
  public Transaction getTransaction(String trxID, String assetName) {
    String assetID = getAssetID(assetName);
    Config transaction = BridgeUtils.getJsonDeserializer().parse(getTrxByID(trxID));

    if (!transaction.contains("assetId")
        || transaction.get("assetId") != assetID
        || Integer.parseInt(transaction.get("type")) != 4) return null;

    ArrayList<TransactionReceiver> recipients = new ArrayList<>();
    ArrayList<TransactionSender> senders = new ArrayList<>();

    String sender;
    String receiver;
    double amount = waveAmount2Double(transaction.get("amount"), assetName);

    try {
      sender = transaction.get("sender").toString().split(":")[1];
    } catch (ArrayIndexOutOfBoundsException exp) {
      sender = transaction.get("sender");
    }

    try {
      receiver = transaction.get("recipient").toString().split(":")[1];
    } catch (ArrayIndexOutOfBoundsException exp) {
      receiver = transaction.get("recipient");
    }

    senders.add(new TransactionSender(sender));
    recipients.add(new TransactionReceiver(receiver, amount));
    return new Transaction(trxID, recipients, senders);
  }

  class WavesAssets {

    @Getter private String assetID;

    @Getter private String assetName;

    @Getter private String assetTicker;

    @Getter private double transferFee;

    @Getter private int decimals;

    @Getter private Config assetControlWallet;

    private Config asset;

    public WavesAssets(final Config asset) {
      this.asset = asset;
      this.assetID = this.asset.get("asset_id");
      this.assetName = this.asset.get("name");
      this.assetTicker = this.asset.get("ticker");
      this.transferFee = this.asset.get("transfer_fee");
      this.decimals = this.asset.get("decimals");
      this.assetControlWallet = this.asset.get("wallet");
    }
  }

  // ======================Helper functions==============================//
  private double waveAmount2Double(int val, String assetName) {
    int decimalPlace = getAssetDecimals(assetName);
    double result = (double) val / (double) decimalPlace;
    return BridgeUtils.roundUp(result, decimalPlace);
  }

  private WavesAssets assetInfoFactory(String assetConfigName) {
    return new WavesAssets(asset.get(assetConfigName));
  }
}
