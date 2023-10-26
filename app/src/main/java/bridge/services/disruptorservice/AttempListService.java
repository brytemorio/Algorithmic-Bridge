package bridge.services.disruptorservice;

import bridge.blockchains.IBaseChain;
import bridge.services.disruptorservice.eventfactory.AttempListEventFactory;
import bridge.services.disruptorservice.eventhandlers.AttempListEventHandler;
import bridge.services.storagservice.TransactionAttemptListStorageService;
import bridge.services.transactionservice.TransactionModels;
import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.dsl.Disruptor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
public class AttempListService
{
  private final RingBuffer<TransactionModels.TransactionAttemptList> ringBuffer;
  private final TransactionAttemptListStorageService transactionAttemptListStorageService;

  public AttempListService(IBaseChain chain)
  {
    int CAPACITY = 2048;
    Disruptor<TransactionModels.TransactionAttemptList> disruptor = new DisruptorObjFactory<>(
        new AttempListEventHandler(chain), new AttempListEventFactory(), CAPACITY);
    disruptor.start();
    this.ringBuffer = disruptor.getRingBuffer();
    this.transactionAttemptListStorageService = new TransactionAttemptListStorageService();
  }


  Runnable doRun = new Runnable()
  {
    @Override
    @SneakyThrows
    public void run()
    {
      for (; ; )
      {
        var trxAttemptStorage = new TransactionAttemptListStorageService();
        var nextTransactionAttempt = trxAttemptStorage.findOldestPendingAttemptList();

        //TODO: remove the logging
        log.info(nextTransactionAttempt.toString());

        nextTransactionAttempt.incrementRetries();
        trxAttemptStorage.updateTransactionAttemptList(nextTransactionAttempt);
        var sequence = ringBuffer.next();
        var trxAttempt = ringBuffer.get(sequence);
        trxAttempt.setId(nextTransactionAttempt.getId());
        trxAttempt.setTrigger(nextTransactionAttempt.getTrigger());
        trxAttempt.setAttempts(nextTransactionAttempt.getAttempts());
        trxAttempt.setTransactions(nextTransactionAttempt.getTransactions());
        trxAttempt.setCreatedOn(nextTransactionAttempt.getCreatedOn());
        trxAttempt.setLastModifiedOn(nextTransactionAttempt.getLastModifiedOn());
        trxAttempt.setTries(nextTransactionAttempt.getTries());
        trxAttempt.setTransactionAttemptID(nextTransactionAttempt.getTransactionAttemptID());
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
