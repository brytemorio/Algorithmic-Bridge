package bridge.controller;

import static io.undertow.servlet.Servlets.defaultContainer;
import static io.undertow.servlet.Servlets.deployment;
import static io.undertow.servlet.Servlets.servlet;

import bridge.common.BridgeUtils;
import bridge.controller.RequestServlets.TestServerlet;
import io.undertow.Handlers;
import io.undertow.Undertow;
import io.undertow.server.HttpHandler;
import io.undertow.server.handlers.PathHandler;
import io.undertow.servlet.api.DeploymentInfo;
import io.undertow.servlet.api.DeploymentManager;
import io.undertow.servlet.api.ErrorPage;
import io.undertow.servlet.api.MimeMapping;
import io.undertow.util.StatusCodes;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import javax.servlet.ServletException;

public class UndertowController {
  public static final String MYAPP = "/api/v1";

  private UndertowController() {}

  public static void runServerController() throws IOException {
    URL errorPages = BridgeUtils.readResource("WEB-INF/errorpages/404.html");
    String errofile = Files.readString(Path.of(errorPages.getPath()));
    ErrorPage errorPage = new ErrorPage(errofile, StatusCodes.INTERNAL_SERVER_ERROR);
    MimeMapping mimeMapping = new MimeMapping(".html", "text/html");
    try {
      DeploymentInfo servletBuilder =
          deployment()
              .setClassLoader(UndertowController.class.getClassLoader())
              .setContextPath(MYAPP)
              .setDeploymentName("Agol-Bridge.war")
              .addErrorPage(errorPage)
              .addMimeMapping(mimeMapping)
              .setIgnoreFlush(true)
              .addServlets(servlet("TestServerlet", TestServerlet.class).addMapping("/*"));

      DeploymentManager manager = defaultContainer().addDeployment(servletBuilder);
      manager.deploy();

      // HttpServerExchange exchange;
      // exchange.getResponseHeaders().put(Headers.CONTENT_LENGTH, "" );
      HttpHandler servletHandler = manager.start();
      PathHandler path =
          Handlers.path(Handlers.redirect(MYAPP)).addPrefixPath(MYAPP, servletHandler);
      Undertow server =
          Undertow.builder().addHttpListener(5000, "127.0.0.2").setHandler(servletHandler).build();
      server.start();
    } catch (ServletException e) {
      throw new RuntimeException(e);
    }
  }
}
