package bridge.transactionservice;

import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@NoArgsConstructor
public class TransactionAttemptListService
{

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
      //Todo: Complete this function
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
