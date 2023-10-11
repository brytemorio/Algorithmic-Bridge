package bridge.services.transactionservice;

import bridge.blockchains.IBaseChain;
import bridge.services.storagservice.DataObjects;
import bridge.services.storagservice.TransactionAttemptListStorageService;
import bridge.services.storagservice.TransactionPollingStateStorageService;
import com.wavesplatform.wavesj.exceptions.NodeException;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.stream.Collectors;

import static bridge.services.transactionservice.TransactionModels.Transaction;


@Slf4j
public class TransactionPollingService
{
  private final TransactionAttemptListStorageService trxAttemptListStorage;
  private final TransactionPollingStateStorageService transactionPollingStateStorageService;
  private final IBaseChain blockChain;
  private String assetName;
  //private final DataObjects.PollingTransactionState pollingTransactionState;
  private DataObjects.PollingState pollingState;

  /*total number of retries for handling transaction before it is marked as failed*/
  private final int MAX_RETRIES = 5;

  public TransactionPollingService(IBaseChain blockChain)
  {
    this.blockChain = blockChain;
    this.trxAttemptListStorage = new TransactionAttemptListStorageService();
    //this.pollingTransactionState = new DataObjects.PollingTransactionState();
    this.pollingState = new DataObjects.PollingState(this.blockChain.getChainName());
    this.transactionPollingStateStorageService = new TransactionPollingStateStorageService();
  }


  public void run(ArrayList<String> trxid)
  {
    var transactions = getTransactionList(trxid);

    if (transactions.isEmpty())
    {
      //FIXME: Remove if block. Used for the debugging
      if (blockChain.getChainName().equals("waves")) log.info("no bridge transaction found");
      return;
    }

    this.transactionPollingStateStorageService.getTransactionPollingState(
        this.blockChain.getChainName());


    var filteredTrx = filterTransactions(transactions);

    //FIXME: Remove logger or change to debug leve
    log.info("Filtered Transactions: " + Arrays.toString(filteredTrx.toArray()));

    try
    {
      //FIXME: Remove logger or change to debug leve
      log.info("Handling filtered transactions");
      filteredTrx.forEach(this::handleTransaction);
      resetPollingState();
    } finally
    {
      this.updateStoredPollingState();
    }
  }


  private void handleTransaction(Transaction trx)
  {
    log.info("Polling transaction from " + this.blockChain.getChainName());
    this.ensurePollingStateHasTransaction(trx);

    try
    {
      this.blockChain.handleTransaction(trx);
      this.pollingState.getTransactionMap().get(trx.getTransactionID()).markAsDone();
    }
    catch (RuntimeException ex)
    {
      log.error(ex.getMessage());
      this.pollingState.getTransactionMap().get(trx.getTransactionID()).incrementTries();
    }
  }

  @SneakyThrows
  private List<Transaction> getTransactionList(ArrayList<String> trx)
  {
    /*return trx.stream().map(this.blockChain::getTransaction).filter(Objects::nonNull)
        .collect(Collectors.toList());*/

    ArrayList<Transaction> validTrxs = new ArrayList<>();
    for (String trxid : trx)
    {
      try
      {
        var trxi = this.blockChain.getTransaction(trxid);
        if (trxi != null) validTrxs.add(trxi);
      }
      catch (NodeException exception)
      {
        if (exception.getErrorCode() == 311) continue;
        else throw exception;
      }
    }
    return validTrxs;
  }

  private void resetPollingState()
  {
    this.pollingState = new DataObjects.PollingState(this.blockChain.getChainName());
  }

  private void updateStoredPollingState()
  {
    this.transactionPollingStateStorageService.setTransactionPollingState(this.pollingState);
  }

  private boolean trxNotExceedRetries(Transaction trx)
  {
    return this.pollingState.getTransactionMap().get(trx.getTransactionID())
        .getTries() > this.MAX_RETRIES;
  }

  private boolean isTrxNotAlreadyProcessed(Transaction trx)
  {
    return this.pollingState.getTransactionMap().get(trx.getTransactionID()).isOk();
  }

  private boolean shouldTrxBeProcessed(Transaction trx)
  {

    return (!(isTrxNotAlreadyProcessed(trx) && trxNotExceedRetries(
        trx))) && this.blockChain.filterTransactions(trx);
  }

  private void ensurePollingStateHasTransaction(Transaction trx)
  {
    Map<String, DataObjects.PollingTransactionState> pollingTransactionStateMap = new HashMap<>();
   /* if (this.pollingState.getTransactionMap().get(trx.getTransactionID()) == null)
    {

    }*/
    pollingTransactionStateMap.put(trx.getTransactionID(),
        new DataObjects.PollingTransactionState());
    this.pollingState.setTransactionMap(pollingTransactionStateMap);

  }

  private List<Transaction> filterTransactions(List<Transaction> transactionList)
  {
    List<Transaction> filteredTrx = new ArrayList<>();
    transactionList.forEach(trx -> {
      ensurePollingStateHasTransaction(trx);
      if (shouldTrxBeProcessed(trx)) filteredTrx.add(trx);
    });
    return filteredTrx;
  }


}
