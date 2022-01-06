package bridge.BlockChains.BitcoinChains;

import bridge.common.IChainQueryService;
import com.electronwill.nightconfig.core.Config;
import java.net.MalformedURLException;
import java.util.List;
import lombok.Data;
import wf.bitcoin.javabitcoindrpcclient.BitcoinJSONRPCClient;
import wf.bitcoin.javabitcoindrpcclient.BitcoindRpcClient;

@Data
public class BitcoinBaseChain implements IChainQueryService {
  private Config assetName;
  private String hostName;
  private int rpcPort;
  private String rpcPassword;
  private String controlWalletAddress;
  private String privateKey;
  private String network;
  private String chainIdentifier;
  private String rpcUser;
  private double transferFee;

  private String jasonRPCUrl;
  private static BitcoinJSONRPCClient bitcoinJSONRPCClient;

  public void init() throws MalformedURLException {
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
