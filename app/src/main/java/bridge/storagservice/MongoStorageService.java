package bridge.storagservice;

import static bridge.common.ConfigFileObj.CONFIG;
import static com.mongodb.client.model.Filters.and;
import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Filters.or;
import static com.mongodb.client.model.Updates.set;
import static org.bson.codecs.configuration.CodecRegistries.fromProviders;
import static org.bson.codecs.configuration.CodecRegistries.fromRegistries;
import java.math.BigInteger;
import java.util.Map;
import java.util.Objects;
import java.util.stream.StreamSupport;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.ClassModel;
import org.bson.codecs.pojo.PojoCodecProvider;
import org.bson.conversions.Bson;
import com.electronwill.nightconfig.core.UnmodifiableConfig;
import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.ServerApi;
import com.mongodb.ServerApiVersion;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import bridge.transactionservice.TransactionModels.MappedAddress;
import bridge.transactionservice.TransactionModels.TransactionAttemptList;
import bridge.transactionservice.TransactionModels.TransactionAttemptListTrigger;
import bridge.storagservice.DataObjects.AddressMappingStorage;
import bridge.storagservice.DataObjects.BlockHeightStorage;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MongoStorageService implements IStorageService {

  private static final UnmodifiableConfig configfile = CONFIG;

  // the name of database to be used in MongoDB
  private String databaseName;
  private String dbHostName;
  private int dbPort;
  private String username;
  private String password;
  private CodecRegistry codecRegistry;

  @Getter private String connectionURL;

  private MongoClient mongoClient;
  private MongoDatabase dataBase;

  public MongoStorageService() {
    this.databaseName =
        Objects.requireNonNullElse(
            MongoStorageService.configfile.get("Bridge.database.name"), "Algo-Bridge");
    this.dbHostName =
        Objects.requireNonNullElse(
            MongoStorageService.configfile.get("Bridge.database.hostname"), "localhost");
    this.dbPort =
        Objects.requireNonNullElse(
            MongoStorageService.configfile.get("Bridge.database.port"), 27017);
    this.username = MongoStorageService.configfile.get("Bridge.database.username");
    this.password = MongoStorageService.configfile.get("Bridge.database.password");

    if (this.username == null || this.password == null) {
      this.connectionURL = "mongodb://" + this.dbHostName + ":" + this.dbPort;
    } else {
      this.connectionURL =
          "mongodb://"
              + this.username
              + ":"
              + this.password
              + "@"
              + this.dbHostName
              + ":"
              + this.dbPort;
    }

    ClassModel<BlockHeightStorage> blockHeightStorageModel =
        ClassModel.builder(BlockHeightStorage.class).enableDiscriminator(true).build();

    ClassModel<AddressMappingStorage> addressMappingStroageModel =
        ClassModel.builder(AddressMappingStorage.class).enableDiscriminator(true).build();

    this.codecRegistry =
        fromRegistries(
            MongoClientSettings.getDefaultCodecRegistry(),
            fromProviders(
                PojoCodecProvider.builder()
                    .register(blockHeightStorageModel, addressMappingStroageModel)
                    .automatic(true)
                    .build()));

    ServerApi serverApi = ServerApi.builder().version(ServerApiVersion.V1).build();
    ConnectionString connectionString = new ConnectionString(this.connectionURL);
    MongoClientSettings mongoSettings =
        MongoClientSettings.builder()
            .codecRegistry(codecRegistry)
            .retryReads(true)
            .retryWrites(false)
            .serverApi(serverApi)
            .applyConnectionString(connectionString)
            .build();
    this.mongoClient = MongoClients.create(mongoSettings);
    this.dataBase = this.mongoClient.getDatabase(this.databaseName);
  }

  @Override
  public void setBlockHeightStorage(String chainIdentifier, BigInteger height) {
    MongoCollection<BlockHeightStorage> collection =
        dataBase.getCollection(
            CollectionNames.BlOCK_HEIGHT_STORAGE.toString(), BlockHeightStorage.class);
    var result = collection.insertOne(new BlockHeightStorage(chainIdentifier, height));
    log.info(result.toString());
  }

  @Override
  public BigInteger getBlockHeightFromStorage(String chainIdentifier) {
    MongoCollection<BlockHeightStorage> collection =
        dataBase.getCollection(
            CollectionNames.BlOCK_HEIGHT_STORAGE.toString(), BlockHeightStorage.class);
    Bson filter = eq("blockChainIdentifier", chainIdentifier);
    var height = collection.find(filter).first();
    if (height == null) return null;
    return BigInteger.valueOf(height.getBlockHeight());
  }

  @Override
  public void updateBlockHeightStorage(String chainIdentifier, BigInteger height) {
    MongoCollection<BlockHeightStorage> collection =
        dataBase.getCollection(
            CollectionNames.BlOCK_HEIGHT_STORAGE.toString(), BlockHeightStorage.class);
    Bson filter = eq("blockChainIdentifier", chainIdentifier);

    /*
     * Serialization problems passing in BigInteger Type directly, hence height.longValue()
     * conversion
     */
    Bson update = set("blockHeight", height.longValue());
    collection.updateOne(filter, update);
  }

  @Override
  public void saveAddressMapping(AddressMappingStorage addressMappingStorage) {
    MongoCollection<AddressMappingStorage> collection =
        dataBase.getCollection(
            CollectionNames.ADDRESS_MAPPING_STORAGE.toString(), AddressMappingStorage.class);
    var result = collection.insertOne(addressMappingStorage);

    // TODO: Change to log.Debug
    log.info(result.toString());
  }

  @Override
  public String getAddressFromSavedMapping(
      Map<String, MappedAddress> address, String targetBlockChainName) {
    // Todo: Find a more efficient method for retrieving valid Address Mappings
    MongoCollection<AddressMappingStorage> collection =
        dataBase.getCollection(
            CollectionNames.ADDRESS_MAPPING_STORAGE.toString(), AddressMappingStorage.class);
    String key = getKey(address);
    MappedAddress keyValue = address.get(key);

    // since variable cannot be directly modified in lambda expressions
    var pairedAddress =
        new Object() {
          String address;
        };

    Bson[] filters = {
      eq("fromBlockChainAddress" + "." + key + "." + "address", keyValue.getAddress()),
      eq("toBlockChainAddress" + "." + key + "." + "address", keyValue.getAddress())
    };

    Bson queryString = or(filters);

    StreamSupport.stream(collection.find(queryString).spliterator(), false)
        .parallel()
        .forEach(
            mappedAddress -> {
              if (key.equals(getKey(mappedAddress.getFromBlockChainAddress()))
                  && (targetBlockChainName.equals(getKey(mappedAddress.getToBlockChainAddress()))))
                pairedAddress.address =
                    mappedAddress.getToBlockChainAddress().get(targetBlockChainName).getAddress();

              if (key.equals(getKey(mappedAddress.getToBlockChainAddress()))
                  && (targetBlockChainName.equals(
                      getKey(mappedAddress.getFromBlockChainAddress()))))
                pairedAddress.address =
                    mappedAddress.getFromBlockChainAddress().get(targetBlockChainName).getAddress();
            });

    return pairedAddress.address;
  }

  public void saveTransactionAttempt(TransactionAttemptList transactionAttemptList) {

    MongoCollection<TransactionAttemptList> collection =
        dataBase.getCollection(
            CollectionNames.VALID_TRASANCTION_STORAGE.toString(), TransactionAttemptList.class);
    var result = collection.insertOne(transactionAttemptList);

    // TODO: Change to Debug
    log.info(result.toString());
  }

  public void updateTransactionAttempt(TransactionAttemptList transactionAttemptList) {
    MongoCollection<TransactionAttemptList> collection =
        dataBase.getCollection(
            CollectionNames.VALID_TRASANCTION_STORAGE.toString(), TransactionAttemptList.class);
    Bson filter = eq("Id", transactionAttemptList.getId());
    var result = collection.findOneAndReplace(filter, transactionAttemptList);

    // TODO: Change to Debug
    log.info(result.toString());
  }

  public TransactionAttemptList findTransactionAttemptById(final String attemptListId) {
    MongoCollection<TransactionAttemptList> collection =
        dataBase.getCollection(
            CollectionNames.VALID_TRASANCTION_STORAGE.toString(), TransactionAttemptList.class);
    Bson filter = eq("id", attemptListId);
    return collection.find(filter).first();
  }

  public TransactionAttemptList findTransactionAttemptByTrigger(
      TransactionAttemptListTrigger trigger) {
    MongoCollection<TransactionAttemptList> collection =
        dataBase.getCollection(
            CollectionNames.VALID_TRASANCTION_STORAGE.toString(), TransactionAttemptList.class);
    Bson[] query = {
      eq("trigger.receiver", trigger.getReceiver()),
      eq("trigger.currency", trigger.getCurrency()),
      eq("trigger.trxId", trigger.getTrxId())
    };
    Bson filter = and(query);
    return collection.find(filter).first();
  }

  // ==============CollectionNames=====================//
  public enum CollectionNames {
    BlOCK_HEIGHT_STORAGE("Block_Height_Store"),
    ADDRESS_MAPPING_STORAGE("Address_Mapping_Store"),
    VALID_TRASANCTION_STORAGE("Transaction_List_Store"),

    /*
     * Bridge internal wallets(public and private Keys pairs per blockchains supported used for
     * handling transactions
     */
    WALLET_STORAGE("Wallet_Store");

    private String name;

    private CollectionNames(final String name) {
      this.name = name;
    }

    @Override
    public String toString() {
      return name;
    }
  }

  // =========================Helper Functions=======================//

  // Assumption: The Map passed into this function has only one entry
  private <K, V> K getKey(Map<K, V> hashMapObj) {
    K key = null;
    for (var iter : hashMapObj.keySet()) {
      key = iter;
    }
    return key;
  }
}
