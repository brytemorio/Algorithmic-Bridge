package bridge.services.storagservice;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.UpdateOptions;
import com.mongodb.client.model.Updates;
import lombok.extern.slf4j.Slf4j;
import org.bson.Document;
import org.bson.conversions.Bson;

@Slf4j
public class ConfigurationStorageService
{
  private MongoDatabase database;

  public ConfigurationStorageService()
  {
    this.database = MongoDatabaseFactory.getMongoDatabase();
  }

  public void saveConfiguration(DataObjects.ConfigurationStorage configurationStorage)
  {
    var collection = getCollection();
    Bson filter = Filters.eq("chainName", configurationStorage.getChainName());
    Bson updateOperation = Updates.combine(
        Updates.setOnInsert("chainName", configurationStorage.getChainName()));
    UpdateOptions updateOptions = new UpdateOptions().upsert(true);
    collection.updateOne(filter, updateOperation, updateOptions);

  }

  public DataObjects.ConfigurationStorage getConfiguration(String chainname){
    var collection = getCollection();
    Bson filter = Filters.eq("chainName", chainname);
    return collection.find(filter).first();
  }

  private MongoCollection<DataObjects.ConfigurationStorage> getCollection()
  {
    return this.database.getCollection(Collections.CONFIGURATION_STORE.toString(),
        DataObjects.ConfigurationStorage.class);
  }
}
