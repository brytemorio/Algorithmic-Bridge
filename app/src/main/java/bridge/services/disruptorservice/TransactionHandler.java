package bridge.services.disruptorservice;

import bridge.blockchains.IBaseChain;
import bridge.services.transactionservice.TransactionPollingService;
import com.lmax.disruptor.EventHandler;

import java.util.ArrayList;
import java.util.Arrays;

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
    //FIXME: Remove check. This checking was for the purpose of debuging
    if(chain.getChainName().equals("waves")){
      log.info(
          chain.getChainName() + ":" + " Number of transactions in block => " + event.toArray().length);
      log.info("Now checking each transactions in " + chain.getChainName());
    }

    try
    {
      var trxPoller = new TransactionPollingService(chain);
      trxPoller.run(event);
    } catch (Exception exp)
    {
      exp.printStackTrace();
    }
  }
}
