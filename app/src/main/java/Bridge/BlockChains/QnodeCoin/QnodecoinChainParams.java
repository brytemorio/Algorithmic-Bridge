package Bridge.BlockChains.QnodeCoin;

import com.electronwill.nightconfig.core.conversion.AdvancedPath;
import lombok.Data;

@Data
public class QnodecoinChainParams {

  @AdvancedPath({"Blockchain", "Qnodecoin", "host"})
  private String Host;

  @AdvancedPath({"Blockchain", "Qnodecoin", "rpc_port"})
  private int RPCPort;

  @AdvancedPath({"Blockchain", "Qnodecoin", "rpc_password"})
  private String RPCPassword;

  @AdvancedPath({"Blockchain", "Qnodecoin", "control_wallet"})
  private String ControlWalletAddress;

  @AdvancedPath({"Blockchain", "Qnodecoin", "private_key"})
  private String PrivateKey;

  @AdvancedPath({"Blockchain", "Qnodecoin", "network"})
  private String Network;

  @AdvancedPath({"Blockchain", "Qnodecoin", "transfer_fee"})
  private double TransferFee;
}
