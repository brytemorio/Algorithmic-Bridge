package bridge.blockchains.ethchains;

import java.lang.reflect.Method;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import org.agrona.collections.Object2ObjectHashMap;
import org.web3j.abi.FunctionReturnDecoder;
import org.web3j.abi.datatypes.Function;
import org.web3j.abi.datatypes.Type;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameter;
import org.web3j.protocol.core.methods.response.EthBlock;
import org.web3j.protocol.core.methods.response.EthBlockNumber;
import org.web3j.protocol.http.HttpService;
import org.web3j.tx.gas.DefaultGasProvider;
import com.electronwill.nightconfig.core.Config;
import bridge.abiwrapper.ERC20ABI;
import bridge.abiwrapper.QnodeCoinABI;
import bridge.blockchains.IBaseChain;
import bridge.common.AssetABI;
import bridge.common.BridgeUtils;
import bridge.common.TransactionModels.Transaction;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;

@SuppressWarnings("unchecked")
class EthIBaseChain<K> implements IBaseChain {

  private BigInteger previousBlockHeight = BigInteger.ZERO;

  @Setter(AccessLevel.PROTECTED)
  @Getter(AccessLevel.PROTECTED)
  Object2ObjectHashMap<String, Config> asset;



  @Getter
  @Setter(AccessLevel.PROTECTED)
  private String networkNode;

  @Getter
  @Setter(AccessLevel.PROTECTED)
  private String network;

  @Getter
  @Setter(AccessLevel.PROTECTED)
  private K networkID;

  @Getter
  @Setter(AccessLevel.PROTECTED)
  private String chainIdentifier;

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



  public Config getAssetWallet(String assetConfigName) {
    EthAssets assetInfo = assetInfoFactory(assetConfigName);
    return assetInfo.getAssetControlWallet();
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
  public org.web3j.protocol.core.methods.response.Transaction getTrxByID(String trxID) {
    return web3j.ethGetTransactionByHash(trxID).send().getResult();
  }


  @Override
  public boolean validateAddress(String address) {
    return false;
  }

  @Override
  public Transaction getTransaction(String trxID, String assetName) {
    String assetId = getAssetID(assetName);
    ERC20ABI erc20abi = getABIWrapper(assetName);
    var transaction = getTrxByID(trxID);
    if (!assetId.equals(transaction.getTo())) return null;
    Function transferFunction = new Function((erc20abi.FUNC_TRANSFER,
        Arrays.asList(erc20abi. ),
        erc20abi.TRANSFER_EVENT.getParameters());
    System.out.println(FunctionReturnDecoder.decode(transaction.getTo(), erc20abi.TRANSFER_EVENT.));

    // TODO: Complete this function
    return null;
  }

  class EthAssets {

    @Getter
    private String assetID;

    @Getter
    private String assetName;

    @Getter
    private String assetTicker;

    @Getter
    private Double transferFee;

    @Getter
    private Config assetControlWallet;



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

  // ========================= Helper Functions ================//
  private EthAssets assetInfoFactory(String assetConfigName) {
    return new EthAssets(asset.get(assetConfigName));
  }

  @SneakyThrows
  private ERC20ABI getABIWrapper(String assetName) {
    String assetId = getAssetID(assetName);
    Credentials assetWalletCredential = Credentials.create(
        getAssetWallet(assetName).get("private_key"), getAssetWallet(assetName).get("public_key"));
    return ERC20ABI.load(assetId, web3j, assetWalletCredential, new DefaultGasProvider());
  }

  class Address implements Type<String> {

    @Override
    public String getValue() {
      // TODO Auto-generated method stub
      return new String();
    }

    @Override
    public String getTypeAsString() {
      // TODO Auto-generated method stub
      return new String().toString();
    }

  }

  class Unit256 implements Type<BigInteger> {

    @Override
    public BigInteger getValue() {
      // TODO Auto-generated method stub
      return new BigInteger();
    }

    @Override
    public String getTypeAsString() {
      // TODO Auto-generated method stub
      return null;
    }

  }

}
