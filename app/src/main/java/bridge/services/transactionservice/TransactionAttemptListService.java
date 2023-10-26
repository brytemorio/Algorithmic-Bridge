package bridge.services.transactionservice;

import bridge.blockchains.IBaseChain;
import bridge.services.storagservice.TransactionAttemptListStorageService;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@NoArgsConstructor
public class TransactionAttemptListService
{

  private TransactionAttemptListStorageService dbService;
  private IBaseChain chain;

  public TransactionAttemptListService(IBaseChain chain)
  {
    this.chain = chain;
    this.dbService = new TransactionAttemptListStorageService();
  }

  @SneakyThrows
  public void continueTrxAttemptList(TransactionModels.TransactionAttemptList trxAttemptList)
  {
    log.info("Tyring to complete attempt_list  " + trxAttemptList.getTransactionAttemptID());
    if (trxAttemptList.hasCompleted())
    {
      log.info("attempt list is already completed");
      return;
    }

    /*for (int i = 0; i <= trxAttemptList.getTransactions().size(); i++)
      logAttemptAlreadyDone(trxAttemptList.getAttempts().get(i));*/

    while (!trxAttemptList.hasCompleted())
    {
      var nextAttempt = trxAttemptList.nextIncompleteAttempt();
      var transaction = this.chain.sendCoin(nextAttempt);
      trxAttemptList.markNextAttemptAsCompleted(nextAttempt, transaction.getTransactionID());
      this.dbService.updateTransactionAttemptList(trxAttemptList);
      logAttemptSuccess(nextAttempt);
    }
    log.info(trxAttemptList.getTransactionAttemptID() + " is completed");
  }


  private void logAttemptSuccess(TransactionModels.TransactionAttempt attempt)
  {
    for (var receivers : attempt.getReceivers())
    {

      log.info(
          "[" + attempt.getCurrency() + "]" + " : Transferred " + receivers.getAmount() + " from " + attempt.getSender() + " to " + receivers.getAddress());
    }
  }

  private void logAttemptAlreadyDone(TransactionModels.TransactionAttempt attempt)
  {
    for (var receivers : attempt.getReceivers())
    {

      log.info(
          "[" + attempt.getCurrency() + "]" + " : Already transferred " + receivers.getAmount() + " from " + attempt.getSender() + " to " + receivers.getAddress());
    }
  }
}
