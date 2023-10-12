package bridge.blockchains.ethchains;

import bridge.blockchains.ethchains.abiwrapper.ERC20ABI;
import bridge.blockchains.IBaseChain;
import bridge.common.BridgeUtils;
import bridge.common.RateLimiter;
import bridge.services.storagservice.DataObjects;
import bridge.services.storagservice.TransactionAttemptListStorageService;
import bridge.services.transactionservice.TransactionModels;
import bridge.services.transactionservice.TransactionModels.Transaction;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.web3j.abi.FunctionReturnDecoder;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Address;
import org.web3j.abi.datatypes.Function;
import org.web3j.abi.datatypes.Type;
import org.web3j.abi.datatypes.generated.Uint256;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.WalletUtils;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameter;
import org.web3j.protocol.core.methods.response.EthBlock;
import org.web3j.protocol.http.HttpService;
import org.web3j.tx.gas.DefaultGasProvider;

import java.math.BigInteger;
import java.util.*;

import bridge.blockchains.ethchains.abiwrapper.ERC20ABI;

@Slf4j
class EthIBaseChain<K> implements IBaseChain
{

  private BigInteger previousBlockHeight;

  @Getter
  private Web3j web3j;
  private TransactionAttemptListStorageService trxAttempListStorage;

  @Setter
  @Getter
  private DataObjects.ConfigurationStorage ethChainConfig;


  //private ERC20ABI erc20ABI;
  private final int RATELIMITER_CAPACITY = 2;
  private final int RATELIMITER_REFILL_RATE = 1;

  protected void init()
  {
    this.web3j = initWeb3j();
    this.previousBlockHeight = BigInteger.ZERO;
    this.trxAttempListStorage = new TransactionAttemptListStorageService();

  }

  @SneakyThrows
  private Web3j initWeb3j()
  {
    return Web3j.build(new HttpService(this.ethChainConfig.getNode()));
  }

  @Override
  @SneakyThrows
  public BigInteger getBlockHeight() throws Exception
  {
    RateLimiter rateLimiter = new RateLimiter(RATELIMITER_CAPACITY, RATELIMITER_REFILL_RATE);
    BigInteger currentHeight = web3j.ethBlockNumber().send().getBlockNumber();
    //BigInteger nextHeight = web3j.ethBlockNumber().send().getBlockNumber();

    return web3j.ethBlockNumber().send().getBlockNumber();
    /*boolean noNewBlockFound = true;

    while (noNewBlockFound){
      if(currentHeight.compareTo(previousBlockHeight) > 0){
        previousBlockHeight = currentHeight;
        noNewBlockFound = false;
      }

    }

    return previousBlockHeight;*/

    /*do
    {
      if (rateLimiter.allowRequest())
      {
        nextHeight = web3j.ethBlockNumber().send().getBlockNumber();
      }
      else
      {
        rateLimiter = new RateLimiter(RATELIMITER_CAPACITY, RATELIMITER_REFILL_RATE);
      }
    }
    while (currentHeight.equals(nextHeight));*/


  }

  @Override
  public String getChainIdentifier()
  {
    return this.ethChainConfig.getChainIdentifier();
  }

  @Override
  @SneakyThrows
  public ArrayList<String> getTrxIdsByBlockHeight(BigInteger height)
  {
    EthBlock.Block block = web3j.ethGetBlockByNumber(DefaultBlockParameter.valueOf(height), false)
        .send().getBlock();
    ArrayList<String> trxIds = new ArrayList<>();
    for (var trx : block.getTransactions())
    {
      trxIds.add(BridgeUtils.object2JsonConverter(trx).get("value").toString());
    }
    return trxIds;
  }


  @Override
  @SneakyThrows
  public org.web3j.protocol.core.methods.response.Transaction getTrxByID(String trxID)
  {
    return web3j.ethGetTransactionByHash(trxID).send().getResult();
  }


  @SneakyThrows
  @Override
  public boolean validateAddress(String address)
  {
    return WalletUtils.isValidAddress(address);
  }

  @Override
  public String getChainPublicGatewayAddress()
  {
    return this.ethChainConfig.getGatewayAddress();
  }

  @Override
  public Transaction sendCoin(TransactionModels.TransactionAttempt attempt)
  {
    return null;
  }


  @Override
  public Boolean filterTransactions(Transaction trx)
  {
    return null;
  }

  @Override
  public void handleTransaction(Transaction trx)
  {

  }

  @Override
  public String getChainName()
  {
    return this.ethChainConfig.getChainName();
  }

  @SneakyThrows
  @Override
  public Transaction getTransaction(String trxID)
  {
    //log.info(getTrxByID(trxID).getTo());
    return convertNodeResponseToTransaction(getTrxByID(trxID));
    //return null;
  }


  // ========================= Helper Functions ================//


  @SneakyThrows
  private Transaction convertNodeResponseToTransaction(
      org.web3j.protocol.core.methods.response.Transaction res)
  {

    String toAddress = res.getTo();
    String assetName = null;
    Integer assetDecimals = null;

    for (var asset : this.ethChainConfig.getAssets())
    {
      if (toAddress.equalsIgnoreCase(asset.getAssetId()))
      {
        assetName = asset.getAssetName();
        assetDecimals = asset.getDecimals();
        break;
      }
      else
      {
        return null;
      }
    }


    String input = res.getInput();

    // Decode the transaction input using web3j
    //We only expect a transfer or transferFrom function
    var decodedTranferFromFunc = FunctionReturnDecoder.decode(input,
        getTransferFromFunction().getOutputParameters());

    BigInteger amount;
    String receiver;


    //if it is a transferFrom function
    if (decodedTranferFromFunc.get(2) != null)
    {
      receiver = ((Address) decodedTranferFromFunc.get(1)).getValue();
      amount = ((BigInteger) decodedTranferFromFunc.get(2));
    }
    else
    {
      receiver = ((Address) decodedTranferFromFunc.get(0)).getValue();
      amount = ((BigInteger) decodedTranferFromFunc.get(1));

      //only integers are used internally to by the brige
      amount = amount.divide(new BigInteger(String.valueOf((long) Math.pow(10, assetDecimals))));
    }


    return new Transaction(res.getHash(), Collections.singletonList(
        new TransactionModels.TransactionReceiver(
            new TransactionModels.MappedAddress(receiver, assetName), amount.intValue())),
        Collections.singletonList(new TransactionModels.TransactionSender(
            new TransactionModels.MappedAddress(res.getFrom(), assetName))));


    /* return Optional.empty();*/
  }



  private ERC20ABI getERC20ABI(String assetId, String assetWalletPrivateKey)
  {
    Credentials cred = Credentials.create(assetWalletPrivateKey);
    return ERC20ABI.load(assetId, this.web3j, cred, BigInteger.valueOf(10000),
        BigInteger.valueOf(50000));
  }

  private Function getTransferFunction()
  {
    return new Function(ERC20ABI.FUNC_TRANSFER, Collections.<Type>emptyList(),
        Arrays.asList(new TypeReference<Address>()
        {
        }, new TypeReference<Uint256>()
        {
        }));
  }

  private Function getTransferFromFunction()
  {
    return new Function(ERC20ABI.FUNC_TRANSFERFROM, Collections.<Type>emptyList(),
        Arrays.asList(new TypeReference<Address>()
        {
        }, new TypeReference<Address>()
        {
        }, new TypeReference<Uint256>()
        {
        }));
  }
}
