package bridge.blockchains.waveschains;

import bridge.blockchains.IBaseChain;
import bridge.common.BridgeUtils;
import bridge.disruptorservice.AddressValidator;
import bridge.exceptions.BridgeExceptions;
import bridge.services.storagservice.ConfigurationStorageService;
import bridge.services.storagservice.DataObjects;
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
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

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

  private Node wavesRpcClient;
  private TransactionAttemptListStorageService trxAttemptListStorage;
  private BigInteger previousBlockHeight;
  private String ticker;

  private DataObjects.ConfigurationStorage wavesConfig;

  @SneakyThrows
  protected void init()
  {
    this.wavesConfig = new ConfigurationStorageService().getConfiguration("waves");
    this.previousBlockHeight = BigInteger.ZERO;
    this.trxAttemptListStorage = new TransactionAttemptListStorageService();
    this.wavesRpcClient = getNodeObj();
  }


  @SneakyThrows
  public Node getNodeObj()
  {
    return new Node(this.wavesConfig.getNode());
  }

  @Override
  public String getChainPublicGatewayAddress()
  {
    return this.wavesConfig.getGatewayAddress();
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
  public String getChainIdentifier()
  {
    return this.wavesConfig.getChainIdentifier();
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
  public Boolean filterTransactions(Transaction trx)
  {

    if (this.trxAttemptListStorage.gatewayTransactionExists(trx.getTransactionID())) return false;
    else return !_filterTransactionReceivers(trx).isEmpty();
  }


  /*
   * FIXME: Use TransactionSender Model from {bridge.services.transactionservice.TransactionModel}
   * */
  @Override
  public void handleTransaction(Transaction trx)
  {
    var receivers = this._filterTransactionReceivers(trx);

    //FIXME: this may not  be necessary
    if (receivers.size() > 1)
      throw new BridgeExceptions.MultipleGateWayReceiverException(trx.getTransactionID());


    for (var tx : receivers)
      this._handleTransaction(trx.getTransactionID(), trx.getReceivers().get(tx), tx);
  }

  @Override
  public String getChainName()
  {
    return this.wavesConfig.getChainName();
  }

  @Override
  public Transaction getTransaction(String trxID)
  {

    Config transaction = BridgeUtils.getJsonDeserializer().parse(getTrxByID(trxID));

    String assetId = null;
    String assetName = null;

    if (Integer.parseInt(transaction.get("type")) != 4) return null;

    //Todo: Optimised this block of code
    if (!transaction.contains("assetId"))
    {
      return null;
    }
    else
    {
      for (var asseti : this.wavesConfig.getAssets())
      {
        String waveAssetId = Objects.requireNonNull(asseti.getAssetId(),
            "asset Id not defined asset id for waves chain cannot be null");
        if (waveAssetId.equals(transaction.get("assetId")))
        {
          assetId = waveAssetId;
          assetName = asseti.getAssetName();
          break;
        }
        else
        {
          assetId = null;
        }
      }
      if (assetId == null) return null;
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

    senders.add(new TransactionSender(new TransactionModels.MappedAddress(sender, assetName)));
    recipients.add(
        new TransactionReceiver(new TransactionModels.MappedAddress(receiver, assetName), amount));
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
  private List<Integer> _filterTransactionReceivers(Transaction trx)
  {
    List<Integer> result = new ArrayList<>();
    List<TransactionReceiver> receivers = trx.getReceivers();
    for (int i = 0; i <= receivers.size(); i++)
    {
      if (receivers.get(i).getAddress().getAddress().equals(this.wavesConfig.getGatewayAddress()))
      {
        var trx_attempt = this.trxAttemptListStorage.findTransactionAttemptByTrigger(
            new TransactionModels.TransactionAttemptListTrigger(trx.getTransactionID(), i,
                receivers.get(i).getAddress().getAssetName()));
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
          receiver.getAddress().getAssetName());

      List<TransactionModels.TransactionAttempt> attempts = new ArrayList<>();
      List<TransactionModels.TransactionAttemptReceiver> tokenReceiversList = new ArrayList<>();

      String tokenReceiver = this.getTokenReceiverFromTransaction(transactionId);
      AddressValidator.addressToValidate.set(tokenReceiver);
      String sender = AddressValidator.gatewayAddress.get();
      tokenReceiversList.add(
          new TransactionModels.TransactionAttemptReceiver(tokenReceiver, receiver.getAmount()));

      attempts.add(
          new TransactionModels.TransactionAttempt(sender, 0, receiver.getAddress().getAssetName(),
              tokenReceiversList));

      attempt_list = new TransactionModels.TransactionAttemptList(trigger, attempts,
          ZonedDateTime.now(ZoneId.of("Etc/Zulu")), ZonedDateTime.now(ZoneId.of("Etc/Zulu")));
      this.trxAttemptListStorage.saveTransactionAttemptList(attempt_list);
      log.info("Created new attempt list " + attempt_list);
    }
  }
}
