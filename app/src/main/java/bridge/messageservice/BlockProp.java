package bridge.messageservice;

import java.math.BigInteger;
import lombok.Data;

@Data
final class BlockProp {

  private String chainIdentifier;

  private BigInteger blockHeight;

  // TODO: implement logic that updates the block height in the Database Registry

}
