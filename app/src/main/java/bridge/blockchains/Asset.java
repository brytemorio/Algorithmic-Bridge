package bridge.blockchains;

import javax.annotation.Nullable;
import org.bson.types.ObjectId;
import com.electronwill.nightconfig.core.Config;
import lombok.Data;
import lombok.Getter;

@Data
public class Asset {
  ObjectId Id;

  private String assetName;
  private String ticker;

  // Bitcoin class of blockchains do not require an assetId(Contract Address of token) so null could
  // be passed in place
  @Nullable private String assetId;

  // Enforcing a transfer fee is optional. This transfer fee is not the same as the fees required
  // for on-chain transaction e.g etheruem gas fee
  private double transferFee;

  // Currency Decimal places peculiar to the asset
  private int decimals;

  private Wallet wallet;

  // Swap ratio defines the the transfer ration between assets on difference block chain. (Like
  // 3.2units of token A on blockchain A been swap for 1unit of token B on blockchain B)
  private double swapRatio; // TODO: Implement swapRation

  public Asset() {}

  public Asset(Config assetInfo) {
    this.assetId = assetInfo.get("name");
    this.ticker = assetInfo.get("ticker");
    this.assetId = assetInfo.get("asset_id");
    this.transferFee = assetInfo.get("transfer_fee");
    this.decimals = assetInfo.get("decimals");
    this.wallet =
        new Wallet(assetInfo.get("wallet.private_key"), assetInfo.get("wallet.public_key"));
  }

  private static class Wallet {

    // private key can be null if the it not required internally by the bridge to handle
    // transactions
    @Nullable @Getter private String privateKey;

    @Getter private String publicKey;

    public Wallet() {}

    public Wallet(final String privateKey, final String publicKey) {
      this.privateKey = privateKey;
      this.publicKey = publicKey;
    }
  }
}
