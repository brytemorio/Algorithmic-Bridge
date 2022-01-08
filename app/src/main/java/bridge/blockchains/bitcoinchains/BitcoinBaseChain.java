package bridge.blockchains.bitcoinchains;

import bridge.common.IChainQueryService;
import com.electronwill.nightconfig.core.Config;
import java.net.URL;
import java.util.List;
import lombok.Data;
import wf.bitcoin.javabitcoindrpcclient.BitcoinJSONRPCClient;
import wf.bitcoin.javabitcoindrpcclient.BitcoindRpcClient;

@Data
class BitcoinBaseChain implements IChainQueryService {
  private Config asset;
  private String hostName;
  private int rpcPort;
  private String rpcPassword;
  private String controlWalletAddress;
  private String network;
  private String chainIdentifier;
  private String rpcUser;

  private URL jasonRPCUrl;
  private static BitcoinJSONRPCClient bitcoinJSONRPCClient;

  public void init() {
    bitcoinJSONRPCClient = new BitcoinJSONRPCClient(jasonRPCUrl);
  }

  @Override
  public <T> List<T> getTrxOfBlockAtHeight(int height) {
    return IChainQueryService.super.getTrxOfBlockAtHeight(height);
  }

  @Override
  public String getTrxByID(String trxID) {
    String rawTrx = bitcoinJSONRPCClient.getRawTransactionHex(trxID);
    BitcoindRpcClient.RawTransaction trx = bitcoinJSONRPCClient.decodeRawTransaction(rawTrx);
    return trx.toString();
  }

  @Override
  public <T> T getTrxHash(int blockHeight) {
    return IChainQueryService.super.getTrxHash(blockHeight);
  }

  @Override
  public boolean validateAddress(String address) {
    return false;
  }
}
