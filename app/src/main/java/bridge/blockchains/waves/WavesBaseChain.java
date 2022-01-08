package bridge.blockchains.waves;

import bridge.common.IChainQueryService;
import lombok.Data;

@Data
public class WavesBaseChain implements IChainQueryService {
  private String assetName;
  private String tickerSymbol;
  private String node;
  private String network;
  private String networkId;
  private String publicKey;
  private String privateKey;
  private String controlWallet;
  private String assetId;
  private String chainIdentifier;
  private double transferFee;

  public void init() {
    // TODO: implementation of WavesBaseChain.init()

  }
}
