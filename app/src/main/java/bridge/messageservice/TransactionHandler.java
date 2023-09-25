package bridge.messageservice;

import bridge.blockchains.IBaseChain;
import com.lmax.disruptor.EventHandler;
import java.util.ArrayList;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TransactionHandler implements EventHandler<ArrayList<String>> {

  private String chainIdentifier;
  private IBaseChain chain;

  public TransactionHandler() {}

  public TransactionHandler(IBaseChain chain) {
    this.chain = chain;
  }

  @Override
  public void onEvent(ArrayList<String> event, long sequence, boolean endOfBatch) throws Exception {
    //log.info(chainIdentifier + ":" + " List of transactions Id => " + event.toString());
    log.info(chainIdentifier + ":" + " Number of transactions in block => " + event.toArray().length);
    event.stream().forEach(trx ->{
      //do nothing
    });
  }
}
