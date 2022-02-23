package bridge.mongoservices;

import static bridge.common.ConfigFileObj.CONFIG;
import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Updates.set;
import static org.bson.codecs.configuration.CodecRegistries.fromProviders;
import static org.bson.codecs.configuration.CodecRegistries.fromRegistries;
import java.math.BigInteger;
import java.util.Objects;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;
import org.bson.BsonDocumentReader;
import org.bson.Document;
import org.bson.codecs.DecoderContext;
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

@Slf4j
public class MongoStorageService {

  private static final UnmodifiableConfig configfile = CONFIG;
  private static AtomicReference<Object> fromBsonToPojoObj;

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

    this.codecRegistry =
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
            .codecRegistry(codecRegistry)
            .serverApi(serverApi)
            .build();
    this.mongoClient = MongoClients.create(mongoSettings);
    this.dataBase = this.mongoClient.getDatabase(this.databaseName);
  }

  public void setBlockHeightStorage(String chainIdentifier, BigInteger height) {
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
    Bson update = set("blockHeight", height.longValue());
    collection.updateOne(filter, update).subscribe(new StorageOperationSubscriber<>());
  }

  public void getBlockHeightStorage(String chainIdentifier) {
    MongoCollection<Document> collection =
        dataBase.getCollection(CollectionNames.BlOCK_HEIGHT_STORAGE.toString());
    Bson filter = eq("blockChainIdentifier", chainIdentifier);
    collection
        .find(filter)
        .first()
        .subscribe(new FromBSONtoPOJOSubscriber<BlockHeightStorage>(BlockHeightStorage.class));
    System.out.println(MongoStorageService.fromBsonToPojoObj);
  }

  // ========================POJOs=================================//
  @Data
  public final class BlockHeightStorage {

    private ObjectId id;
    private String blockChainIdentifier;
    private Long blockHeight;

    public BlockHeightStorage() {}

    public BlockHeightStorage(final String blockChainIdentifier, final BigInteger blockHeight) {
      this.blockChainIdentifier = blockChainIdentifier;
      this.blockHeight = blockHeight.longValue();
    }
  }

  @Data
  protected final class AddressMappingStorage {
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
  /* See  Here: https://bit.ly/3H3eIif */
  class StorageOperationSubscriber<T> implements Subscriber<T> {

    @Getter private volatile T receivedEvent;
    @Getter private volatile Subscription subscription;
    private volatile Throwable error;
    private volatile boolean completed;
    private CountDownLatch countDownLatch;

    StorageOperationSubscriber() {
      this.countDownLatch = new CountDownLatch(1);
      this.error = new Throwable();
    }

    @Override
    public void onSubscribe(Subscription s) {
      subscription = s;
      subscription.request(Long.MAX_VALUE);
    }

    @Override
    public void onNext(T event) {
      receivedEvent = event;
      log.info("Event: " + receivedEvent.toString());
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

  final class FromBSONtoPOJOSubscriber<T extends Object> extends StorageOperationSubscriber<Bson> {
    private Class<T> obj;

    public FromBSONtoPOJOSubscriber(Class<T> object) {
      this.obj = object;
    }

    @Override
    public void onNext(Bson document) {
      super.onNext(document);
      MongoStorageService.fromBsonToPojoObj.set(decodeDocuemnt(document));
    }

    private T decodeDocuemnt(Bson document) {
      var pojo = PojoCodecProvider.builder().automatic(true).build();
      return pojo.get(obj, codecRegistry)
          .decode(
              new BsonDocumentReader(document.toBsonDocument()), DecoderContext.builder().build());
      /* return codecRegistry
      .get(obj, PojoCodecProvider.builder().automatic(true).build())
      .decode(
          new BsonDocumentReader(document.toBsonDocument()), DecoderContext.builder().build())*/
    }
  }
}
