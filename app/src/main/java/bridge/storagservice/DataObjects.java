package bridge.storagservice;

import java.math.BigInteger;
import java.util.Map;
import org.bson.types.ObjectId;
import bridge.transactionservice.TransactionModels.MappedAddress;
import lombok.Data;

public class DataObjects {

  private DataObjects() {}

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
    private Map<String, MappedAddress> fromBlockChainAddress;

    // Wallet Address on the target blockchain asset is been sent to
    private Map<String, MappedAddress> toBlockChainAddress;

    public AddressMappingStorage() {}

    AddressMappingStorage(
        final Map<String, MappedAddress> fromBlockChainAddress,
        final Map<String, MappedAddress> toBlockChainAddress) {
      this.fromBlockChainAddress = fromBlockChainAddress;
      this.toBlockChainAddress = toBlockChainAddress;
    }
  }
}
