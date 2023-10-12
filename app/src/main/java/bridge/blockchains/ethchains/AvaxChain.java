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
import org.web3j.crypto.Bip44WalletUtils;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.WalletUtils;
import org.web3j.tx.gas.DefaultGasProvider;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.math.BigInteger;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
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


  //TODO: Temporary fix
  @SneakyThrows
  @Override
  public TransactionModels.Transaction sendCoin(TransactionModels.TransactionAttempt attempt)
  {
    var web3j = getWeb3j();
    String private_key = "754fe43ff651720347ac36cca05fb9423bdb51c597ad986c6b4c0086ec330758";
    String public_key = "0xaDF0888A5D150938F652c6731a7D8CA5d8ead379";
    String assetId = "0xF77D71Ca22A999F91F3344e12e05591a2B943468";
    var credentials = Credentials.create(private_key, public_key);
    ERC20ABI contract = ERC20ABI.load(assetId, web3j, credentials, new DefaultGasProvider());
    String toAddress = attempt.getReceivers().getFirst().getAddress();
    int amount = attempt.getReceivers().getFirst().getAmount();
    BigInteger amountBigInt = new BigInteger(
        String.valueOf(attempt.getReceivers().getFirst().getAmount()));

    var trxReceipt = contract.mint(toAddress, amountBigInt).send();
    return new TransactionModels.Transaction(trxReceipt.getTransactionHash(),
        Collections.singletonList(new TransactionModels.TransactionReceiver(
            new TransactionModels.MappedAddress(toAddress, "XFT"), amount)));
  }


}
