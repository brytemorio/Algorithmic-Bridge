package bridge.BlockChains.BitcoinChains;

import bridge.common.ConfigObject;
import com.electronwill.nightconfig.core.CommentedConfig;
import java.net.MalformedURLException;
import java.net.URL;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public final class QnodecoinChain extends BitcoinBaseChain {

  private static final CommentedConfig configObject = ConfigObject.CONFIG;

  public QnodecoinChain() throws MalformedURLException {
    super.setAssetName(configObject.get("Blockchain.Qnodecoin.asset_name"));
    super.setNetwork(configObject.get("Blockchain.Qnodecoin.network"));
    super.setHostName(configObject.get("Blockchain.Qnodecoin.host"));
    super.setControlWalletAddress(configObject.get("Blockchain.Qnodecoin.control_wallet"));
    super.setPrivateKey(configObject.get("Blockchain.Qnodecoin.private_key"));
    super.setRpcPassword(configObject.get("Blockchain.Qnodecoin.rpc_password"));
    super.setRpcPort(configObject.get("Blockchain.Qnodecoin.rpc_port"));
    super.setChainIdentifier(configObject.get("Blockchain.Qnodecoin.chain_identifier"));
    super.setTransferFee(configObject.get("Blockchain.Qnodecoin.transfer_fee"));
    super.setRpcUser(configObject.get("Blockchain.Qnodecoin.rpc_user"));
    super.setJasonRPCUrl(setRPCUrl());
    super.init();
  }

  private String setRPCUrl() throws MalformedURLException {
    return new URL(
            "http://"
                + super.getRpcUser()
                + ':'
                + super.getRpcPassword()
                + "@"
                + super.getHostName()
                + ":"
                + super.getRpcPort()
                + "/")
        .toString();
  }
}
