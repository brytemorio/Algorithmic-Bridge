package bridge.services.transactionservice;

import bridge.blockchains.IBaseChain;
import bridge.services.storagservice.DataObjects;
import bridge.services.storagservice.TransactionAttemptListStorageService;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;

import static bridge.services.transactionservice.TransactionModels.Transaction;


@Slf4j
@Getter
@Setter
public class TransactionPollingService
{
  private final TransactionAttemptListStorageService trxAttemptListStorage;
  private IBaseChain blockChain;
  private String assetName;
  private DataObjects.PollingTransactionState pollingTransactionState;
  private DataObjects.PollingState pollingState;

  public TransactionPollingService(IBaseChain blockChain, String assetName)
  {
    this.blockChain = blockChain;
    this.assetName = assetName;
    this.trxAttemptListStorage = new TransactionAttemptListStorageService();
    this.pollingTransactionState = new DataObjects.PollingTransactionState();
    this.pollingState = new DataObjects.PollingState(this.blockChain.getChainIdentifier());
  }


  private void handleTransaction(Transaction trx)
  {
    log.info("Handling transaction for " + this.blockChain.getChainIdentifier());
    this.ensurePollingStateHasTransaction(trx);

    TransactionModels.MappedAddress es;
    this.blockChain.handleTransaction(trx);
  }

  private void ensurePollingStateHasTransaction(Transaction trx)
  {
    Map<String, DataObjects.PollingTransactionState> pollingTransactionStateMap = new HashMap<>();
    pollingTransactionStateMap.put(trx.getTransactionID(), this.pollingTransactionState);
    this.pollingState.setTransactionMap(pollingTransactionStateMap);
  }


  private boolean filterTransactions(Transaction transactionList)
  {
    if (null != trxAttemptListStorage.findTransactionAttemptById(
        transactionList.getTransactionID())) return false;
    return true;


  }
  
 /* private ArrayList<Integer> filterReceivers(Transaction trx){
      for( int i = 0; i < trx.getReceivers().size() - 1; i++) {
        MappedAddress address = trx.getReceivers().get(i).getAddress();
      mappedAddress.put(fromBlockChainName, address);
      String savedMappingForAddress = trxAttemptListStorage.getAddressFromSavedMapping(mappedAddress, toBlockchainName);
      if(null != savedMappingForAddress && !trx.getAddress().getAddress().equals("placeholder")) {
        var attemptList =
            trxAttemptListStorage.findTransactionAttemptByTrigger(new TransactionModels.TransactionAttemptListTrigger(trans));
      }
      }
     
    
  }*/

  //============================Helper functions====================================//

}
