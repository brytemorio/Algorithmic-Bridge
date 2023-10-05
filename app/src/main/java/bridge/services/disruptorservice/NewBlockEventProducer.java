package bridge.services.disruptorservice;

import bridge.blockchains.IBaseChain;
import bridge.common.BridgeUtils;
import bridge.exceptions.BridgeExceptions.ObjectCreationException;
import com.lmax.disruptor.EventFactory;
import com.lmax.disruptor.RingBuffer;
import java.math.BigInteger;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public final class NewBlockEventProducer {
  private static NewBlockEventProducer newBlockEventProducer;
  private static RingBuffer<BlockProp> ringBuffer;
  private final IBaseChain[] blockChains;
  private ExecutorService threadExecutor;

  private NewBlockEventProducer(final IBaseChain... cblockChains) {
    this.blockChains = cblockChains;
  }

  /**
   * The parameter passed to {@code getNewBlockEventProducer} are instance of (Classes or the
   * SubClasses of Classes) that implements the interface {@link bridge.blockchains.IBaseChain}
   *
   * @param cblockChains instances of IBaseChain
   */
  @SneakyThrows
  public static synchronized NewBlockEventProducer getNewBlockEventProducer(
      final IBaseChain... cblockChains) {
    String className = NewBlockEventProducer.class.getName();
    if (newBlockEventProducer == null) {
      newBlockEventProducer =
          new NewBlockEventProducer(
              Objects.requireNonNull(
                  cblockChains,
                  "Cannot construct object "
                      + className
                      + " without the required"
                      + " parameter(s)"));
    } else {
      throw new ObjectCreationException("An instance of " + className + " already exits");
    }
    return newBlockEventProducer;
  }

  public void start() {
    Extractor dispatchService = Extractor.getExtractorObj(blockChains);
    NewBlockEventFactory blockEventFactory = new NewBlockEventFactory();
    Integer bufferSize = 1024;
    DisruptorObjFactory<BlockProp> disruptor =
        new DisruptorObjFactory<>(
            dispatchService,
            blockEventFactory,
            bufferSize
            );
    disruptor.start();
    ringBuffer = disruptor.getRingBuffer();

    threadExecutor = Executors.newFixedThreadPool(BridgeUtils.getCPUCounts());

    for (IBaseChain blockChain : blockChains)
    {
      threadExecutor.execute(new ProducerInitializer(blockChain));
      //new ProducerInitializer(blockChain).run();
    }
  }

  public void stop() {
    threadExecutor.shutdown();
  }

  static class ProducerInitializer implements Runnable {
    private final IBaseChain blockchain;

    public ProducerInitializer(final IBaseChain blockchain) {
      this.blockchain = blockchain;
    }

    @SuppressWarnings("static-access")
    @Override
    public void run() {

      /*
       * TODO Add an keyboard event handler for gracefully shutting down this loop e.g
       * using (CTRL-C)
       */
      for (; ; ) {
        String chainIdentifier = blockchain.getChainIdentifier();
        String chainName = blockchain.getChainName();
        BigInteger chainHeight = null;
        try
        {
          chainHeight = blockchain.getBlockHeight();
        }
        catch (Exception e)
        {
          throw new RuntimeException(e);
        }
        long sequence = ringBuffer.next();
        BlockProp newBlockHeight = ringBuffer.get(sequence);
        newBlockHeight.setChainIdentifier(chainIdentifier);
        newBlockHeight.setBlockHeight(chainHeight);
        newBlockHeight.setChainName(chainName);
        ringBuffer.publish(sequence);

      }
    }
  }

  static class NewBlockEventFactory implements EventFactory<BlockProp> {
    @Override
    public BlockProp newInstance() {
      return new BlockProp();
    }
  }
}
