package bridge.services;

import bridge.blockchains.IBaseChain;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@AllArgsConstructor
public class BridgeValidationService
{
  public IBaseChain chain;

  public void validateAddress(String address)
  {
    this.chain.validateAddress(address);
  }
}
