package bridge.services.disruptorservice;

import bridge.blockchains.IBaseChain;
import bridge.services.disruptorservice.eventfactory.AttempListEventFactory;
import bridge.services.disruptorservice.eventhandlers.AttempListEventHandler;
import bridge.services.storagservice.TransactionAttemptListStorageService;
import bridge.services.transactionservice.TransactionModels;
import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.dsl.Disruptor;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AttempListService
{
  private final IBaseChain chain;
  private final RingBuffer<TransactionModels.TransactionAttemptList> ringBuffer;
  private final int CAPACITY = 4069;
  private final Disruptor<TransactionModels.TransactionAttemptList> disruptor;

  public AttempListService(IBaseChain chain)
  {
    this.chain = chain;
    this.disruptor = new DisruptorObjFactory<>(new AttempListEventHandler(this.chain),
        new AttempListEventFactory(), CAPACITY);
    this.disruptor.start();
    this.ringBuffer = this.disruptor.getRingBuffer();
  }

  Runnable doRun = new Runnable()
  {
    @Override
    public void run()
    {
      for (; ; )
      {
        var nextTransactionAttempt = new TransactionAttemptListStorageService().findOldestPendingAttemptList();
        var sequence = ringBuffer.next();
        var trxAttempt = ringBuffer.get(sequence);
        trxAttempt = nextTransactionAttempt;
        ringBuffer.publish(sequence);
      }
    }
  };

  public void run()
  {
    ExecutorService executorService = Executors.newSingleThreadExecutor();
    executorService.execute(doRun);

  }

}
