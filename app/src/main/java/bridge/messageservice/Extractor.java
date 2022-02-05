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
  private final Object2ObjectHashMap<
          String, RingBuffer<Object2ObjectHashMap<BigInteger, ArrayList<String>>>>
      chainRingBufferMapping = new Object2ObjectHashMap<>();

  private static Extractor extractor;

  private Extractor(final IBaseChain... cBlockchains) {
    this.blockChains = cBlockchains;
    Integer bufferSize = 1024;
    for (IBaseChain chain : this.blockChains) {

      var disruptor =
          new DisruptorObjFactory<Object2ObjectHashMap<BigInteger, ArrayList<String>>>(
              new TransactionHandler(chain.getChainIdentifier()),
              new TrxHashListFactory(),
              bufferSize);
      disruptor.start();
      chainRingBufferMapping.put(chain.getChainIdentifier(), disruptor.getRingBuffer());
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

    // log.info("Got Block: " + event.getBlockHeight() + " with ID: " + event.getChainIdentifier());

    String uniqueChainIdentifier = event.getChainIdentifier();
    BigInteger uniqueChainHeight = event.getBlockHeight();
    IBaseChain uniqueChain = Objects.requireNonNull(getUniqueChain(event.getChainIdentifier()));
    var uniqueChainBuffer = chainRingBufferMapping.get(uniqueChainIdentifier);
    ArrayList<String> trxHashList = uniqueChain.getTrxIdsByBlockHeight(event.getBlockHeight());
    long uniqueChainBufferSequence = uniqueChainBuffer.next();
    var nextSlot = uniqueChainBuffer.get(uniqueChainBufferSequence);
    nextSlot.put(uniqueChainHeight, trxHashList);
    uniqueChainBuffer.publish(uniqueChainBufferSequence);
  }

  /*
   * TODO: Find out about alternate methods of implementing an EventFactory for
   * the disruptor
   */
  class TrxHashListFactory
      implements EventFactory<Object2ObjectHashMap<BigInteger, ArrayList<String>>> {

    @Override
    public Object2ObjectHashMap<BigInteger, ArrayList<String>> newInstance() {
      return new Object2ObjectHashMap<>();
    }
  }
}
