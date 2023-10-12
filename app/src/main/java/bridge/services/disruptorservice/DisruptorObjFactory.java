package bridge.services.disruptorservice;

import bridge.services.disruptorservice.eventhandlers.EventHelpers;
import com.lmax.disruptor.EventFactory;
import com.lmax.disruptor.EventHandler;
import com.lmax.disruptor.WaitStrategy;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.dsl.ProducerType;
import com.lmax.disruptor.util.DaemonThreadFactory;

final class DisruptorObjFactory<T> extends Disruptor<T> {

  public DisruptorObjFactory(
      final EventHandler<T> eventHandler,
      final EventFactory<T> eventFactory,
      final Integer bufferSize) {
    super(eventFactory, bufferSize, DaemonThreadFactory.INSTANCE);
    super.handleExceptionsFor(eventHandler);
    super.handleEventsWith(eventHandler).then(new EventHelpers.ClearBufferSlot());
  }

  public DisruptorObjFactory(
      final EventHandler<T> eventHandler,
      EventFactory<T> eventFactory,
      final Integer bufferSize,
      final WaitStrategy waitStrategy) {
    super(eventFactory, bufferSize, DaemonThreadFactory.INSTANCE, ProducerType.MULTI, waitStrategy);
    super.handleExceptionsFor(eventHandler);
    super.handleEventsWith(eventHandler).then(new EventHelpers.ClearBufferSlot());
  }
}
