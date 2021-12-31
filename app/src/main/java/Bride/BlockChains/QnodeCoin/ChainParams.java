package Bride.BlockChains.QnodeCoin;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Data
public class ChainParams {
    private String RPCHost;
    private int RPCPort;
    private String RPCPassword;
    private String ControlWalletAddress;
    private String PrivateKey;
    private String Network;
    private float TransferFee;

}
