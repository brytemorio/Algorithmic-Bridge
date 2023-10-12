package bridge.services.disruptorservice.eventhandlers;

import com.lmax.disruptor.EventHandler;

public class ClearBufferSlotHandler implements EventHandler<String> {
  @Override
  public void onEvent(String event, long sequence, boolean endOfBatch) throws Exception {
    // System.out.print(event);

  }
}
