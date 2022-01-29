package bridge.messageservice;

import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;

@Data
final class BlockProp {

  @Setter(AccessLevel.NONE)
  private String chainIdentifier;

  @Setter(AccessLevel.NONE)
  private Number blockHeight;

  @Setter(AccessLevel.NONE)
  @Getter(AccessLevel.NONE)
  private Number previousBlockHeight;

  public void setChainIdentifier(String chainIdentifier) {
    this.chainIdentifier = chainIdentifier;
  }

  @SneakyThrows
  public void setBlockHeight(Number blockHeight) {
    if (blockHeight.longValue() > previousBlockHeight.longValue()) {
      this.blockHeight = blockHeight;
      previousBlockHeight = this.blockHeight;
      // TODO: implement logic that updates the block height in the Database Registry
    } else {
      Thread.sleep(2000);
    }
  }
}
