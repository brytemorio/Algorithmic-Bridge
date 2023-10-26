package bridge.services.disruptorservice.eventfactory;

import bridge.services.storagservice.TransactionAttemptListStorageService;
import bridge.services.transactionservice.TransactionModels;
import com.lmax.disruptor.EventFactory;

public class AttempListEventFactory implements EventFactory<TransactionModels.TransactionAttemptList>
{
  @Override
  public TransactionModels.TransactionAttemptList newInstance()
  {
    return new TransactionModels.TransactionAttemptList();
  }
}
