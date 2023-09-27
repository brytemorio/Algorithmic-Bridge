package bridge.services.storagservice;

import bridge.services.transactionservice.TransactionModels;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import lombok.extern.slf4j.Slf4j;
import org.bson.conversions.Bson;

import java.util.Map;
import java.util.stream.StreamSupport;

import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Filters.or;
import static bridge.services.storagservice.StorageServiceUtils.getKey;

@Slf4j
public class AddressMappingStorageService
{
  private MongoDatabase database;

  public AddressMappingStorageService()
  {
    this.database = MongoDatabaseFactory.getMongoDatabase();
  }


  public void saveAddressMapping(DataObjects.AddressMappingStorage addressMappingStorage)
  {
    var collection = getCollection();

    var result = collection.insertOne(addressMappingStorage);

    log.debug(result.toString());
  }


  // FIXME: Find a more efficient method for retrieving valid Address Mappings
  public String getAddressFromSavedMapping(Map<String, TransactionModels.MappedAddress> address,
                                           String targetBlockChainName)
  {

    var collection = getCollection();
    String key = getKey(address);
    TransactionModels.MappedAddress keyValue = address.get(key);

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
          if (key.equals(getKey(
              mappedAddress.getFromBlockChainAddress())) && (targetBlockChainName.equals(
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


  private MongoCollection<DataObjects.AddressMappingStorage> getCollection()
  {
    return this.database.getCollection(Collections.ADDRESS_MAPPING_STORAGE.toString(),
        DataObjects.AddressMappingStorage.class);
  }
}
