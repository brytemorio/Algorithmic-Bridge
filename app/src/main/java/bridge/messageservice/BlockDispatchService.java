package bridge.messageservice;

import bridge.common.IBaseChain;
import com.lmax.disruptor.EventHandler;
import com.lmax.disruptor.RingBuffer;
import lombok.extern.slf4j.Slf4j;
import org.agrona.collections.Object2ObjectHashMap;

@Slf4j
final class BlockDispatchService implements EventHandler<BlockProp> {

  private IBaseChain[] blockChains;
  private final Object2ObjectHashMap<String, RingBuffer<Extractor>> chainRingBufferMapping =
      new Object2ObjectHashMap<>();

  private static BlockDispatchService blockDispatchService;

  private BlockDispatchService(final IBaseChain... cBlockchains) {
    this.blockChains = cBlockchains;
    for (IBaseChain iter : blockChains) {
      var disruptor = new DisruptorObjFactory<Extractor>(null, null, null);
      chainRingBufferMapping.put(iter.getChainIdentifier(), disruptor.start());
    }
  }

  public static synchronized BlockDispatchService getNewBlockDispatchService(
      final IBaseChain... cBlockchains) {
    String className = BlockDispatchService.class.getCanonicalName();
    if (blockDispatchService == null) {
      blockDispatchService = new BlockDispatchService(cBlockchains);
    } else {
      log.trace("An instance of " + className + " already exists");
    }
    return blockDispatchService;
  }

  @Override
  public void onEvent(BlockProp event, long sequence, boolean endOfBatch) throws Exception {
    // TODO Proper Implementation
    System.out.println(event.getChainIdentifier() + ": " + event.getBlockHeight());
  }

  private Extractor extractorFactory() {
    return new Extractor(null);
  }

  class Extractor {
    private IBaseChain blockChain;
    private String chainIdentifier;

    public Extractor(IBaseChain blockChain) {
      this.blockChain = blockChain;
    }
  }
}
