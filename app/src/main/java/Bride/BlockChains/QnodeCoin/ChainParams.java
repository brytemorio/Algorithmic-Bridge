package Bride.BlockChains.QnodeCoin;

import lombok.Data;


import java.util.Optional;

@Data
public class ChainParams {
    private String RPCHost;
    private int RPCPort;
    private String RPCPassword;
    private String ControlWalletAddress;
    private Optional<String> PrivateKey;
    private String Network;
    private float TransferFee;

}
