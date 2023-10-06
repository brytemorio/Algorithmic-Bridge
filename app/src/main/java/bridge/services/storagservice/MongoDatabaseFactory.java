package bridge.services.storagservice;

import bridge.services.transactionservice.TransactionModels;
import bridge.services.transactionservice.TransactionModels.TransactionAttemptList;
import com.electronwill.nightconfig.core.UnmodifiableConfig;
import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.ServerApi;
import com.mongodb.ServerApiVersion;
import com.mongodb.client.*;
import lombok.Data;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.ClassModel;
import org.bson.codecs.pojo.PojoCodecProvider;

import java.util.Objects;

import static bridge.common.ConfigFileObj.CONFIG;
import static org.bson.codecs.configuration.CodecRegistries.fromProviders;
import static org.bson.codecs.configuration.CodecRegistries.fromRegistries;


/*FIXME: This class is in need of heavy refactoring*/
@Slf4j
@Data
public class MongoDatabaseFactory
{

  private static final UnmodifiableConfig configfile = CONFIG;


  private final MongoDatabase dataBase;

  private MongoDatabaseFactory()
  {
    // the name of database to be used in MongoDB
    String databaseName = Objects.requireNonNullElse(
        MongoDatabaseFactory.configfile.get("Bridge.database.name"), "Algo-Bridge");
    String dbHostName = Objects.requireNonNullElse(
        MongoDatabaseFactory.configfile.get("Bridge.database.hostname"), "localhost");
    int dbPort = Objects.requireNonNullElse(
        MongoDatabaseFactory.configfile.get("Bridge.database" + ".port"), 27017);
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

ClassModel<DataObjects.PollingTransactionState> pollingTransactionStateModel = ClassModel.builder(
    DataObjects.PollingTransactionState.class).enableDiscriminator(true).build();

    ClassModel<DataObjects.AssetStorage> assetStorageClassModel = ClassModel.builder(
        DataObjects.AssetStorage.class).enableDiscriminator(true).build();

    ClassModel<DataObjects.ConfigurationStorage> configurationStorageClassModel = ClassModel.builder(
        DataObjects.ConfigurationStorage.class).enableDiscriminator(true).build();

    ClassModel<DataObjects.Wallet> walletClassModel = ClassModel.builder(DataObjects.Wallet.class)
        .enableDiscriminator(true).build();

    ClassModel<TransactionModels.TransactionAttempt> transactionAttemptClassModel = ClassModel.builder(
        TransactionModels.TransactionAttempt.class).enableDiscriminator(true).build();

    ClassModel<TransactionModels.TransactionAttemptListTrigger> transactionAttemptListTriggerClassModel = ClassModel.builder(
        TransactionModels.TransactionAttemptListTrigger.class).enableDiscriminator(true).build();

    ClassModel<TransactionModels.TransactionSender> transactionSenderClassModel = ClassModel.builder(
        TransactionModels.TransactionSender.class).enableDiscriminator(true).build();

    ClassModel<TransactionModels.TransactionReceiver> transactionReceiverClassModel = ClassModel.builder(
        TransactionModels.TransactionReceiver.class).enableDiscriminator(true).build();

    ClassModel<TransactionModels.TransactionAttemptReceiver> transactionAttemptReceiverClassModel = ClassModel.builder(
        TransactionModels.TransactionAttemptReceiver.class).enableDiscriminator(true).build();

    ClassModel<TransactionModels.MappedAddress> mappedAddressClassModel = ClassModel.builder(
        TransactionModels.MappedAddress.class).enableDiscriminator(true).build();

    ClassModel<TransactionModels.Transaction> transactionClassModel = ClassModel.builder(
        TransactionModels.Transaction.class).enableDiscriminator(true).build();

    CodecRegistry codecRegistry = fromRegistries(MongoClientSettings.getDefaultCodecRegistry(),
        fromProviders(PojoCodecProvider.builder()
            .register(blockHeightStorageModel, addressMappingStroageModel,
                transactionAttemptListStorageModel, transactionPolllingStateModel,
                assetStorageClassModel, configurationStorageClassModel, walletClassModel,
                transactionAttemptClassModel, transactionAttemptListTriggerClassModel,
                transactionSenderClassModel, transactionReceiverClassModel,
                transactionAttemptReceiverClassModel, mappedAddressClassModel,
                transactionClassModel,  pollingTransactionStateModel).automatic(true).build()));

    ServerApi serverApi = ServerApi.builder().version(ServerApiVersion.V1).build();
    ConnectionString connectionString = new ConnectionString(connectionURL);
    MongoClientSettings mongoSettings = MongoClientSettings.builder().codecRegistry(codecRegistry)
        .retryReads(true).retryWrites(false).serverApi(serverApi)
        .applyConnectionString(connectionString).build();
    MongoClient mongoClient = MongoClients.create(mongoSettings);
    this.dataBase = mongoClient.getDatabase(databaseName);
  }

  public static MongoDatabase getMongoDatabase()
  {
    return new MongoDatabaseFactory().getDataBase();
  }

}
