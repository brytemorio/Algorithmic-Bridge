package bridge.messageservice;

import bridge.common.IBaseChain;
import com.lmax.disruptor.EventFactory;
import com.lmax.disruptor.EventHandler;
import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.util.DaemonThreadFactory;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Setter;

/*
TODO => Write Code to proberly handle IBaseChain[] past to NewBlockEventProducer.start() method.
TODO => Use a combo of RxJava, Threads, Channels to  coordinate the request for / keep up with
      new blockheigts  among all blockchains in  NewBlockEventProducer.start() or refactor that
      into it's own sub private function;
 */
public class NewBlockEventProducer {
  private RingBuffer<BlockProp> ringBuffer;

  public NewBlockEventProducer() {}

  // Constructor is use internally
  public NewBlockEventProducer(RingBuffer<BlockProp> ringBuffer) {
    this.ringBuffer = ringBuffer;
  }

  /**
   * The parameter passed to {@code start()} are instance of Classes or the SubClasses of Classes
   * that implements the interface {@link bridge.common.IBaseChain}
   *
   * @param blockchains instances of BaseBlockchain
   */
  public void start(IBaseChain... blockchains) {
    int buffersize = 4096;
    NewBlockEventFactory newBlockEventFactory = new NewBlockEventFactory();
    Disruptor<BlockProp> disruptor =
        new Disruptor<>(newBlockEventFactory, buffersize, DaemonThreadFactory.INSTANCE);
    disruptor.handleEventsWith(new NewBlockEventHandler());
    disruptor.start();

    ringBuffer = disruptor.getRingBuffer();
    NewBlockEventProducer producer = new NewBlockEventProducer(ringBuffer);

    for (int i = 0; true; i++) {
      producer.onData(blockchains[0]);
    }
  }

  private void onData(IBaseChain blockchain) {
    long sequence = ringBuffer.next();
    BlockProp newBlockHeight = ringBuffer.get(sequence);
    newBlockHeight.setChainIdentifier(blockchain.getChainIdentifier());
    newBlockHeight.setBlockHeight(blockchain.getBlockHeight());
    ringBuffer.publish(sequence);
  }

  private static class NewBlockEventHandler implements EventHandler<BlockProp> {
    @Override
    public void onEvent(BlockProp event, long sequence, boolean endOfBatch) throws Exception {
      System.out.println("Event: " + event.getChainIdentifier() + " => " + event.getBlockHeight());
    }
  }

  private static class NewBlockEventFactory implements EventFactory<BlockProp> {
    @Override
    public BlockProp newInstance() {
      return new BlockProp();
    }
  }

  @Data
  private static class BlockProp<T> {

    @Setter(AccessLevel.PROTECTED)
    private String chainIdentifier;

    @Setter(AccessLevel.PROTECTED)
    private T blockHeight;

    public BlockProp() {}

    public BlockProp(String chainIdentifier, T blockHeight) {
      this.chainIdentifier = chainIdentifier;
      this.blockHeight = blockHeight;
    }
  }
}
