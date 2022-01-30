package bridge.messageservice;

import com.lmax.disruptor.EventFactory;
import com.lmax.disruptor.EventHandler;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.util.DaemonThreadFactory;

final class DisruptorObjFactory<T> extends Disruptor<T> {

  public DisruptorObjFactory(
      final EventHandler<T> eventHandler,
      final EventFactory<T> eventFactory,
      final Integer bufferSize) {
    super(eventFactory, bufferSize, DaemonThreadFactory.INSTANCE);
    super.handleEventsWith(eventHandler);
  }
}
