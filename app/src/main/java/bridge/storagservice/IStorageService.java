package bridge.storagservice;

import java.math.BigInteger;
import java.util.Map;
import bridge.transactionservice.TransactionModels.MappedAddress;
import bridge.storagservice.DataObjects.AddressMappingStorage;

public interface IStorageService {

  void setBlockHeightStorage(String chainIdentifier, BigInteger height);

  BigInteger getBlockHeightFromStorage(String chainIdentifier);

  void updateBlockHeightStorage(String chainIdentifier, BigInteger height);

  void saveAddressMapping(AddressMappingStorage addressMappingStorage);

  /*
   * Retrieves either the sending address or the receiving address from the saved mapping of both,
   * depending on the one that is passed to the function (first parameter). That is if the sending
   * address is passed then the receiving address is retrieved and vice versa.
   */
  String getAddressFromSavedMapping(
      Map<String, MappedAddress> address, String targetBlockChainName);
}
