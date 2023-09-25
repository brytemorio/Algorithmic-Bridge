package bridge.services.transactionservice;

import static bridge.services.transactionservice.TransactionModels.Transaction;

import com.lmax.disruptor.EventHandler;

import java.util.ArrayList;
import java.util.List;

import bridge.blockchains.IBaseChain;
import bridge.services.storagservice.MongoStorageService;


public class TransactionPollerEventHandler implements EventHandler<ArrayList<String>>
{
  private final MongoStorageService storageService = new MongoStorageService();
  private IBaseChain blockChain;
  private String assetName;

  public TransactionPollerEventHandler(IBaseChain blockChain, String assetName)
  {
    this.blockChain = blockChain;
    this.assetName = assetName;
  }

  public TransactionPollerEventHandler()
  {
  }

  @Override
  public void onEvent(ArrayList<String> event, long sequence, boolean endOfBatch) throws Exception
  {
    List<Transaction> transactionList = new ArrayList<>();

    event.stream()
        .forEach(trxId -> transactionList.add(blockChain.getTransaction(trxId, assetName)));


  }


  private boolean filterTransactions(Transaction transactionList)
  {
    if (null != storageService.findTransactionAttemptById(transactionList.getTransactionID()))
      return false;
    return true;


  }
  
 /* private ArrayList<Integer> filterReceivers(Transaction trx){
      for( int i = 0; i < trx.getReceivers().size() - 1; i++) {
        MappedAddress address = trx.getReceivers().get(i).getAddress();
      mappedAddress.put(fromBlockChainName, address);
      String savedMappingForAddress = storageService.getAddressFromSavedMapping(mappedAddress, toBlockchainName);
      if(null != savedMappingForAddress && !trx.getAddress().getAddress().equals("placeholder")) {
        var attemptList =
            storageService.findTransactionAttemptByTrigger(new TransactionModels.TransactionAttemptListTrigger(trans));
      }
      }
     
    
  }*/

  //============================Helper functions====================================//

}
