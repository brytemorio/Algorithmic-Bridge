package bridge.services.storagservice;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.UpdateOptions;
import org.bson.Document;
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
    Bson filter = Filters.eq("chainName", pollingState.getChainName());
    var existChainPollingState = collection.find(filter).first();
    if (existChainPollingState != null)
    {
      Document updateIt = new Document("$set", pollingState);
      UpdateOptions updateOptions = new UpdateOptions().upsert(true);
      collection.updateOne(filter, updateIt, updateOptions);
    }
    else
    {
      collection.insertOne(pollingState);
    }

  }

  public DataObjects.PollingState getTransactionPollingState(String chainName)
  {
    var collection = getCollection();
    Bson query = eq("chainName", chainName);
    try (var coursor = collection.find(query).iterator())
    {
      if (coursor.hasNext()) return coursor.next();
      return null;
    }
  }

  private MongoCollection<DataObjects.PollingState> getCollection()
  {
    return this.database.getCollection(Collections.TRANSACTION_POLLING_STATE.toString(),
        DataObjects.PollingState.class);
  }
}
