package bridge.messageservice;

import bridge.blockchains.IBaseChain;
import bridge.common.BridgeUtils;
import bridge.exceptions.BridgeExceptions.ObjectCreationException;
import com.lmax.disruptor.EventFactory;
import com.lmax.disruptor.EventHandler;
import com.lmax.disruptor.RingBuffer;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Objects;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.agrona.collections.Object2ObjectHashMap;

@Slf4j
final class Extractor implements EventHandler<BlockProp> {
  private IBaseChain[] blockChains;
  private final Object2ObjectHashMap<String, RingBuffer<ArrayList<String>>> chainRingBufferMapping =
      new Object2ObjectHashMap<>();

  private static Extractor extractor;

  private Extractor(final IBaseChain... cBlockchains) {
    // TOOD: Implement eventHandler and specify an appropriate buffer size
    this.blockChains = cBlockchains;
    Integer bufferSize = 1024;
    for (IBaseChain iter : this.blockChains) {
      var disruptor =
          new DisruptorObjFactory<ArrayList<String>>(
              new TransactionHandler(), new TrxHashListFactory(), bufferSize);
      disruptor.start();
      chainRingBufferMapping.put(iter.getChainIdentifier(), disruptor.getRingBuffer());
    }
  }

  @SneakyThrows
  public static synchronized Extractor getExtractorObj(final IBaseChain... cBlockchains) {
    String className = Extractor.class.getName();
    BridgeUtils.checkArgsLength(
        cBlockchains, "Atleast One or more blockchain Object is required as parameter");
    if (extractor == null) {
      extractor = new Extractor(cBlockchains);
    } else {
      throw new ObjectCreationException("An instance of " + className + " already exits");
    }
    return extractor;
  }

  private IBaseChain getUniqueChain(String chainIdentifier) {
    IBaseChain match = null;
    for (IBaseChain chain : blockChains) {
      if (chainIdentifier.equals(chain.getChainIdentifier())) {
        match = chain;
      }
    }
    return match;
  }

  @Override
  public void onEvent(BlockProp event, long sequence, boolean endOfBatch) throws Exception {
    BigInteger previousBlockHeight = BigInteger.ZERO;
    BigInteger currentBlockHeight = event.getBlockHeight();
    String currentBlockHeightChainID = event.getChainIdentifier();
    if (currentBlockHeight.compareTo(previousBlockHeight) > 0) {
      previousBlockHeight = currentBlockHeight;
    } else {
      return;
    }
    log.info("Got Block: " + previousBlockHeight + " with ID: " + currentBlockHeightChainID);

    String uniqueChainIdentifier = event.getChainIdentifier();
    IBaseChain uniqueChain = Objects.requireNonNull(getUniqueChain(currentBlockHeightChainID));
    RingBuffer<ArrayList<String>> uniqueChainBuffer =
        chainRingBufferMapping.get(uniqueChainIdentifier);
    ArrayList<String> trxHashList = uniqueChain.getTrxIdsByBlockHeight(previousBlockHeight);
    long uniqueChainBufferSequence = uniqueChainBuffer.next();
    ArrayList<String> nextSlot = uniqueChainBuffer.get(uniqueChainBufferSequence);
    nextSlot.addAll(trxHashList);
    uniqueChainBuffer.publish(uniqueChainBufferSequence);
  }

  /*
   * TODO: Find out about alternate methods of implementing an EventFactory for
   * the disruptor
   */
  class TrxHashListFactory implements EventFactory<ArrayList<String>> {

    @Override
    public ArrayList<String> newInstance() {
      return new ArrayList<>();
    }
  }
}
