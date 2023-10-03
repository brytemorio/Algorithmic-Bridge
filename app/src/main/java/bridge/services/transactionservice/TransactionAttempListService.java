package bridge.services.transactionservice;

import bridge.services.storagservice.TransactionAttemptListStorageService;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;

@Slf4j
public class TransactionAttempListService
{
  private TransactionAttemptListStorageService attemptListStorageService;
  private ArrayList<String> trxID;

  public TransactionAttempListService()
  {

  }

  public void continueTrxAttempt(TransactionModels.TransactionAttemptList attemptList)
  {
    log.info("Trying to complete transaction:  " + attemptList.getTransactionAttemptID());
    if(attemptList.hasCompleted())
    {
      log.info("Current transaction has been completed");
      return;
    }
  }

}
