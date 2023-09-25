package bridge.transactionservice;

import bridge.blockchains.IBaseChain;
import bridge.storagservice.MongoStorageService;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@NoArgsConstructor
public class TransactionAttemptListService
{

  private MongoStorageService dbService;
  private IBaseChain chain;
  public TransactionAttemptListService(IBaseChain chain)
  {
    this.chain = chain;
    this.dbService = new MongoStorageService();
  }

  public void continueTrxAttemptList(TransactionModels.TransactionAttemptList trxAttemptList)
  {
    log.info("Tyring to complete attempt_list '%s'" + trxAttemptList.getTransactionAttemptID());
    if (trxAttemptList.hasCompleted())
    {
      log.info("attempt list is already completed");
      return;
    }

    for(int i =0; i <= trxAttemptList.getTransactions().size(); i++)
      logAttemptAlreadyDone(trxAttemptList.getAttempts().get(i));

    while(!trxAttemptList.hasCompleted())
    {
      var nextAttempt = trxAttemptList.nextIncompleteAttempt();
      var transaction = this.chain.sendCoin(nextAttempt);
      trxAttemptList.markNextAttemptAsCompleted(nextAttempt, transaction.getTransactionID());
      this.dbService.updateTransactionAttempt(trxAttemptList);
      logAttemptSuccess(nextAttempt);
    }
    log.info(trxAttemptList.getTransactionAttemptID() + " is completed");
  }



  private void logAttemptSuccess(TransactionModels.TransactionAttempt attempt)
  {
    for (var receivers : attempt.getReceivers())
    {

      log.info("[" + attempt.getCurrency() + "]" + " : Transferred " + receivers.getAmount()
          .toString() + " from " + attempt.getSender() + " to " + receivers.getAddress());
    }
  }

  private void logAttemptAlreadyDone(TransactionModels.TransactionAttempt attempt)
  {
    for (var receivers : attempt.getReceivers())
    {

      log.info("[" + attempt.getCurrency() + "]" + " : Already transferred " + receivers.getAmount()
          .toString() + " from " + attempt.getSender() + " to " + receivers.getAddress());
    }
  }
}
