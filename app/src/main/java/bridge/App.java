package bridge;

import java.util.ArrayList;
import java.util.Arrays;

import bridge.blockchains.ethchains.AvaxChain;
import bridge.blockchains.waveschains.WavesChain;
import bridge.common.BridgeUtils;
import bridge.services.disruptorservice.AttempListService;
import bridge.services.disruptorservice.NewBlockEventProducer;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class App
{
  @SneakyThrows
  public static void main(String[] args)
  {

    WavesChain<?> wavesChain = new WavesChain<>("fishfactoryP");
    AvaxChain<?> avaxChain = new AvaxChain<>("fishfarmtoken");
    NewBlockEventProducer producer = NewBlockEventProducer.getNewBlockEventProducer(wavesChain,
        avaxChain);
    AttempListService attempListService = new AttempListService(avaxChain);
    log.info("Starting Up Bridge...");
    //producer.start();
    attempListService.run();



    ArrayList<Integer> testArray = new ArrayList<Integer>();
    testArray.addAll(Arrays.asList(1, 2, 3, 5, 6, null, 20, null, null, 59, 69));
    log.info(BridgeUtils.filterNulls(testArray).toString());
  }
}
