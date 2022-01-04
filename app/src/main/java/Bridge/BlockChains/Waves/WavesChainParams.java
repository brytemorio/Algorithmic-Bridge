package Bridge.BlockChains.Waves;

import com.electronwill.nightconfig.core.conversion.AdvancedPath;
import lombok.Data;

@Data
public class WavesChainParams {
  @AdvancedPath({"Blockchain", "Waves", "node"})
  private String Node;

  @AdvancedPath({"Blockchain", "Waves", "private_key"})
  private String PrivateKey;

  @AdvancedPath({"Blockchain", "Waves", "public_key"})
  private String PublicKey;

  @AdvancedPath({"Blockchain", "Waves", "network"})
  private String Network;

  @AdvancedPath({"Blockchain", "Waves", "network_id"})
  private String NetworkSymbol;

  @AdvancedPath({"Blockchain", "Waves", "transfer_fee"})
  private double TransferFee;

  @AdvancedPath({"Blockchain", "Waves", "asset_id"})
  private String AssetID;
}
