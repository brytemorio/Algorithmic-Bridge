package Bride.BlockChains.EthChains;

import lombok.Data;

@Data
public class ChainParams {
    private String Node;
    private String ControlWalletAddress;
    private String PrivateKey;
    private String Network;
    private float TransferFee;
}
