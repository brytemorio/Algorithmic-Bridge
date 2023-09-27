package bridge.services.storagservice;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.conversions.Bson;

import static com.mongodb.client.model.Filters.eq;

public class TransactionPollingStateStorageService
{
  private final MongoDatabase database;

  public TransactionPollingStateStorageService()
  {
    this.database = MongoDatabaseFactory.getMongoDatabase();
  }


  public void setTransactionPollingState(DataObjects.PollingState pollingState)
  {
    var collection = getCollection();
    collection.insertOne(pollingState);
  }

  public DataObjects.PollingState getTransactionPollingState(String chainIdentifier)
  {
    var collection = getCollection();
    Bson query = eq("chainIdentifier", chainIdentifier);
    return collection.find(query).first();
  }

  private MongoCollection<DataObjects.PollingState> getCollection()
  {
    return this.database.getCollection(Collections.TRANSACTION_POLLING_STATE.toString(),
        DataObjects.PollingState.class);
  }
}
