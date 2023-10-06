package bridge.services.storagservice;

import bridge.services.transactionservice.TransactionModels;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TransactionAttemptListService
{
  public void continueAttemptListTransaction(TransactionModels.TransactionAttemptList attemptList)
  {
    log.info("Trying to complete attempt list " + attemptList.getTransactionAttemptID());
    if (attemptList.hasCompleted())
    {
      log.info("attempt list is already completed");
      return;
    }
  }

  private static void logAttemptListAlreadyDone(TransactionModels.TransactionAttempt attempt)
  {
    for (var receiver : attempt.getReceivers())
    {
      log.info(
          attempt.getCurrency() + ": Already transfered " + receiver.getAmount() + "from " +
              attempt.getSender() + "to " + receiver.getAddress());
    }
  }
}
