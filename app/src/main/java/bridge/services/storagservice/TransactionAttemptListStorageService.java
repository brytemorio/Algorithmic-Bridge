package bridge.services.storagservice;

import bridge.services.transactionservice.TransactionModels;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import lombok.extern.slf4j.Slf4j;
import org.bson.conversions.Bson;

import static com.mongodb.client.model.Filters.eq;

@Slf4j
public class TransactionAttemptListStorageService
{

  private final MongoDatabase database;
  public TransactionAttemptListStorageService()
  {
    this.database = MongoDatabaseFactory.getMongoClient();
  }


  public void saveTransactionAttemptList(
      TransactionModels.TransactionAttemptList transactionAttemptList)
  {

    var collection = getCollection();
    var result = collection.insertOne(transactionAttemptList);

    log.debug(result.toString());
  }

  public void updateTransactionAttemptList(
      TransactionModels.TransactionAttemptList transactionAttemptList)
  {
    var collection = getCollection();
    Bson filter = eq("Id", transactionAttemptList.getId());
    var result = collection.findOneAndReplace(filter, transactionAttemptList);


    log.debug(result.toString());
  }


  public TransactionModels.TransactionAttemptList findTransactionAttemptById(final String attemptListId)
  {
    MongoCollection<TransactionModels.TransactionAttemptList> collection = dataBase.getCollection(
        CollectionNames.VALID_TRASANCTION_STORAGE.toString(), TransactionModels.TransactionAttemptList.class);
    Bson filter = eq("id", attemptListId);
    return collection.find(filter).first();
  }



  private MongoCollection<TransactionModels.TransactionAttemptList> getCollection()
  {
    return this.database.getCollection(Collections.VALID_TRASANCTION_STORAGE.toString(),
        TransactionModels.TransactionAttemptList.class);
  }
}
