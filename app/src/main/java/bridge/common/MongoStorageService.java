package bridge.common;

import static bridge.common.ConfigFileObj.CONFIG;
import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Filters.or;
import static com.mongodb.client.model.Updates.set;
import static org.bson.codecs.configuration.CodecRegistries.fromProviders;
import static org.bson.codecs.configuration.CodecRegistries.fromRegistries;
import java.math.BigInteger;
import java.util.Objects;
import org.agrona.collections.Object2ObjectHashMap;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.ClassModel;
import org.bson.codecs.pojo.PojoCodecProvider;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;
import com.electronwill.nightconfig.core.UnmodifiableConfig;
import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.ServerApi;
import com.mongodb.ServerApiVersion;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import lombok.Data;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MongoStorageService {

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
            MongoStorageService.configfile.get("Bridge.database.name"), "Algo-bridge");
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
            .retryReads(false)
            .retryWrites(false)
            .serverApi(serverApi)
            .applyConnectionString(connectionString)
            .build();
    this.mongoClient = MongoClients.create(mongoSettings);
    this.dataBase = this.mongoClient.getDatabase(this.databaseName);
  }

  public void setBlockHeightStorage(String chainIdentifier, BigInteger height) {
    MongoCollection<BlockHeightStorage> collection =
        dataBase.getCollection(
            CollectionNames.BlOCK_HEIGHT_STORAGE.toString(), BlockHeightStorage.class);
    var result = collection.insertOne(new BlockHeightStorage(chainIdentifier, height));
    log.info(result.toString());
  }

  public BigInteger getBlockHeightFromStorage(String chainIdentifier) {
    MongoCollection<BlockHeightStorage> collection =
        dataBase.getCollection(
            CollectionNames.BlOCK_HEIGHT_STORAGE.toString(), BlockHeightStorage.class);
    Bson filter = eq("blockChainIdentifier", chainIdentifier);
    var height = collection.find(filter).first();
    log.info(BigInteger.valueOf(height.getBlockHeight()).toString());
    return BigInteger.valueOf(height.getBlockHeight());
  }

  public void updateBlockHeightStorage(String chainIdentifier, BigInteger height) {
    MongoCollection<BlockHeightStorage> collection =
        dataBase.getCollection(
            CollectionNames.BlOCK_HEIGHT_STORAGE.toString(), BlockHeightStorage.class);
    Bson filter = eq("blockChainIdentifier", chainIdentifier);

    // since BSON can't handle BigInteger Type directly, hence height.longValue()
    Bson update = set("blockHeight", height.longValue());
    collection.updateOne(filter, update);
  }

  public void saveAddressMapping(
      Object2ObjectHashMap<String, String> fromBlockChainAddress,
      Object2ObjectHashMap<String, String> toBlockChainAddress) {
    MongoCollection<AddressMappingStorage> collection =
        dataBase.getCollection(
            CollectionNames.ADDRESS_MAPPING_STORAGE.toString(), AddressMappingStorage.class);
    var result =
        collection.insertOne(new AddressMappingStorage(fromBlockChainAddress, toBlockChainAddress));

    // Todo: Change to log.Debug
    log.info(result.toString());
  }

  /**
   * Retrieves either the sending address or the receiving address from the saved mapping of both,
   * depening on the one that is passed to the function (first parameter). That is if the sending
   * address is passed then the receiving address is retrieved and vice versa.
   *
   * @param targetBlockChainName - is the name of the blockchain the wallet to be retrieved belongs
   *     to.
   * @param address - either the sending or receiving the address. It a hashMap containing the a
   *     mapping of the name of the BlockChain to the Address.
   * @return - returns the mappedAddress
   */
  public String getAddressFromSavedMapping(
      Object2ObjectHashMap<String, String> address, String targetBlockChainName) {
    // Todo: Find a more efficient method for retrieving valid Address Mappings
    MongoCollection<AddressMappingStorage> collection =
        dataBase.getCollection(
            CollectionNames.ADDRESS_MAPPING_STORAGE.toString(), AddressMappingStorage.class);
    String key = getKey(address);
    String keyValue = address.get(key);
    String pairedAddress = null;

    Bson[] filters = {
      eq("fromBlockChainAddress" + "." + key, keyValue),
      eq("toBlockChainAddress" + "." + key, keyValue)
    };
    Bson queryString = or(filters);
    var result = collection.find().filter(queryString);
    for (var iter : result) {
      if (key.equals(getKey(iter.getFromBlockChainAddress()))
          && (targetBlockChainName.equals(getKey(iter.getToBlockChainAddress()))))
        pairedAddress = iter.getToBlockChainAddress().get(targetBlockChainName);

      if (key.equals(getKey(iter.getToBlockChainAddress()))
          && (targetBlockChainName.equals(getKey(iter.getFromBlockChainAddress()))))
        pairedAddress = iter.getFromBlockChainAddress().get(targetBlockChainName);
    }

    return pairedAddress;
  }
  // ========================POJOs=================================//
  @Data
  protected static final class BlockHeightStorage {

    private ObjectId id;
    private String blockChainIdentifier;

    // since BSON can't handle BigInteger Type directly, we default to long
    private long blockHeight;

    public BlockHeightStorage() {}

    public BlockHeightStorage(final String blockChainIdentifier, final BigInteger blockHeight) {
      this.blockChainIdentifier = blockChainIdentifier;
      this.blockHeight = blockHeight.longValue();
    }
  }

  @Data
  protected static final class AddressMappingStorage {
    private ObjectId id;

    /*
     * The fields fromBlockChainAddress and toBlockChainAddress
     * are Hashmaps containing the  mapping of the blockchain
     * ChainName as key to the users WalletAddress as value:
     * BlockChiainName => Address
     */

    // Wallet Address on the blockchain asset is been sent from
    private Object2ObjectHashMap<String, String> fromBlockChainAddress =
        new Object2ObjectHashMap<>();

    // Wallet Address on the target blockchain asset is been sent to
    private Object2ObjectHashMap<String, String> toBlockChainAddress = new Object2ObjectHashMap<>();

    public AddressMappingStorage() {}

    AddressMappingStorage(
        final Object2ObjectHashMap<String, String> fromBlockChainAddress,
        final Object2ObjectHashMap<String, String> toBlockChainAddress) {
      this.fromBlockChainAddress = fromBlockChainAddress;
      this.toBlockChainAddress = toBlockChainAddress;
    }
  }

  // ==============CollectionNames=====================//
  public enum CollectionNames {
    BlOCK_HEIGHT_STORAGE("Block_Height_Store"),
    ADDRESS_MAPPING_STORAGE("Address_Mapping_Store"),

    /*Bridge internal wallets(public and private Keys pairs
     *  per blockchains supported  used for handling
     *  transactions */
    WALLET_STORAGE("Wallet_Store"),

    VALID_TRASANCTION_STORAGE("Transaction_List_Store");

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

  // the parameter:Hashmap  passed into this function has only one key
  private String getKey(Object2ObjectHashMap<String, String> hashMapObj) {
    String key = null;
    for (var iter : hashMapObj.keySet()) {
      key = iter;
    }
    return key;
  }
}
