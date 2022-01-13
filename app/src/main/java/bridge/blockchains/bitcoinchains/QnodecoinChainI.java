package bridge.blockchains.bitcoinchains;

import bridge.common.ConfigFileObj;
import com.electronwill.nightconfig.core.UnmodifiableConfig;
import java.net.MalformedURLException;
import java.net.URL;
import lombok.extern.java.Log;

@Log
public final class QnodecoinChainI extends BitcoinIBaseChain {

  private static final UnmodifiableConfig configObject = ConfigFileObj.CONFIG;

  public QnodecoinChainI() throws MalformedURLException {
    super.setAsset(configObject.get("Blockchain.Qnodecoin.asset"));
    super.setNetwork(configObject.get("Blockchain.Qnodecoin.network"));
    super.setHostName(configObject.get("Blockchain.Qnodecoin.host"));
    super.setControlWalletAddress(configObject.get("Blockchain.Qnodecoin.control_wallet"));
    super.setRpcPassword(configObject.get("Blockchain.Qnodecoin.rpc_password"));
    super.setRpcPort(configObject.get("Blockchain.Qnodecoin.rpc_port"));
    super.setChainIdentifier(configObject.get("Blockchain.Qnodecoin.chain_identifier"));
    super.setRpcUser(configObject.get("Blockchain.Qnodecoin.rpc_user"));
    super.setJasonRPCUrl(setRPCUrl());
    super.init();
  }

  private URL setRPCUrl() throws MalformedURLException {
    return new URL(
        "http://"
            + super.getRpcUser()
            + ':'
            + super.getRpcPassword()
            + "@"
            + super.getHostName()
            + ":"
            + super.getRpcPort()
            + "/");
  }
}
