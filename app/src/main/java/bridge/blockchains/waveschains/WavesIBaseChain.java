package bridge.blockchains.waveschains;

import bridge.blockchains.Asset;
import bridge.blockchains.IBaseChain;
import bridge.common.BridgeUtils;
import bridge.exceptions.BridgeExceptions;
import bridge.services.storagservice.AssetStorageService;
import bridge.services.storagservice.TransactionAttemptListStorageService;
import bridge.services.transactionservice.TransactionModels;
import bridge.services.transactionservice.TransactionModels.Transaction;
import bridge.services.transactionservice.TransactionModels.TransactionReceiver;
import bridge.services.transactionservice.TransactionModels.TransactionSender;
import com.electronwill.nightconfig.core.Config;
import com.wavesplatform.transactions.account.Address;
import com.wavesplatform.transactions.common.Id;
import com.wavesplatform.wavesj.Block;
import com.wavesplatform.wavesj.Node;
import com.wavesplatform.wavesj.info.TransactionInfo;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.agrona.collections.Object2ObjectHashMap;

import java.math.BigInteger;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Slf4j
@SuppressWarnings("unchecked")
class WavesIBaseChain<K> implements IBaseChain
{

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

  @Getter
  @Setter(AccessLevel.PROTECTED)
  Object2ObjectHashMap<String, Object> chain2IdentifierMapping;

  @Getter
  @Setter
  private String wavesBridgeAddress;

  @Getter
  private ArrayList<Asset> wavesAsset;
  private Node wavesRpcClient;

  private TransactionAttemptListStorageService trxAttemptListStorage;
  private BigInteger previousBlockHeight;

  private String assetID;
  private String ticker;

  protected void init()
  {
    this.wavesRpcClient = getNodeObj();
    this.previousBlockHeight = BigInteger.ZERO;
    this.trxAttemptListStorage = new TransactionAttemptListStorageService();
    AssetStorageService assetStorage = new AssetStorageService();
    this.wavesAsset = assetStorage.getAssetFromStorage("waves");
  }


  @SneakyThrows
  public Node getNodeObj()
  {
    return new Node(networkNode);
  }

  @Override
  @SneakyThrows
  public BigInteger getBlockHeight()
  {

    int height = wavesRpcClient.getHeight();

    while (height == wavesRpcClient.getHeight())
    {
    }

    previousBlockHeight = BigInteger.valueOf(height);
    return previousBlockHeight;
  }

  @Override
  @SneakyThrows
  public ArrayList<String> getTrxIdsByBlockHeight(BigInteger height)
  {
    Block block = wavesRpcClient.getBlock(height.intValue());
    ArrayList<String> trxIDs = new ArrayList<>();

    for (var eachTrxDS : block.transactions())
    {
      Config trx = BridgeUtils.getJsonDeserializer().parse(eachTrxDS.tx().toJson());
      trxIDs.add(trx.get("id"));
    }
    return trxIDs;
  }

  @Override
  @SneakyThrows
  public String getTrxByID(String trxID)
  {
    TransactionInfo trxInfo = wavesRpcClient.getTransactionInfo(Id.as(trxID));
    return trxInfo.tx().toJson();
  }

  @Override
  public boolean validateAddress(String address)
  {
    return Address.isValid(address);
  }

  @Override
  public Transaction sendCoin(TransactionModels.TransactionAttempt attempt)
  {
    //Todo: implement this function
    return null;
  }

  @Override
  public Boolean filterTransactions(Transaction trx, TransactionModels.MappedAddress sender)
  {
    if (this.trxAttemptListStorage.gatewayTransactionExists(trx.getTransactionID())) return false;
    else return !_filterTransactionReceivers(trx, sender.getAddress()).isEmpty();
  }


  /*
   * FIXME: Use TransactionSender Model from {bridge.services.transactionservice.TransactionModel}
   * */
  @Override
  public void handleTransaction(Transaction trx, TransactionModels.MappedAddress sender)
  {
    var receivers = this._filterTransactionReceivers(trx, sender.getAssetName());

    //FIXME: this may not  be necessary
    if (receivers.size() > 1)
      throw new BridgeExceptions.MultipleGateWayReceiverException(trx.getTransactionID());

    //FIXME: senders should not be empty. senderAddress
    for (var tx : receivers)
      this._handleTransaction(trx.getTransactionID(), sender.getAddress(),
          trx.getReceivers().get(tx), tx, sender.getAssetName());
  }

