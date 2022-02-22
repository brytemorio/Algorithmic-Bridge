package bridge.mongoservices;

import static bridge.common.ConfigFileObj.CONFIG;
import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Updates.set;
import static org.bson.codecs.configuration.CodecRegistries.fromProviders;
import static org.bson.codecs.configuration.CodecRegistries.fromRegistries;
import java.math.BigInteger;
import java.util.Objects;
import java.util.concurrent.CountDownLatch;
import org.bson.Document;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.PojoCodecProvider;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import com.electronwill.nightconfig.core.UnmodifiableConfig;
import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.ServerApi;
import com.mongodb.ServerApiVersion;
import com.mongodb.reactivestreams.client.MongoClient;
import com.mongodb.reactivestreams.client.MongoClients;
import com.mongodb.reactivestreams.client.MongoCollection;
import com.mongodb.reactivestreams.client.MongoDatabase;
import lombok.Data;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

// Going reactive was just an experiment, nothing more;
@Slf4j
public class MongoStorageService {

  private static UnmodifiableConfig configfile = CONFIG;

  // the name of database to be used in MongoDB
  @Getter private String databaseName;

  @Getter private String dbHostName;
  @Getter private int dbPort;
  @Getter private String username;
  @Getter private String password;

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

    CodecRegistry pojoCodecRegistry =
        fromRegistries(
            MongoClientSettings.getDefaultCodecRegistry(),
            fromProviders(PojoCodecProvider.builder().automatic(true).build()));

    ServerApi serverApi = ServerApi.builder().version(ServerApiVersion.V1).build();
    ConnectionString connectionString = new ConnectionString(this.connectionURL);
    MongoClientSettings mongoSettings =
        MongoClientSettings.builder()
            .applyConnectionString(connectionString)
            .retryReads(false)
            .retryWrites(false)
            .codecRegistry(pojoCodecRegistry)
            .serverApi(serverApi)
            .build();
    this.mongoClient = MongoClients.create(mongoSettings);
    this.dataBase = this.mongoClient.getDatabase(this.databaseName);
  }

  public void setBlockHeight(String chainIdentifier, BigInteger height) {
    MongoCollection<BlockHeightStorage> collection =
        dataBase.getCollection(
            CollectionNames.BlOCK_HEIGHT_STORAGE.toString(), BlockHeightStorage.class);
    collection
        .insertOne(new BlockHeightStorage(chainIdentifier, height))
        .subscribe(new StorageOperationSubscriber<>());
  }

  public void updateBlockHeightStorage(String chainIdentifier, BigInteger height) {
    MongoCollection<Document> collection =
        dataBase.getCollection(CollectionNames.BlOCK_HEIGHT_STORAGE.toString());
    Bson filter = eq("blockChainIdentifier", chainIdentifier);
    Bson update = set("blockHeight", height);
    collection.updateOne(filter, update).subscribe(new StorageOperationSubscriber<>());
  }

  // ========================POJOs=================================//
  @Data
  final class BlockHeightStorage {

    private ObjectId id;
    private String blockChainIdentifier;
    private BigInteger blockHeight;

    public BlockHeightStorage() {}

    BlockHeightStorage(final String blockChainIdentifier, final BigInteger blockHeight) {
      this.blockChainIdentifier = blockChainIdentifier;
      this.blockHeight = blockHeight;
    }
  }

  @Data
  final class AddressMappingStorage {
    private ObjectId id;

    // Wallet Address on the blockchain asset is been sent from
    private String fromBlockChainAddress;

    // Wallet Address on the target blockchain asset is been sent to
    private String toBlockChainAddress;

    AddressMappingStorage(final String fromBlockChainAddress, final String toBlockChainAddress) {
      this.fromBlockChainAddress = fromBlockChainAddress;
      this.toBlockChainAddress = toBlockChainAddress;
    }
  }

  // ==============CollectionNames=====================//
  public enum CollectionNames {
    BlOCK_HEIGHT_STORAGE("block_height_Store"),
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

  /* ============================Subscribers======================= */
  /* See  Here: https://bit.ly/3H3eIif
   * The sample code from above link uses
   * a list to store events and  errors
   * from the Publishers (MongoDB Operations).
   *
   * But since this MongoDB Client  instance is configure to
   * not retry Reads and Write operations and Inserts and Updates
   * Operations are singular(Single Entry) it therefore follows that
   * publishers results should be immediately consumed the moment
   * they are available
   *
   * */

  final class StorageOperationSubscriber<T> implements Subscriber<T> {

    @Getter private T receivedEvent;
    private Throwable error;
    @Getter private volatile Subscription subscription;
    private CountDownLatch countDownLatch;
    private volatile boolean completed;

    StorageOperationSubscriber() {
      this.countDownLatch = new CountDownLatch(1);
      this.error = new Throwable();
    }

    @Override
    public void onSubscribe(Subscription s) {
      subscription = s;
    }

    @Override
    public void onNext(T event) {
      receivedEvent = event;
      log.info("Event: ", receivedEvent);
    }

    @Override
    public void onError(Throwable t) {
      error = t;
      log.error("Error: ", error);
      onComplete();
    }

    @Override
    public void onComplete() {
      completed = true;
      countDownLatch.countDown();
    }
  }
}
