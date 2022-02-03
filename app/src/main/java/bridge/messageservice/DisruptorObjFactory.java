package bridge.messageservice;

import com.lmax.disruptor.BusySpinWaitStrategy;
import com.lmax.disruptor.EventFactory;
import com.lmax.disruptor.EventHandler;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.dsl.ProducerType;
import com.lmax.disruptor.util.DaemonThreadFactory;

final class DisruptorObjFactory<T> extends Disruptor<T> {

  public DisruptorObjFactory(
      final EventHandler<T> eventHandler,
      final EventFactory<T> eventFactory,
      final Integer bufferSize) {
    super(eventFactory, bufferSize, DaemonThreadFactory.INSTANCE);
    super.handleEventsWith(eventHandler);
  }

  /** the Boolean parameter is just a Marker. It's value is never used */
  public DisruptorObjFactory(
      final EventHandler<T> eventHandler,
      EventFactory<T> eventFactory,
      final Integer bufferSize,
      final Boolean flag) {
    super(
        eventFactory,
        bufferSize,
        DaemonThreadFactory.INSTANCE,
        ProducerType.MULTI,
        new BusySpinWaitStrategy());
    super.handleEventsWith(eventHandler);
  }
}
