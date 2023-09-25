package bridge.transactionservice;
import static bridge.common.TransactionModels.Transaction;

import bridge.common.TransactionModels;
import com.lmax.disruptor.EventHandler;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.StreamSupport;
import org.agrona.collections.Object2ObjectHashMap;
import bridge.blockchains.IBaseChain;
import bridge.common.TransactionModels.MappedAddress;
import bridge.common.TransactionModels.TransactionReceiver;
import bridge.storagservice.MongoStorageService;
import static bridge.common.BridgeUtils.filterNulls;


public class TransactionPoller implements EventHandler<ArrayList<String>>{
  private final MongoStorageService storageService = new MongoStorageService(); 
  private IBaseChain blockChain;
  private String assetName;
  
  public TransactionPoller(IBaseChain blockChain, String assetName) {
    this.blockChain = blockChain;
    this.assetName = assetName;
  }
  public TransactionPoller() {}
  
  @Override
  public void onEvent(ArrayList<String> event, long sequence, boolean endOfBatch) throws Exception {
    List<Transaction> transactionList = new ArrayList<>();
    
    event.parallelStream().forEach(trxId -> transactionList.add(blockChain.getTransaction(trxId, assetName)));
    
    
  }
  
  
  private boolean filterTransactions(Transaction transactionList){
      if(null != storageService.findTransactionAttemptById(transactionList.getTransactionID())) return false;
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
