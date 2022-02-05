package bridge.messageservice;

import java.math.BigInteger;
import lombok.Data;

@Data
final class BlockProp {

  private String chainIdentifier;

  private BigInteger blockHeight;

  public BlockProp() {}

  /* TODO: When it becomes necessary, implement a preventive
   * measure for accidentally overriding states for an instance
   * of this once they have been initialized via the constructor
   * or the state's respective setter methods
   * */

}
