package bridge.common;

import static bridge.exceptions.Chain.ChainNodeException;
import static java.net.http.HttpResponse.*;

import com.electronwill.nightconfig.core.Config;
import com.electronwill.nightconfig.core.ConfigFormat;
import com.electronwill.nightconfig.core.io.ConfigParser;
import com.electronwill.nightconfig.json.JsonFormat;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.*;
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

  default void establishNodeConnection(String networkNode)
      throws ChainNodeException, IOException, URISyntaxException, InterruptedException {
    URL node = new URL(networkNode);
    String protocol = node.getProtocol();
    if ("wss".equals(protocol) || "ws".equals(protocol)) {
      // TODO: implement websocket handling logic here
    } else {
      HttpRequest request =
          HttpRequest.newBuilder(node.toURI()).header("Accept", "application/json").build();
      HttpClient client = HttpClient.newBuilder().build();

      // TODO: Work on the call below, so that it properly uses "Config" Object
      var response = client.send(request, new JsonRepsonseHandler(Config.class)).body().get();
    }
  }

  static class JsonRepsonseHandler implements BodyHandler<Supplier<?>> {
    private final Class<?> responseType;

    public JsonRepsonseHandler(Class<?> responseType) {
      this.responseType = responseType;
    }

    @Override
    public BodySubscriber<Supplier<?>> apply(HttpResponse.ResponseInfo responseInfo) {
      return asConfig(responseType);
    }

    private BodySubscriber<Supplier<?>> asConfig(Class<?> targetType) {
      BodySubscriber<InputStream> upStream = BodySubscribers.ofInputStream();

      return BodySubscribers.mapping(upStream, this::supplierOfTypeConfig);
    }

    private Supplier<?> supplierOfTypeConfig(InputStream inputStream) {
      return () -> {
        try (InputStream inputStream1 = inputStream) {
          Charset CHARSET = StandardCharsets.ISO_8859_1;
          ConfigFormat<?> jsonFormat = JsonFormat.fancyInstance();
          ConfigParser<?> jsonParser = jsonFormat.createParser();
          return jsonParser.parse(inputStream1, CHARSET);
        } catch (IOException e) {
          throw new UncheckedIOException(e);
        }
      };
    }
  }
}
