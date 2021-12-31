package Bride.BlockChains.Waves;

import lombok.Data;

import java.util.Optional;

@Data
public class ChainParams {
    private String Node;
    private String ControlWalletAddress;
    private String PrivateKey;
    private String Network;
    private Optional<Character> NetworkSymbol;
    private float TransferFee;
}
