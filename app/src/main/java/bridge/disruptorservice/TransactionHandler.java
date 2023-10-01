package bridge.disruptorservice;

import bridge.blockchains.IBaseChain;
import bridge.services.transactionservice.TransactionPollingService;
import com.lmax.disruptor.EventHandler;

import java.util.ArrayList;

import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@NoArgsConstructor
public class TransactionHandler implements EventHandler<ArrayList<String>>
{

  private String chainIdentifier;
  private IBaseChain chain;


  public TransactionHandler(IBaseChain chain)
  {
    this.chain = chain;
  }

  @Override
  public void onEvent(ArrayList<String> event, long sequence, boolean endOfBatch) throws Exception
  {
    log.info(
        chain.getChainName() + ":" + " Number of transactions in block => " + event.toArray().length);
    log.info("Now checking the each transactions");
    var trxPoller = new TransactionPollingService(chain);
    trxPoller.run(event);
  }

}
