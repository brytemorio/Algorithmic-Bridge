package bridge.blockchains.ethchains;

import bridge.blockchains.Asset;
import bridge.blockchains.ethchains.abiwrapper.ERC20ABI;
import bridge.common.BridgeUtils;
import bridge.common.ConfigFileObj;
import bridge.exceptions.BridgeExceptions;
import bridge.services.storagservice.ConfigurationStorageService;
import bridge.services.storagservice.DataObjects;
import bridge.services.transactionservice.TransactionModels;
import com.electronwill.nightconfig.core.Config;
import com.electronwill.nightconfig.core.UnmodifiableConfig;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.web3j.abi.FunctionEncoder;
import org.web3j.abi.datatypes.Address;
import org.web3j.abi.datatypes.Function;
import org.web3j.abi.datatypes.generated.Uint256;
import org.web3j.abi.datatypes.generated.Uint8;
import org.web3j.crypto.*;
import org.web3j.protocol.core.DefaultBlockParameter;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.request.Transaction;
import org.web3j.protocol.core.methods.response.EthCall;
import org.web3j.tx.gas.DefaultGasProvider;

import java.io.*;
import java.math.BigInteger;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

@Slf4j
public final class AvaxChain<K> extends EthIBaseChain<K>
{
  private static final UnmodifiableConfig configObject = ConfigFileObj.CONFIG;

  public AvaxChain(String... assetNamesFromConfig) throws BridgeExceptions.AssetNotFoundException,
      RuntimeException
  {
    BridgeUtils.checkArgsLength(assetNamesFromConfig,
        "At least one token name should be passed to " + getClass().getSimpleName() + " " + "constructor");
    String[] assetNames = assetNamesFromConfig;
    Config assets = configObject.get("Blockchain.Avax.assets");
    ArrayList<Asset> assetList = new ArrayList<>();
    for (String assetNameI : assetNames)
    {
      if (!assets.contains(assetNameI))
      {
        throw new BridgeExceptions.AssetNotFoundException(
            String.format("Asset: %s, could not be found in the config file", assetNameI));
      }
      assetList.add(new Asset(configObject.get("Blockchain.Avax.assets" + "." + assetNameI)));
    }

    var avaxConfig = new DataObjects.ConfigurationStorage();
    avaxConfig.setChainName("avax");
    avaxConfig.setNode(configObject.get("Blockchain.Avax.node"));
    avaxConfig.setNetwork(configObject.get("Blockchain.Avax.network"));
    avaxConfig.setNetworkId(configObject.get("Blockchain.Avax.network_id"));
    avaxConfig.setChainIdentifier(configObject.get("Blockchain.Avax.chain_identifier"));
    avaxConfig.setGatewayAddress(configObject.get("Blockchain.Avax.gateway_address"));
    avaxConfig.setAssets(assetList);

    ConfigurationStorageService configurationStorageService = new ConfigurationStorageService();
    configurationStorageService.saveConfiguration(avaxConfig);
    super.setEthChainConfig(configurationStorageService.getConfiguration("avax"));
    super.init();
  }

  @Override
  public BigInteger getBlockHeight() throws IOException, InterruptedException
  {
    var blockHeight = getWeb3j().ethBlockNumber().send().getBlockNumber();
    Thread.sleep(10000);
    return blockHeight;
  }


  //TODO: Temporary Hotfix
  @SneakyThrows
  @Override
  public TransactionModels.Transaction sendCoin(TransactionModels.TransactionAttempt attempt)
  {
    var web3j = getWeb3j();
    String private_key = "754fe43ff651720347ac36cca05fb9423bdb51c597ad986c6b4c0086ec330758";
    String public_key = "0xaDF0888A5D150938F652c6731a7D8CA5d8ead379";
    String assetId = "0xF77D71Ca22A999F91F3344e12e05591a2B943468";
    String password = "@CCr21t3v2p@$$@$$";
    ClassLoader resourceDir = AvaxChain.class.getClassLoader();
    String walletName = "avax_walle.json";
    InputStream walletFileName = resourceDir.getResourceAsStream(walletName);
    String newWalletFileName = "";

    if (walletFileName == null)
    {
      var credentials = Credentials.create(private_key);
      //WalletFile newWalletFile = Wallet.create(password, credentials.getEcKeyPair(), 16384, 1);
      File newfile = new File(resourceDir.getResource("").getPath());
      newWalletFileName =  WalletUtils.generateWalletFile(password, credentials.getEcKeyPair(),
        newfile, false);

      //todo: remove logging
      log.info(newWalletFileName);
    }

    var walletCrendentials = WalletUtils.loadCredentials(password,
        resourceDir.getResource(newWalletFileName).getPath());
    ERC20ABI contract = ERC20ABI.load(assetId, web3j, walletCrendentials, new DefaultGasProvider());


    String toAddress = attempt.getReceivers().getFirst().getAddress();
    int amount = attempt.getReceivers().getFirst().getAmount();
    BigInteger amountBigInt = new BigInteger(
        String.valueOf(attempt.getReceivers().getFirst().getAmount()));
    Function mintFunction = new Function("mint", Arrays.asList(new Uint256(amountBigInt),
        new Address(toAddress)), Collections.emptyList());
    String encodedFunction = FunctionEncoder.encode(mintFunction);
    EthCall response = web3j.ethCall(Transaction.createEthCallTransaction(public_key, assetId,
        encodedFunction), DefaultBlockParameterName.LATEST).send();
    log.info("Ethcall Response" + response.getRawResponse());
    //var trxReceipt = contract.mint(toAddress, amountBigInt).sendAsync().get();
    /*return new TransactionModels.Transaction(tres.getTransactionHash(),
        Collections.singletonList(new TransactionModels.TransactionReceiver(
            new TransactionModels.MappedAddress(toAddress, "XFT"), amount)));*/
    return null;
  }


}
