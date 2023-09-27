package bridge.services.transactionservice;

import static bridge.services.transactionservice.TransactionModels.Transaction;

import bridge.services.storagservice.DataObjects;
import com.lmax.disruptor.EventHandler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import bridge.blockchains.IBaseChain;
import bridge.services.storagservice.MongoStorageService;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;


@Slf4j
@Getter
@Setter
@NoArgsConstructor
public class TransactionPollingService
{
  private final MongoStorageService storageService = new MongoStorageService();
  private  IBaseChain blockChain;
  private String assetName;
  private MongoStorageService mongoStorageService;
  private DataObjects.PollingTransactionState pollingTransactionState;
  private DataObjects.PollingState pollingState;
  public TransactionPollingService(IBaseChain blockChain, String assetName)
  {
    this.blockChain = blockChain;
    this.assetName = assetName;
    this.mongoStorageService = new MongoStorageService();
    this.pollingTransactionState = new DataObjects.PollingTransactionState();
    this.pollingState = new DataObjects.PollingState(this.blockChain.getChainIdentifier());
  }


  private void handleTransaction(Transaction trx)
  {
    log.info("Handling transaction for " + this.blockChain.getChainIdentifier());
    this.ensurePollingStateHasTransaction(trx);

    TransactionModels.MappedAddress es;
    this.blockChain.handleTransaction(trx, es.getAddress());
  }

  private void ensurePollingStateHasTransaction(Transaction trx)
  {
    Map<String, DataObjects.PollingTransactionState> pollingTransactionStateMap = new HashMap<>();
    pollingTransactionStateMap.put(trx.getTransactionID(), this.pollingTransactionState);
    this.pollingState.setTransactionMap(pollingTransactionStateMap);
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
