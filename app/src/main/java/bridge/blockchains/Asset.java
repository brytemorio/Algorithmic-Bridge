package bridge.blockchains;

import javax.annotation.Nullable;

import bridge.services.storagservice.DataObjects;
import org.bson.types.ObjectId;
import com.electronwill.nightconfig.core.Config;
import lombok.Data;
import lombok.Getter;

import java.util.Objects;

import static bridge.services.storagservice.DataObjects.Wallet;

@Data
public class Asset
{
  ObjectId Id;

  private String assetName;
  private String ticker;

  // Bitcoin class of blockchains do not require an assetId(Contract Address of token) so null could
  // be passed in place
  @Nullable
  private String assetId;

  // Enforcing a transfer fee is optional. This transfer fee is not the same as the fees required
  // for on-chain transaction e.g etheruem gas fee
  private int transferFee;

  // Currency Decimal places peculiar to the asset
  private int decimals;

  private DataObjects.Wallet wallet;

  // Swap ratio defines the transfer ration between assets on difference block chain. (Like
  // 3.2units of token A on blockchain A been swap for 1unit of token B on blockchain B)
  private int swapRatio; // TODO: Implement swapRation

  public Asset()
  {
  }

  public Asset(Config assetInfo)
  {
    this.assetName = Objects.requireNonNullElse(assetInfo.get("name"), "name of asset not empty");
    this.ticker = Objects.requireNonNull(assetInfo.get("ticker"), "ticker of asset not empty");
    String asset_id = assetInfo.get("asset_id");
    this.assetId = Objects.equals(asset_id, "") || Objects.equals(asset_id, " ") ? null : asset_id;
    this.transferFee = Objects.requireNonNullElse(assetInfo.get("transfer_fee"), 0);
    this.decimals = Objects.requireNonNull(assetInfo.get("decimals"), "decimal cannot empty");
    this.wallet = new Wallet(
        Objects.requireNonNull(assetInfo.get("wallet.private_key"), "private key cannot be empty"),
        Objects.requireNonNull(assetInfo.get("wallet.public_key"), "public key cannot be empty"));
  }

}
