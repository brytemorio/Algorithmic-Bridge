package bridge.messageservice;

import bridge.common.IBaseChain;
import com.lmax.disruptor.EventFactory;
import com.lmax.disruptor.EventHandler;
import com.lmax.disruptor.RingBuffer;
import java.util.ArrayList;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import org.agrona.collections.Object2ObjectHashMap;

@Slf4j
final class BlockDispatchService implements EventHandler<BlockProp> {
  private IBaseChain[] blockChains;
  private final Object2ObjectHashMap<String, RingBuffer<ArrayList<String>>> chainRingBufferMapping =
      new Object2ObjectHashMap<>();

  private static BlockDispatchService blockDispatchService;

  private BlockDispatchService(final IBaseChain... cBlockchains) {
    // TOOD: Implement eventHandler and specify an appropriate buffer size
    this.blockChains = cBlockchains;
    for (IBaseChain iter : blockChains) {
      var disruptor =
          new DisruptorObjFactory<ArrayList<String>>(null, new ExtractorFactory(), null);
      disruptor.start();
      chainRingBufferMapping.put(iter.getChainIdentifier(), disruptor.getRingBuffer());
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

  private IBaseChain getUniqueChain(String chainIdentifier) {
    IBaseChain match = null;
    for (IBaseChain chain : blockChains) {
      if (chain.getChainIdentifier().equals(chainIdentifier)) {
        match = chain;
      }
    }
    return match;
  }

  @Override
  public void onEvent(BlockProp event, long sequence, boolean endOfBatch) throws Exception {

    log.info("Got " + event.getBlockHeight() + " with ID: " + event.getChainIdentifier());

    String uniqueChainIdentifier = event.getChainIdentifier();
    IBaseChain uniqueChain = Objects.requireNonNull(getUniqueChain(event.getChainIdentifier()));
    RingBuffer<ArrayList<String>> uniqueChainBuffer =
        chainRingBufferMapping.get(uniqueChainIdentifier);
    ArrayList<String> trxHashList = uniqueChain.getTrxHash(event.getBlockHeight());
    long uniqueChainBufferSequence = uniqueChainBuffer.next();
    ArrayList<String> nextSlot = uniqueChainBuffer.get(uniqueChainBufferSequence);
    nextSlot.addAll(trxHashList);
    uniqueChainBuffer.publish(uniqueChainBufferSequence);

    /*
     * Extractor extractor = new Extractor(uniqueChainIdentifier, uniqueChain,
     * uniqueChainBuffer); extractor.extractTrx(event.getBlockHeight());
     */

  }

  /*
   * TODO: Find out about alternate methods of implementing an EventFactory for
   * the disruptor
   */
  class ExtractorFactory implements EventFactory<ArrayList<String>> {

    @Override
    public ArrayList<String> newInstance() {
      return new ArrayList<String>();
    }
  }

  /*
   * @Data class Extractor { private IBaseChain blockChain; private String
   * chainIdentifier; private RingBuffer<ArrayList<String>> chainRingBuffer;
   *
   * public Extractor(final String chainIdentifier, final IBaseChain blockChain,
   * final RingBuffer<ArrayList<String>> chainRingBuffer) { this.chainIdentifier =
   * chainIdentifier; this.blockChain = blockChain; this.chainRingBuffer =
   * chainRingBuffer; }
   *
   * public void extractTrx(Number height) { ArrayList<String> trxHashList =
   * blockChain.getTrxHash(height); long sequence = chainRingBuffer.next();
   * ArrayList<String> nextSlot = chainRingBuffer.get(sequence);
   * nextSlot.addAll(trxHashList); chainRingBuffer.publish(sequence); }
   *
   * }
   */
}
