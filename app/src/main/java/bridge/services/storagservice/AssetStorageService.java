package bridge.services.storagservice;

import bridge.blockchains.Asset;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.UpdateOptions;
import com.mongodb.client.model.Updates;
import org.bson.Document;
import org.bson.conversions.Bson;
import java.util.ArrayList;

import static bridge.services.storagservice.StorageServiceUtils.getKey;

public class AssetStorageService
{
  private final MongoDatabase database;

  public AssetStorageService()
  {
    this.database = MongoDatabaseFactory.getMongoDatabase();
  }


  public void saveAssetsToStorage(DataObjects.AssetStorage assetStorage)
  {
    var collection = getCollection();
    var chain = getKey(assetStorage.getAssets());

    Bson filter = Filters.eq("assets." + chain, new Document("$exits", true));

    Bson updateOperation = Updates.combine(
        Updates.setOnInsert("assets." + chain, assetStorage.getAssets().get(chain)));

    UpdateOptions updateOptions = new UpdateOptions().upsert(true);
    collection.updateOne(filter, updateOperation, updateOptions);
  }

  public ArrayList<Asset> getAssetFromStorage(String blockhain)
  {
    var collection = getCollection();

    Bson filter = Filters.eq("assets." + blockhain, new Document("$exits", true));
    DataObjects.AssetStorage restult = collection.find(filter).first();

    if (restult != null) return restult.getAssets().get(blockhain);
    else return null;
  }


  private MongoCollection<DataObjects.AssetStorage> getCollection()
  {
    return this.database.getCollection(Collections.ASSET_STORAGE.toString(),
        DataObjects.AssetStorage.class);
  }
}
