package bridge.blockchains.waves;

import bridge.common.BaseChainInterface;
import com.electronwill.nightconfig.core.Config;
import java.util.List;
import lombok.*;
import org.apache.commons.lang3.StringUtils;

@Data
class WavesBaseChain<K> implements BaseChainInterface {

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

  @Override
  public void init() {
    this.assetID = this.asset.get("asset_id");
    this.assetName = this.asset.get("name");
    this.assetTicker = this.asset.get("ticker");
    this.transferFee = this.asset.get("transfer_fee");
    this.assetControlWallet = this.asset.get("wallet");
  }

  @Override
  @SneakyThrows
  public Config getNodeResponse(String nodeEndpoint) {
    String fullRPCQueryPath;
    if (networkNode.endsWith("/")) {
      if (nodeEndpoint.startsWith("/")) {
        fullRPCQueryPath = StringUtils.chop(networkNode) + nodeEndpoint;
      } else {
        fullRPCQueryPath = networkNode + nodeEndpoint;
      }
    } else if (!networkNode.endsWith("/") || !nodeEndpoint.startsWith("/")) {
      fullRPCQueryPath = networkNode + "/" + nodeEndpoint;
    } else {
      fullRPCQueryPath = networkNode + nodeEndpoint;
    }
    // String fullRPCQueryPath = networkNode.st + nodeEndpoint;
    return BaseChainInterface.super.getNodeResponse(fullRPCQueryPath);
  }

  @Override
  public <T> List<T> getTrxOfBlockAtHeight(int height) {
    return BaseChainInterface.super.getTrxOfBlockAtHeight(height);
  }

  @Override
  public String getTrxByID(String trxID) {
    return BaseChainInterface.super.getTrxByID(trxID);
  }

  @Override
  public <T> T getTrxHash(int blockHeight) {
    return BaseChainInterface.super.getTrxHash(blockHeight);
  }

  @Override
  public boolean validateAddress(String address) {
    return false;
  }
}
