package bridge.BlockChains.BitcoinChains;

import bridge.common.IChainQueryService;
import java.util.List;
import lombok.Data;

@Data
public class BitcoinBaseChain implements IChainQueryService {
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

  @Override
  public <T> List<T> getTrxOfBlockAtHeight(int height) {
    return IChainQueryService.super.getTrxOfBlockAtHeight(height);
  }

  @Override
  public String getTrxByID(String trxID) {
    return null;
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
