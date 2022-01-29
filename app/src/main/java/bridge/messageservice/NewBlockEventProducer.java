package bridge.messageservice;

import bridge.common.IBaseChain;
import com.lmax.disruptor.EventFactory;
import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.util.DaemonThreadFactory;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/*
TODO => Write Code to proberly handle IBaseChain[] past to NewBlockEventProducer.start() method.
TODO => Use a combo of RxJava, Threads, Channels to  coordinate the request for / keep up with
      new blockheigts  among all blockchains in  NewBlockEventProducer.start() or refactor that
      into it's own sub private function;
 */

public class NewBlockEventProducer {
  private RingBuffer<BlockProp> ringBuffer;
  private IBaseChain[] blockChains;

  /**
   * The parameter passed to {@code NewBlockEventProducer} are instance of Classes or the SubClasses
   * of Classes that implements the interface {@link bridge.common.IBaseChain}
   *
   * @param blockchains instances of IBaseChain
   */
  public NewBlockEventProducer(final IBaseChain... cblockChains) {
    if (Objects.nonNull(cblockChains)) {
      this.blockChains = cblockChains;
    } else {
      throw new NullPointerException(
          "Cannot construct object NewBlockEventProducer without the required parameter(s)");
    }
  }

  public void start() {
    ExecutorService threadExecutor = Executors.newFixedThreadPool(blockChains.length);
    for (int index = 0; index < blockChains.length; index++) {
      threadExecutor.execute(new ProducerInitializer(blockChains[index]));
    }
  }

  class NewBlockEventFactory implements EventFactory<BlockProp> {
    @Override
    public BlockProp newInstance() {
      return new BlockProp();
    }
  }

  class ProducerInitializer implements Runnable {
    private IBaseChain blockchain;
    private RingBuffer<BlockProp> ringBuffer;

    public ProducerInitializer(final IBaseChain blockchain) {
      this.blockchain = blockchain;
    }

    @Override
    public void run() {
      int buffersize = 4096;
      NewBlockEventFactory newBlockEventFactory = new NewBlockEventFactory();
      Disruptor<BlockProp> disruptor =
          new Disruptor<>(newBlockEventFactory, buffersize, DaemonThreadFactory.INSTANCE);
      disruptor.handleEventsWith(new BlockDispatchService());
      disruptor.start();

      ringBuffer = disruptor.getRingBuffer();
      // NewBlockEventProducer producer = new NewBlockEventProducer(ringBuffer);

      // TODO Add an keyboard event handler for gracefully shutting down this loop
      // (CTRL-C)
      for (; ; ) {
        long sequence = ringBuffer.next();
        BlockProp newBlockHeight = ringBuffer.get(sequence);
        newBlockHeight.setChainIdentifier(blockchain.getChainIdentifier());
        newBlockHeight.setBlockHeight(blockchain.getBlockHeight());
        ringBuffer.publish(sequence);
      }
    }
  }
}
