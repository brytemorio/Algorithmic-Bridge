package bridge.BlockChains.EthChains;

import bridge.common.IChainQueryService;
import java.util.List;
import lombok.Data;

@Data
public class EthBaseChain implements IChainQueryService {

  private String assetName;
  private String tickerSymbol;
  private String networkNode;
  private String network;
  private int networkID;
  private String controlWallet;
  private String privateKey;
  private String assetId;
  private String chainIdentifier;
  private double transferFee;

  public void init() {
    // implementation for later
  }

  @Override
  public <T> List<T> getTrxOfBlockAtHeight(int height) {
    return IChainQueryService.super.getTrxOfBlockAtHeight(height);
  }

  @Override
  public String getTrxByID(String trxID) {
    return IChainQueryService.super.getTrxByID(trxID);
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
