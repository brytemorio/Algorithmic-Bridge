package bridge.blockchains.ethchains;

import java.math.BigInteger;
import java.util.ArrayList;
import org.agrona.collections.Object2ObjectHashMap;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameter;
import org.web3j.protocol.core.methods.response.EthBlock;
import org.web3j.protocol.core.methods.response.EthBlock.Block;
import org.web3j.protocol.core.methods.response.EthBlockNumber;
import org.web3j.protocol.http.HttpService;
import com.electronwill.nightconfig.core.Config;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import bridge.blockchains.IBaseChain;
import bridge.common.BridgeUtils;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;

@SuppressWarnings("unchecked")
@Data
class EthIBaseChain<K> implements IBaseChain {

  @Setter(AccessLevel.NONE)
  @Getter(AccessLevel.NONE)
  private final Gson gsonParser = new GsonBuilder().create();

  @Setter(AccessLevel.NONE)
  @Getter(AccessLevel.NONE)
  private BigInteger previousBlockHeight = BigInteger.ZERO;

  @Setter(AccessLevel.PROTECTED)
  @Getter(AccessLevel.PROTECTED)
  Object2ObjectHashMap<String, Config> asset;

  @Setter(AccessLevel.PROTECTED)
  private String networkNode;

  @Setter(AccessLevel.PROTECTED)
  private String network;

  @Setter(AccessLevel.PROTECTED)
  private K networkID;

  @Setter(AccessLevel.PROTECTED)
  private String chainIdentifier;

  @Setter(AccessLevel.NONE)
  @Getter(AccessLevel.NONE)
  private Web3j web3j;

  public String getAssetID(String assetConfigName) {
    EthAssets assetInfo = assetInfoFactory(assetConfigName);
    return assetInfo.getAssetID();
  }

  public String getAssetTicker(String assetConfigName) {
    EthAssets assetInfo = assetInfoFactory(assetConfigName);
    return assetInfo.getAssetTicker();
  }

  public String getAssetName(String assetConfigName) {
    EthAssets assetInfo = assetInfoFactory(assetConfigName);
    return assetInfo.getAssetName();
  }

  public Double getAssetTransferFee(String assetConfigName) {
    EthAssets assetInfo = assetInfoFactory(assetConfigName);
    return assetInfo.getTransferFee();
  }

  private EthAssets assetInfoFactory(String assetConfigName) {
    return new EthAssets(asset.get(assetConfigName));
  }

  protected void init() {
    this.web3j = initWeb3j();
  }

  @SneakyThrows
  private Web3j initWeb3j() {
    return Web3j.build(new HttpService(networkNode));
  }

  @Override
  @SneakyThrows
  public BigInteger getBlockHeight() {
    EthBlockNumber ethBlockNumber;
    boolean noNewBlockFound = true;
    BigInteger currentHeight;
    while (noNewBlockFound) {
      ethBlockNumber = web3j.ethBlockNumber().send();
      currentHeight = ethBlockNumber.getBlockNumber();
      if (currentHeight.compareTo(this.previousBlockHeight) > 0) {
        this.previousBlockHeight = currentHeight;
        noNewBlockFound = false;
      }
    }
    return this.previousBlockHeight;
  }

  @Override
  @SneakyThrows
  public ArrayList<String> getTrxIdsByBlockHeight(BigInteger height) {
    EthBlock.Block block =
        web3j.ethGetBlockByNumber(DefaultBlockParameter.valueOf(height), false).send().getBlock();
    ArrayList<String> trxIds = new ArrayList<>();
    for (var trx : block.getTransactions()) {
      trxIds.add(BridgeUtils.object2JsonConverter(trx).get("value").toString());
    }
    return trxIds;
  }

  @Override
  @SneakyThrows
  public Block getTrxByID(String trxID) {
    return web3j.ethGetBlockByHash(trxID, true).send().getBlock();
  }

  @Override
  public <Integer> ArrayList<String> getTrxHash(Integer blockHeight) {
    return new ArrayList<>();
  }

  @Override
  public boolean validateAddress(String address) {
    return false;
  }

  @Data
  class EthAssets {

    @Setter(AccessLevel.NONE)
    private String assetID;

    @Setter(AccessLevel.NONE)
    private String assetName;

    @Setter(AccessLevel.NONE)
    private String assetTicker;

    @Setter(AccessLevel.NONE)
    private Double transferFee;

    @Setter(AccessLevel.NONE)
    private Config assetControlWallet;

    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    private Config asset;

    public EthAssets(final Config asset) {
      this.asset = asset;
      this.assetID = this.asset.get("asset_id");
      this.assetName = this.asset.get("name");
      this.assetTicker = this.asset.get("ticker");
      this.transferFee = this.asset.get("transfer_fee");
      this.assetControlWallet = this.asset.get("wallet");
    }
  }
}
