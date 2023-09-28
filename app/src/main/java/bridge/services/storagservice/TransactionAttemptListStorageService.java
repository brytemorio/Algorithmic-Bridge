package bridge.services.storagservice;

import bridge.services.transactionservice.TransactionModels;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Sorts;
import lombok.extern.slf4j.Slf4j;
import org.bson.conversions.Bson;

import static com.mongodb.client.model.Filters.and;
import static com.mongodb.client.model.Filters.eq;

@Slf4j
public class TransactionAttemptListStorageService
{

  private final MongoDatabase database;

  public TransactionAttemptListStorageService()
  {
    this.database = MongoDatabaseFactory.getMongoDatabase();
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


    assert result != null;
    log.debug(result.toString());
  }


  public TransactionModels.TransactionAttemptList findTransactionAttemptById(
      final String attemptListId)
  {
    var collection = getCollection();
    Bson filter = eq("id", attemptListId);
    return collection.find(filter).first();
  }

  public TransactionModels.TransactionAttemptList findOldestPendingAttemptList()
  {
    var collection = getCollection();

    int maxTries = 10;
    Bson query = Filters.and(Filters.or(Filters.and(Filters.not(Filters.size("attempts", 1)),
                Filters.not(Filters.size("transactions", 1))),
            Filters.and(Filters.size("attempts", 2), Filters.size("transactions", 2)),
            Filters.and(Filters.size("attempts", 3), Filters.size("transactions", 3))),
        Filters.or(Filters.lt("tries", maxTries), Filters.exists("tries", false)));

    FindIterable<TransactionModels.TransactionAttemptList> results = collection.find(query)
        .sort(Sorts.ascending("lastModifiedOn")).limit(1);

    TransactionModels.TransactionAttemptList document = results.first();
    if (document != null)
    {
      return document;
    }
    else
    {
      return null;
    }
  }

  public TransactionModels.TransactionAttemptList findTransactionAttemptByTrigger(
      TransactionModels.TransactionAttemptListTrigger trigger)
  {
    var collection = getCollection();
    Bson[] query = {eq("trigger.receiver", trigger.getReceiver()), eq("trigger.currency",
        trigger.getCurrency()), eq("trigger.trxId", trigger.getTrxId())};
    Bson filter = and(query);
    return collection.find(filter).first();
  }

  public boolean gatewayTransactionExists(String transactionID)
  {
    var collections = getCollection();
    for (var collection : collections.find())
    {
      java.util.List<String> transactions = collection.getTransactions();
      if (transactions != null && transactions.contains(transactionID))
      {
        return true;
      }
    }
    return false;
  }


  private MongoCollection<TransactionModels.TransactionAttemptList> getCollection()
  {
    return this.database.getCollection(Collections.VALID_TRASANCTION_STORAGE.toString(),
        TransactionModels.TransactionAttemptList.class);
  }
}
