package bridge.common;

import static bridge.exceptions.Chain.ChainNodeException;
import static java.net.http.HttpResponse.*;

import com.electronwill.nightconfig.core.Config;
import com.electronwill.nightconfig.core.ConfigFormat;
import com.electronwill.nightconfig.core.io.ConfigParser;
import com.electronwill.nightconfig.json.JsonFormat;
import java.io.*;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

public interface BaseChainInterface {
  void init();

  default <T> List<T> getTrxOfBlockAtHeight(int height) {
    return Collections.emptyList();
  }

  default String getTrxByID(String trxID) {
    return null;
  }

  default <T> T getTrxHash(int blockHeight) {
    return null;
  }

  default boolean validateAddress(String address) {
    return false;
  }

  default Config getNodeResponse(String nodeEndpoint)
      throws ChainNodeException, IOException, URISyntaxException, InterruptedException {
    URL node = new URL(nodeEndpoint);

    String protocol = node.getProtocol();
    if ("wss".equals(protocol) || "ws".equals(protocol)) {
      // TODO: implement websocket handling logic here
    } else {
      HttpRequest request =
          HttpRequest.newBuilder(node.toURI()).header("Accept", "application/json").build();
      HttpClient client = HttpClient.newBuilder().build();
      HttpResponse<Supplier<Config>> response =
          client.send(request, new JsonRepsonseHandler(Config.class));
      if (200 != response.statusCode())
        throw new ChainNodeException(
            response.uri().toString(), response.statusCode(), response.body().get().toString());
      return response.body().get();
    }
    return null;
  }

  class JsonRepsonseHandler implements BodyHandler<Supplier<Config>> {
    private Class<Config> responseType;

    public JsonRepsonseHandler(Class<Config> responseType) {
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
          // InputStreamReader inputStreamReader= new InputStreamReader(inputStream1);
          // BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
          // bufferedReader.lines().forEach(System.out::println);
          return jsonParser.parse(inputStream1);

        } catch (IOException e) {
          throw new UncheckedIOException(e);
        }
      };
    }
  }
}
