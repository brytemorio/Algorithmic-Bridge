package bridge;

import bridge.common.IBaseChain;
import com.lmax.disruptor.EventFactory;
import com.lmax.disruptor.EventHandler;
import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.util.DaemonThreadFactory;
import org.agrona.collections.Object2ObjectHashMap;
import org.jetbrains.annotations.NotNull;

/**
 * This classs is just a temporary hack
 *
 * @author bryte
 */
public class NewBlockEventProducer {
  private RingBuffer<BlockProp> ringBuffer;

  public NewBlockEventProducer() {}

  // Constructor is use internally
  private NewBlockEventProducer(RingBuffer<BlockProp> ringBuffer) {
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
    newBlockHeight.setProp(blockchain);
    ringBuffer.publish(sequence);
  }

  private static class NewBlockEventHandler implements EventHandler<BlockProp> {
    @Override
    public void onEvent(BlockProp event, long sequence, boolean endOfBatch) throws Exception {
      System.out.println("Event: " + event.getProp().stream().iterator().next().getValue());
    }
  }

  private static class NewBlockEventFactory implements EventFactory<BlockProp> {
    @Override
    public BlockProp newInstance() {
      return new BlockProp();
    }
  }

  private static class BlockProp {
    private final Object2ObjectHashMap<Integer, String> newBlockProp = new Object2ObjectHashMap<>();

    public void setProp(@NotNull IBaseChain blockChain) {
      newBlockProp.put(blockChain.getBlockHeight(), blockChain.getChainIdentifier());
    }

    public Object2ObjectHashMap<Integer, String>.EntrySet getProp() {
      return newBlockProp.entrySet();
    }
  }
}
