package bridge.blockchains.ethchains;

import bridge.blockchains.Asset;
import bridge.common.BridgeUtils;
import bridge.common.ConfigFileObj;
import bridge.exceptions.BridgeExceptions;
import bridge.services.storagservice.ConfigurationStorageService;
import bridge.services.storagservice.DataObjects;
import com.electronwill.nightconfig.core.Config;
import com.electronwill.nightconfig.core.UnmodifiableConfig;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.math.BigInteger;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

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

    ConfigurationStorageService configurationStorageService =
        new ConfigurationStorageService();
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

  //TODO: Hotfix
  private static BigInteger getLatestBlockHeight(String url) {
    try {
      HttpURLConnection con = getHttpURLConnection(url);

      // Get the response from the server
      try (BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()))) {
        String inputLine;
        StringBuilder response = new StringBuilder();

        while ((inputLine = in.readLine()) != null) {
          response.append(inputLine);
        }

        con.disconnect();

        // Parse the JSON response and extract the block height as a BigInteger
        // Modify this part based on the actual JSON response structure
        // Example: {"jsonrpc": "2.0", "result": "12345", "id": 1}
        String jsonResponse = response.toString();
        log.info(jsonResponse);
        /*String blockHeightString = jsonResponse.split("\"result\": \"")[1].split("\",")[0];
        BigInteger blockHeight = new BigInteger(blockHeightString);*/

        if (jsonResponse.contains("\"result\":")) {
          int startIndex = jsonResponse.indexOf("\"result\":") + 9;
          int endIndex = jsonResponse.indexOf(",", startIndex);
          String blockHeightString = jsonResponse.substring(startIndex, endIndex).trim();
          BigInteger blockHeight = new BigInteger(blockHeightString);

          return blockHeight;
        }

        //return blockHeight;
      }

    } catch (Exception e) {
      e.printStackTrace();
    }

    return BigInteger.ZERO; // Return a default value if an error occurs
  }

  @NotNull
  private static HttpURLConnection getHttpURLConnection(String url) throws IOException
  {
    String requestData = "{\"jsonrpc\": \"2.0\",\"method\": \"platform.getHeight\",\"params\": {},\"id\": 1}";

    URL obj = new URL(url);
    HttpURLConnection con = (HttpURLConnection) obj.openConnection();

    // Set the HTTP request method to POST
    con.setRequestMethod("POST");

    // Set the content type to indicate JSON data
    con.setRequestProperty("Content-Type", "application/json");

    // Enable input and output streams
    con.setDoOutput(true);

    // Write the JSON data to the output stream
    try (OutputStream os = con.getOutputStream()) {
      byte[] input = requestData.getBytes("utf-8");
      os.write(input, 0, input.length);
    }
    return con;
  }

}
