package bridge.disruptorservice;

import bridge.blockchains.IBaseChain;
import bridge.common.BridgeUtils;
import bridge.exceptions.BridgeExceptions;
import com.lmax.disruptor.EventFactory;
import com.lmax.disruptor.EventHandler;
import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.dsl.Disruptor;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicReference;

@Slf4j
public class AddressValidator
{
  private final IBaseChain[] blockchains;

  public static final AtomicReference<String> gatewayAddress = new AtomicReference<>();
  public static final AtomicReference<String> addressToValidate = new AtomicReference<>();
  public static final AtomicReference<ArrayList<IBaseChain>> chains = new AtomicReference<>();

  private String previousAddressToValidate;
  private final RingBuffer<ValidatorObject> ringBuffer;

  private static AddressValidator addressValidator;

  private AddressValidator(final IBaseChain... cblockchains)
  {
    this.blockchains = cblockchains;
    Integer bufferSize = 1024;

    Disruptor<ValidatorObject> disruptor = new DisruptorObjFactory<ValidatorObject>(
        new AddressValidationEventHandler(), new ValidatorObjectFactory(), bufferSize);
    disruptor.start();
    this.ringBuffer = disruptor.getRingBuffer();
  }


  @SneakyThrows
  public void start()
  {
    for(;;)
    {
      String currentAddressToValid = AddressValidator.addressToValidate.get();
      if(!currentAddressToValid.equals(previousAddressToValidate))
      {
        previousAddressToValidate = currentAddressToValid;
        long sequence = ringBuffer.next();
        ValidatorObject validatorObject = ringBuffer.get(sequence);
        validatorObject.setAddressToValidate(currentAddressToValid);
        validatorObject.setChains((ArrayList<IBaseChain>) Arrays.asList(this.blockchains));
        ringBuffer.publish(sequence);
      }
      /*try
      {
        Thread.sleep(1000);
      } catch (InterruptedException e)
      {
        log.error(e.getMessage());
        Thread.currentThread().interrupt();
      }*/
    }
  }




  public static AddressValidator getNewInstance(final IBaseChain... chains)
  {
    BridgeUtils.checkArgsLength(chains,
        "Atleast One or more blockchain Object is required as parameter");

    if (addressValidator == null)
    {
      addressValidator = new AddressValidator(chains);
    }
    else
    {
      throw new BridgeExceptions.ObjectCreationException(
          "An instance of " + AddressValidator.class.getName() + " already exits");
    }
    return addressValidator;
  }


  static class AddressValidationEventHandler implements EventHandler<ValidatorObject>
  {
    @Override
    @SneakyThrows
    public void onEvent(ValidatorObject event, long sequece, boolean endOfBatch)
    {

    }
  }


  static class ValidatorObjectFactory implements EventFactory<ValidatorObject>
  {

    @Override
    public ValidatorObject newInstance()
    {
      return new ValidatorObject();
    }
  }

  @Data
  static class ValidatorObject
  {
    private ArrayList<IBaseChain> chains;
    private String addressToValidate;
  }
}
