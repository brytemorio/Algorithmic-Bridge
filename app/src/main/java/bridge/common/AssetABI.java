package bridge.common;

import lombok.Getter;

public final class AssetABI {
  @Getter final String assetName;
  @Getter final Class<?> assetContractABI;

  public AssetABI(final String assetName, final Class<?> assetContractABI) {
    this.assetName = assetName;
    this.assetContractABI = assetContractABI;
  }
}
