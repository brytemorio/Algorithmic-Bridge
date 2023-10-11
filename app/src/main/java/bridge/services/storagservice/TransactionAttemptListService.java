package bridge.services.storagservice;

import bridge.blockchains.IBaseChain;
import bridge.services.transactionservice.TransactionModels;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TransactionAttemptListService
{
  private final IBaseChain blockchain;
  private final TransactionAttemptListStorageService trxAttempListStorageService;

  public TransactionAttemptListService(IBaseChain blockchain)
  {
    this.blockchain = blockchain;
    this.trxAttempListStorageService = new TransactionAttemptListStorageService();
  }

  @SneakyThrows
  public void continueAttemptListTransaction(TransactionModels.TransactionAttemptList attemptList)
  {
    log.info("Trying to complete attempt list " + attemptList.getTransactionAttemptID());
    if (attemptList.hasCompleted())
    {
      log.info("attempt list is already completed");
      return;
    }

    for (int i = 0; i < attemptList.getTransactions().size(); i++)
      logAttemptListAlreadyDone(attemptList.getAttempts().get(i));

    while (!attemptList.hasCompleted())
    {
      var nextAttempt = attemptList.nextIncompleteAttempt();
      var transaction = this.blockchain.sendCoin(nextAttempt);
      attemptList.markNextAttemptAsCompleted(nextAttempt, transaction.getTransactionID());
      this.trxAttempListStorageService.updateTransactionAttemptList(attemptList);
      logAttemptSuccess(nextAttempt);
    }

    log.info("Attemp list " + attemptList.getTransactionAttemptID() + " is complete");
  }

  private static void logAttemptListAlreadyDone(TransactionModels.TransactionAttempt attempt)
  {
    for (var receiver : attempt.getReceivers())
    {
      log.info(
          attempt.getCurrency() + ": Already transferred " + receiver.getAmount() + "from "
              + attempt.getSender() + "to " + receiver.getAddress());
    }
  }

  private static void logAttemptSuccess(TransactionModels.TransactionAttempt attempt)
  {
    for (var receiver : attempt.getReceivers())
    {
      log.info(
          attempt.getCurrency() + ": Transferred " + receiver.getAmount() + "from "
              + attempt.getSender() + "to " + receiver.getAddress());
    }
  }
}
