package bridge.blockchains.waves;

import bridge.common.IBaseChain;
import com.electronwill.nightconfig.core.Config;
import com.wavesplatform.wavesj.Block;
import com.wavesplatform.wavesj.Node;
import java.util.List;
import lombok.*;

@Data
class WavesIBaseChain<K> implements IBaseChain {

  @Setter(AccessLevel.PROTECTED)
  private String networkNode;

  @Setter(AccessLevel.PROTECTED)
  private String network;

  @Setter(AccessLevel.PROTECTED)
  private K networkID;

  @Setter(AccessLevel.PROTECTED)
  private String chainIdentifier;

  @Setter(AccessLevel.PROTECTED)
  @Getter(AccessLevel.PROTECTED)
  private Config asset;

  @Setter(AccessLevel.NONE)
  private String assetName;

  @Setter(AccessLevel.NONE)
  private String assetID;

  @Setter(AccessLevel.NONE)
  private String assetTicker;

  @Setter(AccessLevel.NONE)
  private Config assetControlWallet;

  @Setter(AccessLevel.NONE)
  private double transferFee;

  @Setter(AccessLevel.NONE)
  @Getter(AccessLevel.NONE)
  private Node rpcClient;

  @Override
  public void init() {
    this.assetID = asset.get("asset_id");
    this.assetName = asset.get("name");
    this.assetTicker = asset.get("ticker");
    this.transferFee = asset.get("transfer_fee");
    this.assetControlWallet = asset.get("wallet");
    this.rpcClient = getNodeObj();
  }

  @SneakyThrows
  private Node getNodeObj() {
    return new Node(networkNode);
  }

  @Override
  @SneakyThrows
  public Integer getBlockHeight() {
    return rpcClient.getHeight();
  }

  @Override
  @SneakyThrows
  public String getNodeResponse(String nodeEndpoint) {
    String fullRPCQueryPath = networkNode + nodeEndpoint;
    return IBaseChain.super.getNodeResponse(fullRPCQueryPath);
  }

  @Override
  @SneakyThrows
  public Block getTrxOfBlockAtHeight(int height) {
    return rpcClient.getBlock(height);
  }

  @Override
  public String getTrxByID(String trxID) {
    return IBaseChain.super.getTrxByID(trxID);
  }

  @Override
  public <T> List<T> getTrxHash(int blockHeight) {
    return IBaseChain.super.getTrxHash(blockHeight);
  }

  @Override
  public boolean validateAddress(String address) {
    return false;
  }
}
