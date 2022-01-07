package bridge.BlockChains.EthChains;

import bridge.common.IChainQueryService;
import java.util.List;
import java.util.Map;
import lombok.Data;
import org.web3j.protocol.Web3j;

@Data
public class EthBaseChain<T> implements IChainQueryService {

  // private Config asset;
  private String networkNode;
  private String network;
  private T networkID;
  private String chainIdentifier;

  private static Web3j web3j;
  /*public void init(Map<String, Map<String, String>> asset) {
    this.asset = asset;
    AssetParameter assetParameter = new AssetParameter();
    assetParameter.assetID = asset
    web3j =  Web3j.build(new HttpService(node));

    // implementation for later
  }

   */

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

  static class AssetParameter {
    public String assetName;
    public String assetTicker;
    public String assetID;
    public double transferFee;
    public Map<String, String> assetControlWallet;
  }
}
