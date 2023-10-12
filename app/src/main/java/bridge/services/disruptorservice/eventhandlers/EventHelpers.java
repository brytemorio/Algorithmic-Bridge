package bridge.services.disruptorservice.eventhandlers;

import com.lmax.disruptor.EventHandler;

public class EventHelpers {
  private EventHelpers() {}

  public static class ClearBufferSlot implements EventHandler<Object> {


    @Override
    public void onEvent(Object event, long sequence, boolean endOfBatch) throws Exception {
      event =null;
    }
  }
}