  @Override
  public Transaction getTransaction(String trxID)
  {

    Config transaction = BridgeUtils.getJsonDeserializer().parse(getTrxByID(trxID));


    if (Integer.parseInt(transaction.get("type")) != 4) return null;

    //Todo: Optimised this block of code
    if (!transaction.contains("assetId"))
    {
      return null;
    }
    else
    {
      for (var asseti : wavesAsset)
      {
        String waveAssetId = Objects.requireNonNull(asseti.getAssetId(),
            "asset Id not defined asset id for waves chain cannot be null");
        if (waveAssetId.equals(transaction.get("assetId")))
        {
          this.assetID = waveAssetId;
          this.ticker = asseti.getTicker();
          break;
        }
        else
        {
          this.assetID = null;
        }
      }
      if (this.assetID == null) return null;
    }
    ;


    ArrayList<TransactionReceiver> recipients = new ArrayList<>();
    ArrayList<TransactionSender> senders = new ArrayList<>();

    String sender;
    String receiver;
    int amount = transaction.get("amount");

    try
    {
      sender = transaction.get("sender").toString().split(":")[1];
    }
    catch (ArrayIndexOutOfBoundsException exp)
    {
      sender = transaction.get("sender");
    }

    try
    {
      receiver = transaction.get("recipient").toString().split(":")[1];
    }
    catch (ArrayIndexOutOfBoundsException exp)
    {
      receiver = transaction.get("recipient");
    }

    senders.add(new TransactionSender(new TransactionModels.MappedAddress(sender, this.ticker)));
    recipients.add(
        new TransactionReceiver(new TransactionModels.MappedAddress(receiver, this.ticker), amount));
    return new Transaction(trxID, recipients, senders);
  }


  // ======================Helper functions==============================//

  @SneakyThrows
  private String getTokenReceiverFromTransaction(String transactionID)
  {
    TransactionInfo trxInfo = this.wavesRpcClient.getTransactionInfo(Id.as(transactionID));
    Config attachment = BridgeUtils.getJsonDeserializer().parse(trxInfo.tx().toJson());
    return attachment.get("attachment");
  }


  /*ensure only transactions to the associated waves bridge address should be handled*/
  private List<Integer> _filterTransactionReceivers(Transaction trx, String assetName)
  {
    List<Integer> result = new ArrayList<>();
    List<TransactionReceiver> receivers = trx.getReceivers();
    for (int i = 0; i <= receivers.size(); i++)
    {
      if (receivers.get(i).getAddress().getAddress().equals(this.wavesBridgeAddress))
      {
        var trx_attempt = this.trxAttemptListStorage.findTransactionAttemptByTrigger(
            new TransactionModels.TransactionAttemptListTrigger(trx.getTransactionID(), i,
                assetName));
        if (trx_attempt == null || trx_attempt.hasCompleted()) result.add(i);
      }
    }
    return result;
  }

  private void _handleTransaction(String transactionId, TransactionReceiver receiver, int index)
  {
    TransactionModels.TransactionAttemptList attempt_list;
    TransactionModels.TransactionAttemptListTrigger trigger;


    attempt_list = this.trxAttemptListStorage.findTransactionAttemptByTrigger(
        new TransactionModels.TransactionAttemptListTrigger(transactionId, index, this.ticker));


    if (attempt_list == null)
    {
      trigger = new TransactionModels.TransactionAttemptListTrigger(transactionId, index,
          this.ticker);

      List<TransactionModels.TransactionAttempt> attempts = new ArrayList<>();
      List<TransactionModels.TransactionAttemptReceiver> tokenReceiversList = new ArrayList<>();

      String tokenReceiver = this.getTokenReceiverFromTransaction(transactionId);

      tokenReceiversList.add(
          new TransactionModels.TransactionAttemptReceiver(tokenReceiver, receiver.getAmount()));

      attempts.add(new TransactionModels.TransactionAttempt(sendingAddress, 0, assetName,
          tokenReceiversList));

      attempt_list = new TransactionModels.TransactionAttemptList(trigger, attempts,
          ZonedDateTime.now(ZoneId.of("Etc/Zulu")), ZonedDateTime.now(ZoneId.of("Etc/Zulu")));
      this.trxAttemptListStorage.saveTransactionAttemptList(attempt_list);
      log.info("Created new attempt list " + attempt_list);
    }

  }
}
