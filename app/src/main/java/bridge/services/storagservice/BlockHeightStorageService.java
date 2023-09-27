package bridge.services.storagservice;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import lombok.extern.slf4j.Slf4j;
import org.bson.conversions.Bson;

import java.math.BigInteger;

import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Updates.set;


@Slf4j
public class BlockHeightStorageService
{
  private MongoDatabase database;
  public BlockHeightStorageService()
  {
    this.database = MongoDatabaseFactory.getMongoDatabase();
  }



  public void setBlockHeightStorage(String chainIdentifier, BigInteger height)
  {
    var collection = getCollection();
    var result = collection.insertOne(new DataObjects.BlockHeightStorage(chainIdentifier, height));

    log.debug(result.toString());
  }

  public BigInteger getBlockHeightFromStorage(String chainIdentifier)
  {
    var collection = getCollection();
    Bson filter = eq("blockChainIdentifier", chainIdentifier);
    var height = collection.find(filter).first();
    if (height == null) return null;
    return BigInteger.valueOf(height.getBlockHeight());
  }

  public void updateBlockHeightStorage(String chainIdentifier, BigInteger height)
  {
    var collection = getCollection();
    Bson filter = eq("blockChainIdentifier", chainIdentifier);

    /*
     * Serialization problems passing in BigInteger Type directly, hence height.longValue()
     * conversion
     */
    Bson update = set("blockHeight", height.longValue());
    collection.updateOne(filter, update);
  }


  private MongoCollection<DataObjects.BlockHeightStorage> getCollection()
  {
    return this.database.getCollection(Collections.BlOCK_HEIGHT_STORAGE.toString(),
        DataObjects.BlockHeightStorage.class);
  }
}
