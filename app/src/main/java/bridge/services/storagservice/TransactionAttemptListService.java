package bridge.services.storagservice;

import bridge.blockchains.IBaseChain;
import bridge.services.transactionservice.TransactionModels;
import com.lmax.disruptor.EventHandler;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.agrona.concurrent.UnsafeBuffer;
import org.agrona.concurrent.ringbuffer.OneToOneRingBuffer;
import org.agrona.concurrent.ringbuffer.RingBuffer;
import org.agrona.concurrent.ringbuffer.RingBufferDescriptor;

import java.nio.ByteBuffer;

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
  public void run()
  {
    int capacity = 4096 + RingBufferDescriptor.TRAILER_LENGTH;
    UnsafeBuffer interBuffer = new UnsafeBuffer(ByteBuffer.allocateDirect(capacity));
    OneToOneRingBuffer ringBuffer = new OneToOneRingBuffer(interBuffer);
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
