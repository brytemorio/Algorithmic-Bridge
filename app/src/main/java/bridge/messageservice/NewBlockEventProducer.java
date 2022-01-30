package bridge.messageservice;

import bridge.common.IBaseChain;
import com.lmax.disruptor.EventFactory;
import com.lmax.disruptor.RingBuffer;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public final class NewBlockEventProducer {
  private static NewBlockEventProducer newBlockEventProducer;
  private static RingBuffer<BlockProp> ringBuffer;
  private IBaseChain[] blockChains;
  private ExecutorService threadExecutor;

  private NewBlockEventProducer(final IBaseChain... cblockChains) {
    this.blockChains = cblockChains;
  }

  /**
   * The parameter passed to {@code getNewBlockEventProducer} are instance of (Classes or the
   * SubClasses of Classes) that implements the interface {@link bridge.common.IBaseChain}
   *
   * @param blockchains instances of IBaseChain
   */
  public static synchronized NewBlockEventProducer getNewBlockEventProducer(
      final IBaseChain... cblockChains) {
    if (!Objects.nonNull(newBlockEventProducer)) {
      newBlockEventProducer =
          new NewBlockEventProducer(
              Objects.requireNonNull(
                  cblockChains,
                  "Cannot construct object NewBlockEventProducer without the required"
                      + " parameter(s)"));
    } else {
      log.trace("An instance of NewBlockEventProducer already exits");
    }
    return newBlockEventProducer;
  }

  public void start() {
    BlockDispatchService dispatchService =
        BlockDispatchService.getNewBlockDispatchService(blockChains);
    NewBlockEventFactory blockEventFactory = new NewBlockEventFactory();
    Integer bufferSize = 4096;
    DisruptorObjFactory<BlockProp> disruptor =
        new DisruptorObjFactory<>(dispatchService, blockEventFactory, bufferSize);
    disruptor.start();
    ringBuffer = disruptor.getRingBuffer();

    threadExecutor = Executors.newFixedThreadPool(blockChains.length);
    for (int index = 0; index < blockChains.length; index++) {
      threadExecutor.execute(new ProducerInitializer(blockChains[index]));
    }
  }

  public void stop() {
    threadExecutor.shutdown();
  }

  class ProducerInitializer implements Runnable {
    private IBaseChain blockchain;

    public ProducerInitializer(final IBaseChain blockchain) {
      this.blockchain = blockchain;
    }

    @Override
    public void run() {

      /*
       * TODO Add an keyboard event handler for gracefully shutting down this loop e.g
       * using (CTRL-C)
       */
      for (; ; ) {
        long sequence = ringBuffer.next();
        BlockProp newBlockHeight = ringBuffer.get(sequence);
        newBlockHeight.setChainIdentifier(blockchain.getChainIdentifier());
        newBlockHeight.setBlockHeight(blockchain.getBlockHeight());
        ringBuffer.publish(sequence);
      }
    }
  }

  class NewBlockEventFactory implements EventFactory<BlockProp> {
    @Override
    public BlockProp newInstance() {
      return new BlockProp();
    }
  }
}
