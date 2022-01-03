package Bridge.BlockChains.EthChains;

import com.electronwill.nightconfig.core.conversion.AdvancedPath;
import lombok.Data;

@Data
public class EthChainParams {
    @AdvancedPath({})
    private String Node;
    private String ControlWalletAddress;
    private String PrivateKey;
    private String Network;
    private float TransferFee;
}
