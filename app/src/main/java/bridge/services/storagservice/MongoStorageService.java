package bridge.services.storagservice;

import bridge.services.transactionservice.TransactionModels.MappedAddress;
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

import java.math.BigInteger;
import java.util.Map;
import java.util.Objects;
import java.util.stream.StreamSupport;

import static bridge.common.ConfigFileObj.CONFIG;
import static com.mongodb.client.model.Filters.*;
import static com.mongodb.client.model.Updates.set;
import static org.bson.codecs.configuration.CodecRegistries.fromProviders;
import static org.bson.codecs.configuration.CodecRegistries.fromRegistries;


/*FIXME: This class is in need of heavy refactoring*/
@Slf4j
public class MongoStorageService implements IStorageService
{

  private static final UnmodifiableConfig configfile = CONFIG;

  // the name of database to be used in MongoDB
  private String databaseName;
  private String dbHostName;
  private int dbPort;
  private String username;
  private String password;
  private CodecRegistry codecRegistry;

  @Getter
  private String connectionURL;

  private MongoClient mongoClient;
  private MongoDatabase dataBase;

  public MongoStorageService()
  {
    this.databaseName = Objects.requireNonNullElse(
        MongoStorageService.configfile.get("Bridge.database.name"), "Algo-Bridge");
    this.dbHostName = Objects.requireNonNullElse(
        MongoStorageService.configfile.get("Bridge.database.hostname"), "localhost");
    this.dbPort = Objects.requireNonNullElse(
        MongoStorageService.configfile.get("Bridge.database.port"), 27017);
    this.username = MongoStorageService.configfile.get("Bridge.database.username");
    this.password = MongoStorageService.configfile.get("Bridge.database.password");

    if (this.username == null || this.password == null)
    {
      this.connectionURL = "mongodb://" + this.dbHostName + ":" + this.dbPort;
    }
    else
    {
      this.connectionURL = "mongodb://" + this.username + ":" + this.password + "@" + this.dbHostName + ":" + this.dbPort;
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



    this.codecRegistry = fromRegistries(MongoClientSettings.getDefaultCodecRegistry(),
        fromProviders(PojoCodecProvider.builder()
            .register(blockHeightStorageModel, addressMappingStroageModel,
                transactionAttemptListStorageModel, transactionPolllingStateModel).automatic(true)
            .build()));

    ServerApi serverApi = ServerApi.builder().version(ServerApiVersion.V1).build();
    ConnectionString connectionString = new ConnectionString(this.connectionURL);
    MongoClientSettings mongoSettings = MongoClientSettings.builder().codecRegistry(codecRegistry)
        .retryReads(true).retryWrites(false).serverApi(serverApi)
        .applyConnectionString(connectionString).build();
    this.mongoClient = MongoClients.create(mongoSettings);
    this.dataBase = this.mongoClient.getDatabase(this.databaseName);
  }

  @Override
  public void setBlockHeightStorage(String chainIdentifier, BigInteger height)
  {
    MongoCollection<DataObjects.BlockHeightStorage> collection = dataBase.getCollection(
        CollectionNames.BlOCK_HEIGHT_STORAGE.toString(), DataObjects.BlockHeightStorage.class);
    var result = collection.insertOne(new DataObjects.BlockHeightStorage(chainIdentifier, height));
    log.info(result.toString());
  }

  @Override
  public BigInteger getBlockHeightFromStorage(String chainIdentifier)
  {
    MongoCollection<DataObjects.BlockHeightStorage> collection = dataBase.getCollection(
        CollectionNames.BlOCK_HEIGHT_STORAGE.toString(), DataObjects.BlockHeightStorage.class);
    Bson filter = eq("blockChainIdentifier", chainIdentifier);
    var height = collection.find(filter).first();
    if (height == null) return null;
    return BigInteger.valueOf(height.getBlockHeight());
  }

  @Override
  public void updateBlockHeightStorage(String chainIdentifier, BigInteger height)
  {
    MongoCollection<DataObjects.BlockHeightStorage> collection = dataBase.getCollection(
        CollectionNames.BlOCK_HEIGHT_STORAGE.toString(), DataObjects.BlockHeightStorage.class);
    Bson filter = eq("blockChainIdentifier", chainIdentifier);

    /*
     * Serialization problems passing in BigInteger Type directly, hence height.longValue()
     * conversion
     */
    Bson update = set("blockHeight", height.longValue());
    collection.updateOne(filter, update);
  }

  @Override
  public void saveAddressMapping(DataObjects.AddressMappingStorage addressMappingStorage)
  {
    MongoCollection<DataObjects.AddressMappingStorage> collection = dataBase.getCollection(
        CollectionNames.ADDRESS_MAPPING_STORAGE.toString(),
        DataObjects.AddressMappingStorage.class);
    var result = collection.insertOne(addressMappingStorage);

    // TODO: Change to log.Debug
    log.info(result.toString());
  }

  @Override
  public String getAddressFromSavedMapping(Map<String, MappedAddress> address,
                                           String targetBlockChainName)
  {
    // Todo: Find a more efficient method for retrieving valid Address Mappings
    MongoCollection<DataObjects.AddressMappingStorage> collection = dataBase.getCollection(
        CollectionNames.ADDRESS_MAPPING_STORAGE.toString(),
        DataObjects.AddressMappingStorage.class);
    String key = getKey(address);
    MappedAddress keyValue = address.get(key);

    // since variable cannot be directly modified in lambda expressions
    var pairedAddress = new Object()
    {
      String address;
    };

    Bson[] filters = {eq("fromBlockChainAddress" + "." + key + "." + "address",
        keyValue.getAddress()), eq("toBlockChainAddress" + "." + key + "." + "address",
        keyValue.getAddress())};

    Bson queryString = or(filters);

    StreamSupport.stream(collection.find(queryString).spliterator(), false).parallel()
        .forEach(mappedAddress -> {
          if (key.equals(
              getKey(mappedAddress.getFromBlockChainAddress())) && (targetBlockChainName.equals(
              getKey(mappedAddress.getToBlockChainAddress()))))
            pairedAddress.address = mappedAddress.getToBlockChainAddress().get(targetBlockChainName)
                .getAddress();

          if (key.equals(
              getKey(mappedAddress.getToBlockChainAddress())) && (targetBlockChainName.equals(
              getKey(mappedAddress.getFromBlockChainAddress()))))
            pairedAddress.address = mappedAddress.getFromBlockChainAddress()
                .get(targetBlockChainName).getAddress();
        });

    return pairedAddress.address;
  }

  public void saveTransactionAttemptList(TransactionAttemptList transactionAttemptList)
  {

    MongoCollection<TransactionAttemptList> collection = dataBase.getCollection(
        CollectionNames.VALID_TRASANCTION_STORAGE.toString(), TransactionAttemptList.class);
    var result = collection.insertOne(transactionAttemptList);

    // TODO: Change to Debug
    log.info(result.toString());
  }

  public void updateTransactionAttemptList(TransactionAttemptList transactionAttemptList)
  {
    MongoCollection<TransactionAttemptList> collection = dataBase.getCollection(
        CollectionNames.VALID_TRASANCTION_STORAGE.toString(), TransactionAttemptList.class);
    Bson filter = eq("Id", transactionAttemptList.getId());
    var result = collection.findOneAndReplace(filter, transactionAttemptList);

    // TODO: Change to Debug
    log.info(result.toString());
  }

  public TransactionAttemptList findTransactionAttemptById(final String attemptListId)
  {
    MongoCollection<TransactionAttemptList> collection = dataBase.getCollection(
        CollectionNames.VALID_TRASANCTION_STORAGE.toString(), TransactionAttemptList.class);
    Bson filter = eq("id", attemptListId);
    return collection.find(filter).first();
  }

  public TransactionAttemptList findTransactionAttemptByTrigger(
      TransactionAttemptListTrigger trigger)
  {
    MongoCollection<TransactionAttemptList> collection = dataBase.getCollection(
        CollectionNames.VALID_TRASANCTION_STORAGE.toString(), TransactionAttemptList.class);
    Bson[] query = {eq("trigger.receiver", trigger.getReceiver()), eq("trigger.currency",
        trigger.getCurrency()), eq("trigger.trxId", trigger.getTrxId())};
    Bson filter = and(query);
    return collection.find(filter).first();
  }

  public TransactionAttemptList findOldestPendingAttemptList()
  {
    int maxTries = 10;
    Bson query = Filters.and(Filters.or(Filters.and(Filters.not(Filters.size("attempts", 1)),
                Filters.not(Filters.size("transactions", 1))),
            Filters.and(Filters.size("attempts", 2), Filters.size("transactions", 2)),
            Filters.and(Filters.size("attempts", 3), Filters.size("transactions", 3))),
        Filters.or(Filters.lt("tries", maxTries), Filters.exists("tries", false)));

    MongoCollection<TransactionAttemptList> collection = dataBase.getCollection(
        CollectionNames.VALID_TRASANCTION_STORAGE.toString(), TransactionAttemptList.class);

    FindIterable<TransactionAttemptList> results = collection.find(query)
        .sort(Sorts.ascending("lastModifiedOn")).limit(1);

    TransactionAttemptList document = results.first();
    if (document != null)
    {
      return document;
    }
    else
    {
      return null;
    }
  }

  public boolean gatewayTransactionExists(String transactionID)
  {
    MongoCollection<TransactionAttemptList> collections = dataBase.getCollection(
        CollectionNames.VALID_TRASANCTION_STORAGE.toString(), TransactionAttemptList.class);
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

  public void saveAssets(DataObjects.AssetStorage assetStorage)
  {
    MongoCollection<DataObjects.AssetStorage> collection = dataBase.getCollection(
        CollectionNames.ASSET_STORAGE.toString(), DataObjects.AssetStorage.class);
    var chain = getKey(assetStorage.getAssets());

    Bson filter = Filters.eq("assets." + chain, new Document("$exits", true));

    Bson updateOperation = Updates.combine(
        Updates.setOnInsert("assets." + chain, assetStorage.getAssets().get(chain))
    );

    UpdateOptions updateOptions = new UpdateOptions().upsert(true);
    collection.updateOne(filter, updateOperation, updateOptions);
  }



  // ==============CollectionNames=====================//
  public enum CollectionNames
  {
    BlOCK_HEIGHT_STORAGE("Block_Height_Store"), ADDRESS_MAPPING_STORAGE(
      "Address_Mapping_Store"), VALID_TRASANCTION_STORAGE(
      "Transaction_AttemptList_Store"), TRANSACTION_POLLING_STATE("transaction_polling_state"),
    ASSET_STORAGE("Assets_Store"),

    /*
     * Bridge internal wallets(public and private Keys pairs per blockchains supported used for
     * handling transactions
     */
    WALLET_STORAGE("Wallet_Store");

    private String name;

    private CollectionNames(final String name)
    {
      this.name = name;
    }

    @Override
    public String toString()
    {
      return name;
    }
  }

  // =========================Helper Functions=======================//

  // Assumption: The Map passed into this function has only one entry
  private <K, V> K getKey(Map<K, V> hashMapObj)
  {
    K key = null;
    for (var iter : hashMapObj.keySet())
    {
      key = iter;
    }
    return key;
  }
}
