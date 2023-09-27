package bridge.services.storagservice;

import bridge.blockchains.Asset;
import bridge.services.transactionservice.TransactionModels.TransactionAttemptList;
import bridge.services.transactionservice.TransactionModels.TransactionAttemptListTrigger;
import com.electronwill.nightconfig.core.UnmodifiableConfig;
import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.ServerApi;
import com.mongodb.ServerApiVersion;
import com.mongodb.client.*;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Sorts;
import com.mongodb.client.model.UpdateOptions;
import com.mongodb.client.model.Updates;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.bson.Document;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.ClassModel;
import org.bson.codecs.pojo.PojoCodecProvider;
import org.bson.conversions.Bson;

import java.util.ArrayList;
import java.util.Objects;

import static bridge.common.ConfigFileObj.CONFIG;
import static com.mongodb.client.model.Filters.*;
import static org.bson.codecs.configuration.CodecRegistries.fromProviders;
import static org.bson.codecs.configuration.CodecRegistries.fromRegistries;


/*FIXME: This class is in need of heavy refactoring*/
@Slf4j
public class MongoDatabaseFactory
{

  private static final UnmodifiableConfig configfile = CONFIG;

  @Getter
  private final MongoDatabase dataBase;

  private MongoDatabaseFactory()
  {
    // the name of database to be used in MongoDB
    String databaseName = Objects.requireNonNullElse(
        MongoDatabaseFactory.configfile.get("Bridge.database.name"), "Algo-Bridge");
    String dbHostName = Objects.requireNonNullElse(
        MongoDatabaseFactory.configfile.get("Bridge.database.hostname"), "localhost");
    int dbPort = Objects.requireNonNullElse(MongoDatabaseFactory.configfile.get("Bridge.database" +
            ".port"),
        27017);
    String username = MongoDatabaseFactory.configfile.get("Bridge.database.username");
    String password = MongoDatabaseFactory.configfile.get("Bridge.database.password");

    String connectionURL;
    if (username == null || password == null)
    {
      connectionURL = "mongodb://" + dbHostName + ":" + dbPort;
    }
    else
    {
      connectionURL = "mongodb://" + username + ":" + password + "@" + dbHostName + ":" + dbPort;
    }

    ClassModel<DataObjects.BlockHeightStorage> blockHeightStorageModel = ClassModel.builder(
        DataObjects.BlockHeightStorage.class).enableDiscriminator(true).build();

    ClassModel<DataObjects.AddressMappingStorage> addressMappingStroageModel = ClassModel.builder(
        DataObjects.AddressMappingStorage.class).enableDiscriminator(true).build();

    ClassModel<TransactionAttemptList> transactionAttemptListStorageModel = ClassModel.builder(
        TransactionAttemptList.class).enableDiscriminator(true).build();

    ClassModel<DataObjects.PollingState> transactionPolllingStateModel = ClassModel.builder(
        DataObjects.PollingState.class).enableDiscriminator(true).build();

    ClassModel<DataObjects.AssetStorage> assetStorageClassModel = ClassModel.builder(
        DataObjects.AssetStorage.class).enableDiscriminator(true).build();


    CodecRegistry codecRegistry = fromRegistries(MongoClientSettings.getDefaultCodecRegistry(),
        fromProviders(PojoCodecProvider.builder()
            .register(blockHeightStorageModel, addressMappingStroageModel,
                transactionAttemptListStorageModel, transactionPolllingStateModel,
                assetStorageClassModel).automatic(true).build()));

    ServerApi serverApi = ServerApi.builder().version(ServerApiVersion.V1).build();
    ConnectionString connectionString = new ConnectionString(connectionURL);
    MongoClientSettings mongoSettings = MongoClientSettings.builder().codecRegistry(codecRegistry)
        .retryReads(true).retryWrites(false).serverApi(serverApi)
        .applyConnectionString(connectionString).build();
    MongoClient mongoClient = MongoClients.create(mongoSettings);
    this.dataBase = mongoClient.getDatabase(databaseName);
  }

  

  public static MongoDatabase getMongoClient()
  {
    return new MongoDatabaseFactory().getDataBase();
  }





  public void setTransactionPollingState(DataObjects.PollingState pollingState)
  {
    MongoCollection<DataObjects.PollingState> collection = dataBase.getCollection(
        CollectionNames.TRANSACTION_POLLING_STATE.toString(), DataObjects.PollingState.class);
    collection.insertOne(pollingState);
  }

  public DataObjects.PollingState getTrnasactionPollingState(String chainIdentifier)
  {
    MongoCollection<DataObjects.PollingState> collection = dataBase.getCollection(
        CollectionNames.TRANSACTION_POLLING_STATE.toString(), DataObjects.PollingState.class);
    Bson query = eq("chainIdentifier", chainIdentifier);
    return collection.find(query).first();
  }

  public void saveAssetsToStorage(DataObjects.AssetStorage assetStorage)
  {
    MongoCollection<DataObjects.AssetStorage> collection = dataBase.getCollection(
        CollectionNames.ASSET_STORAGE.toString(), DataObjects.AssetStorage.class);
    var chain = getKey(assetStorage.getAssets());

    Bson filter = Filters.eq("assets." + chain, new Document("$exits", true));

    Bson updateOperation = Updates.combine(
        Updates.setOnInsert("assets." + chain, assetStorage.getAssets().get(chain)));

    UpdateOptions updateOptions = new UpdateOptions().upsert(true);
    collection.updateOne(filter, updateOperation, updateOptions);
  }

  public ArrayList<Asset> getAssetFromStorage(String blockhain)
  {
    MongoCollection<DataObjects.AssetStorage> collection = dataBase.getCollection(
        CollectionNames.ASSET_STORAGE.toString(), DataObjects.AssetStorage.class);

    Bson filter = Filters.eq("assets." + blockhain, new Document("$exits", true));

    DataObjects.AssetStorage restult = collection.find(filter).first();

    if (restult != null) return restult.getAssets().get(blockhain);
    else return null;
  }

}
