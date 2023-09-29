package bridge.disruptorservice;

import bridge.blockchains.IBaseChain;
import bridge.common.BridgeUtils;
import bridge.exceptions.BridgeExceptions.ObjectCreationException;
import bridge.services.storagservice.BlockHeightStorageService;
import com.lmax.disruptor.EventFactory;
import com.lmax.disruptor.EventHandler;
import com.lmax.disruptor.RingBuffer;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.agrona.collections.Object2ObjectHashMap;

import java.util.ArrayList;
import java.util.Objects;

@Slf4j
final class Extractor implements EventHandler<BlockProp>
{
  private final IBaseChain[] blockChains;
  private final Object2ObjectHashMap<String, RingBuffer<ArrayList<String>>> chainRingBufferMapping = new Object2ObjectHashMap<>();

  private static Extractor extractor;

  private Extractor(final IBaseChain... cBlockchains)
  {
    this.blockChains = cBlockchains;
    Integer bufferSize = 1024;
    for (IBaseChain chain : this.blockChains)
    {

      var disruptor = new DisruptorObjFactory<ArrayList<String>>(new TransactionHandler(chain),
          new TrxHashListFactory(), bufferSize);
      disruptor.start();
      chainRingBufferMapping.put(chain.getChainIdentifier(), disruptor.getRingBuffer());
    }
  }

  @SneakyThrows
  public static synchronized Extractor getExtractorObj(final IBaseChain... cBlockchains)
  {
    String className = Extractor.class.getName();
    BridgeUtils.checkArgsLength(cBlockchains,
        "Atleast One or more blockchain Object is required as parameter");
    if (extractor == null)
    {
      extractor = new Extractor(cBlockchains);
    }
    else
    {
      throw new ObjectCreationException("An instance of " + className + " already exits");
    }
    return extractor;
  }

  private IBaseChain getUniqueChain(String chainIdentifier)
  {
    IBaseChain match = null;
    for (IBaseChain chain : blockChains)
    {
      if (chainIdentifier.equals(chain.getChainIdentifier()))
      {
        match = chain;
      }
    }
    return match;
  }

  @Override
  public void onEvent(BlockProp event, long sequence, boolean endOfBatch) throws Exception
  {

    // TODO: Change log.info() to log.debug()
    log.info("Got new block: " + event.getBlockHeight() + " from " + event.getChainName());

    BlockHeightStorageService dbService = new BlockHeightStorageService();
    if (dbService.getBlockHeightFromStorage(event.getChainIdentifier()) == null)
      dbService.setBlockHeightStorage(event.getChainIdentifier(), event.getBlockHeight());
    else dbService.updateBlockHeightStorage(event.getChainIdentifier(), event.getBlockHeight());

    IBaseChain uniqueChain = Objects.requireNonNull(getUniqueChain(event.getChainIdentifier()));
    var uniqueChainBuffer = chainRingBufferMapping.get(event.getChainIdentifier());
    ArrayList<String> trxHashList = uniqueChain.getTrxIdsByBlockHeight(event.getBlockHeight());
    long uniqueChainBufferSequence = uniqueChainBuffer.next();
    var nextSlot = uniqueChainBuffer.get(uniqueChainBufferSequence);
    nextSlot.addAll(trxHashList);
    uniqueChainBuffer.publish(uniqueChainBufferSequence);
  }

  /*
   * TODO: Find out about alternate methods of implementing an EventFactory for
   * the disruptor
   */
  static class TrxHashListFactory implements EventFactory<ArrayList<String>>
  {

    @Override
    public ArrayList<String> newInstance()
    {
      return new ArrayList<>();
    }
  }
}
