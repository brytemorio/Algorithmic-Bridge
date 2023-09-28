package bridge.services.storagservice;

import bridge.blockchains.Asset;
import bridge.services.transactionservice.TransactionModels.MappedAddress;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Map;

public class DataObjects {

  private DataObjects() {}

  @Data
  public static final class BlockHeightStorage {

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
  public static final class AddressMappingStorage {
    private ObjectId id;

    /*
     * The fields fromBlockChainAddress and toBlockChainAddress
     * are Hashmaps containing the  mapping of the blockchain
     * ChainName as key to the users WalletAddress as value:
     * BlockChiainName => Address
     */

    // Wallet Address on the blockchain asset is been sent from
    private Map<String, MappedAddress> fromBlockChainAddress;

    // Wallet Address on the target blockchain asset is been sent to
    private Map<String, MappedAddress> toBlockChainAddress;

    public AddressMappingStorage() {}

    public AddressMappingStorage(
        final Map<String, MappedAddress> fromBlockChainAddress,
        final Map<String, MappedAddress> toBlockChainAddress) {
      this.fromBlockChainAddress = fromBlockChainAddress;
      this.toBlockChainAddress = toBlockChainAddress;
    }
  }


  /*Stores Information about a processed transaction*/
  @Data
  @NoArgsConstructor
  public  static final class PollingTransactionState
  {
    ObjectId id;
    private boolean ok;
    private int tries;


    public void incrementTries()
    {
      this.tries += 1;
    }

    public void markAsDone()
    {
      this.ok = true;
    }
  }

  // Save information about the last poller execution
  @Data
  public static class PollingState
  {
    private String chainIdentifier;
    private Map<String, PollingTransactionState> transactionMap;

    public PollingState(String chainIdentifier)
    {
      this.chainIdentifier =chainIdentifier;
    }
  }

  @Data
  public static class AssetStorage
  {
    private Map<String, ArrayList<Asset>> assets;

    public AssetStorage(Map<String, ArrayList<Asset>> assets)
    {
      this.assets = assets;
    }
  }

  @Data
  @NoArgsConstructor
  public static class ConfigurationStorage
  {
    private String chainName;
    private String node;
    private String network;
    private String networkId;
    private String chainIdentifier;
    private String gatewayAddress;
    private ArrayList<Asset> assets;
  }
}
