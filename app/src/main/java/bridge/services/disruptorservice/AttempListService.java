package bridge.services.disruptorservice;

import bridge.blockchains.IBaseChain;
import bridge.services.disruptorservice.eventfactory.AttempListEventFactory;
import bridge.services.disruptorservice.eventhandlers.AttempListEventHandler;
import bridge.services.storagservice.TransactionAttemptListStorageService;
import bridge.services.transactionservice.TransactionModels;
import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.dsl.Disruptor;
import lombok.SneakyThrows;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AttempListService
{
  private final RingBuffer<TransactionModels.TransactionAttemptList> ringBuffer;

  public AttempListService(IBaseChain chain)
  {
    int CAPACITY = 4069;
    Disruptor<TransactionModels.TransactionAttemptList> disruptor = new DisruptorObjFactory<>(
        new AttempListEventHandler(chain), new AttempListEventFactory(), CAPACITY);
    disruptor.start();
    this.ringBuffer = disruptor.getRingBuffer();
  }

  Runnable doRun = new Runnable()
  {
    @Override
    @SneakyThrows
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
    try (ExecutorService executorService = Executors.newSingleThreadExecutor())
    {
      executorService.execute(doRun);
    }
  }
}
