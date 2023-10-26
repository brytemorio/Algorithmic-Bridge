package bridge.services.disruptorservice.eventhandlers;

import bridge.blockchains.IBaseChain;
import bridge.services.transactionservice.TransactionAttemptListService;
import bridge.services.transactionservice.TransactionModels;
import com.lmax.disruptor.EventHandler;
import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;

@Slf4j
public class AttempListEventHandler implements
    EventHandler<TransactionModels.TransactionAttemptList>
{
  private final IBaseChain chain;

  public AttempListEventHandler(IBaseChain chain)
  {
    this.chain = chain;
  }

  @Override
  public void onEvent(TransactionModels.TransactionAttemptList event, long sequence,
                      boolean endOfBatch)
  {
    try
    {
      TransactionAttemptListService attemptListService = new TransactionAttemptListService(
          this.chain);
      attemptListService.continueTrxAttemptList(event);
    }
    catch (Exception exp)
    {
      exp.printStackTrace();
    }
  }
}
