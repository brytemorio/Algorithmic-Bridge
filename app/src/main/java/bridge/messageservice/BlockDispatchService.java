package bridge.messageservice;

import com.lmax.disruptor.EventHandler;

final class BlockDispatchService implements EventHandler<BlockProp> {

  @Override
  public void onEvent(BlockProp event, long sequence, boolean endOfBatch) throws Exception {
    // TODO Proper Implementation
    System.out.println(event.getChainIdentifier() + ": " + event.getBlockHeight());
  }
}
