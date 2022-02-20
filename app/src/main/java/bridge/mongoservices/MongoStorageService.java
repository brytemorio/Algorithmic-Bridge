package bridge.mongoservices;

import static bridge.common.ConfigFileObj.CONFIG;
import static org.bson.codecs.configuration.CodecRegistries.fromProviders;
import static org.bson.codecs.configuration.CodecRegistries.fromRegistries;
import java.math.BigInteger;
import java.util.Objects;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.PojoCodecProvider;
import org.bson.types.ObjectId;
import com.electronwill.nightconfig.core.UnmodifiableConfig;
import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.ServerApi;
import com.mongodb.ServerApiVersion;
import com.mongodb.reactivestreams.client.MongoClient;
import com.mongodb.reactivestreams.client.MongoClients;
import com.mongodb.reactivestreams.client.MongoDatabase;
import lombok.Data;
import lombok.Getter;

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

  @Data
  final class BlockHeightStorage {

    private ObjectId id;
    private String blockchainName;
    private BigInteger blockHeight;

    BlockHeightStorage(final String blockchainName, final BigInteger blockHeight) {
      this.blockchainName = blockchainName;
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

  public enum CollectionNames {
    BlOCK_HEIGHT("block_height"),
    ADDRESS_MAPPING("Address_Mapping"),
    WALLET_STORAGE("Wallet_Storage"),
    VALID_TRASANCTION_LIST("Transaction_List");

    private String name;

    private CollectionNames(final String name) {
      this.name = name;
    }

    @Override
    public String toString() {
      return name;
    }
  }
}
