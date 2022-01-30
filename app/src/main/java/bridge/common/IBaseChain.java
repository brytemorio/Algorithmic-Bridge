package bridge.common;

import bridge.exceptions.Chain.ChainNodeException;
import com.electronwill.nightconfig.core.Config;
import com.electronwill.nightconfig.core.ConfigFormat;
import com.electronwill.nightconfig.core.io.ConfigParser;
import com.electronwill.nightconfig.core.io.ParsingException;
import com.electronwill.nightconfig.json.JsonFormat;
import com.google.common.base.Supplier;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandler;
import java.net.http.HttpResponse.BodyHandlers;
import java.net.http.HttpResponse.BodySubscriber;
import java.net.http.HttpResponse.BodySubscribers;
import java.net.http.WebSocket;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import org.agrona.collections.Object2ObjectHashMap;

public interface IBaseChain {
  void init();

  Number getBlockHeight();

  Object2ObjectHashMap<String, Object> getChain2IdentifierMapping();

  <T extends Object> T getChainIdentifier();

  default <T extends Object> T getTrxOfBlockAtHeight(int height) {
    return null;
  }

  default String getTrxByID(String trxID) {
    return null;
  }

  default <T extends Object> List<T> getTrxHash(int blockHeight) {
    return Collections.emptyList();
  }

  default boolean validateAddress(String address) {
    return false;
  }

  default String getNodeResponse(String nodeEndpoint)
      throws ChainNodeException, IOException, URISyntaxException, InterruptedException,
          ExecutionException {
    URL node = new URL(nodeEndpoint);
    String protocol = node.getProtocol();
    HttpRequest request =
        HttpRequest.newBuilder(node.toURI()).header("Accept", "application/json").build();
    HttpResponse<String> response;

    if ("wss".equals(protocol) || "ws".equals(protocol)) {
      HttpClient webSocket = HttpClient.newHttpClient();
      WebSocket.Listener listener = new WebSocket.Listener() {};
      WebSocket.Builder wsBuilder = webSocket.newWebSocketBuilder();
      CompletableFuture<WebSocket> wsClient = wsBuilder.buildAsync(node.toURI(), listener);
      return wsClient.get().toString();

    } else {
      HttpClient client = HttpClient.newBuilder().build();
      response = client.send(request, BodyHandlers.ofString());
    }
    if (200 != response.statusCode())
      throw new ChainNodeException(
          response.uri().toString(), response.statusCode(), response.body().toString());
    return response.body();
  }

  /**/
  class JsonresponseHandler implements BodyHandler<Supplier<Config>> {
    private Class<Config> responseType;

    public JsonresponseHandler(Class<Config> responseType) {
      this.responseType = responseType;
    }

    @Override
    public BodySubscriber<Supplier<Config>> apply(HttpResponse.ResponseInfo responseInfo) {
      return asConfig(responseType);
    }

    private BodySubscriber<Supplier<Config>> asConfig(Class<Config> targetType) {
      BodySubscriber<InputStream> upStream = BodySubscribers.ofInputStream();

      return BodySubscribers.mapping(
          upStream, inputStream -> supplierOfTypeConfig(inputStream, targetType));
    }

    private Supplier<Config> supplierOfTypeConfig(
        InputStream inputStream, Class<Config> targetType) {
      return () -> {
        try (InputStream inputStream1 = inputStream) {
          Charset charset = StandardCharsets.ISO_8859_1;
          ConfigFormat<Config> jsonFormat = JsonFormat.fancyInstance();
          ConfigParser<Config> jsonParser = jsonFormat.createParser();
          return jsonParser.parse(inputStream1);

        } catch (IOException e) {
          throw new UncheckedIOException(e);
        } catch (ParsingException e) {
          throw new RuntimeException(e);
        }
      };
    }
  }

  class JsonWebSocketListener implements WebSocket.Listener {}
}
