package bridge.controller;

import bridge.common.BridgeUtils;
import bridge.common.ConfigFileObj;
import io.undertow.io.Sender;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.Headers;

public class BridgeRequestHandler implements HttpHandler {
  private ConfigFileObj pro;

  @Override
  public void handleRequest(HttpServerExchange exchange) throws Exception {
    if (exchange.getStatusCode() == 500) {
      String errorResponse = BridgeUtils.readResourceAsStream("404.hmtl").toString();
      exchange.getResponseHeaders().put(Headers.CONTENT_LENGTH, " " + errorResponse.length());
      Sender sender = exchange.getResponseSender();
      sender.send(errorResponse);
    }
  }
}
