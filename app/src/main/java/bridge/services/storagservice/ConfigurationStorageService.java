package bridge.services.storagservice;

import bridge.exceptions.BridgeExceptions;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.UpdateOptions;
import com.mongodb.client.model.Updates;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.bson.Document;
import org.bson.conversions.Bson;

@Getter
@Slf4j
public class ConfigurationStorageService
{
  private final MongoDatabase database;


  public ConfigurationStorageService()
  {
    this.database = MongoDatabaseFactory.getMongoDatabase();
  }

  public void saveConfiguration(DataObjects.ConfigurationStorage configurationStorage)
  {
    var collection = getCollection();
    Bson filter = Filters.eq("chainName", configurationStorage.getChainName());
    var exsitConfig = collection.find(filter).first();
    if (exsitConfig != null)
    {
      Document updateDocument = new Document("$set", configurationStorage);
      UpdateOptions updateOptions = new UpdateOptions().upsert(true);
      collection.updateOne(filter, updateDocument, updateOptions);
    }
    else
    {
      collection.insertOne(configurationStorage);
    }


    //collection.insertOne(configurationStorage);

  }

  public DataObjects.ConfigurationStorage getConfiguration(String chainname)
  {
    var collection = getCollection();
    //Bson filter = Filters.eq("chainName", chainname);
    Document findFilter = new Document("chainName", chainname);
    try (MongoCursor<DataObjects.ConfigurationStorage> cursor =
             collection.find(findFilter).iterator())
    {
      if (cursor.hasNext())
      {
        return cursor.next();
      }
      else
      {
        throw new BridgeExceptions.ConfigurationStoreNotFoundException(
            "could not find the " + "confiuration store for " + chainname);
      }
    }

  }

  private MongoCollection<DataObjects.ConfigurationStorage> getCollection()
  {
    return this.database.getCollection(Collections.CONFIGURATION_STORE.toString(),
        DataObjects.ConfigurationStorage.class);
  }
}
