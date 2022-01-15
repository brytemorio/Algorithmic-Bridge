package bridge;

import bridge.blockchains.waves.WavesChainI;
import bridge.common.BaseBlockChain;
import com.lmax.disruptor.EventFactory;
import com.lmax.disruptor.EventHandler;
import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.util.DaemonThreadFactory;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Setter;
import lombok.SneakyThrows;

/**
 * This classs is just a temporary hack
 *
 * @author bryte
 */
public class NewBlockEventProducer {
  private RingBuffer<BlockProp> ringBuffer;

  public NewBlockEventProducer() {}

  public NewBlockEventProducer(RingBuffer<BlockProp> ringBuffer) {
    this.ringBuffer = ringBuffer;
  }

  /** @param blockchains */
  public void start(BaseBlockChain... blockchains) {
    int buffersize = 4096;
    NewBlockEventFactory newBlockEventFactory = new NewBlockEventFactory();
    Disruptor<BlockProp> disruptor =
        new Disruptor<>(newBlockEventFactory, buffersize, DaemonThreadFactory.INSTANCE);
    disruptor.handleEventsWith(new NewBlockEventHandler());
    disruptor.start();

    ringBuffer = disruptor.getRingBuffer();
    NewBlockEventProducer producer = new NewBlockEventProducer(ringBuffer);

    for (int i = 0; true; i++) {
      producer.onData();
    }
  }

  private void onData() {
    long sequence = ringBuffer.next();
    BlockProp newBlockHeight = ringBuffer.get(sequence);
    ringBuffer.publish(sequence);
  }

  private static class NewBlockEventHandler implements EventHandler<BlockProp> {
    @Override
    public void onEvent(BlockProp event, long sequence, boolean endOfBatch) throws Exception {
      System.out.println("Event: " + event.blockHeight + "  " + event.chainIdentifier);
    }
  }

  private static class NewBlockEventFactory implements EventFactory<BlockProp> {
    private final WavesChainI<?> wavesChainI;

    @SneakyThrows
    NewBlockEventFactory() {
      this.wavesChainI = new WavesChainI<>("qnodecoin");
    }

    @Override
    public BlockProp newInstance() {
      return new BlockProp(wavesChainI.getBlockHeight(), wavesChainI.getChainIdentifier());
    }
  }

  @Data
  private static class BlockProp {
    @Setter(AccessLevel.NONE)
    private Integer blockHeight;

    @Setter(AccessLevel.NONE)
    private String chainIdentifier;

    BlockProp(Integer blockHeight, String chainIdentifier) {
      this.blockHeight = blockHeight;
      this.chainIdentifier = chainIdentifier;
    }
  }
}
