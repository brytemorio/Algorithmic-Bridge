package bridge.controller;

import static bridge.controller.RequestServlets.TestServerlet;
import static io.undertow.servlet.Servlets.*;

import bridge.common.ConfigFileObj;
import io.undertow.Handlers;
import io.undertow.Undertow;
import io.undertow.server.HttpHandler;
import io.undertow.server.handlers.PathHandler;
import io.undertow.servlet.api.DeploymentInfo;
import io.undertow.servlet.api.DeploymentManager;
import io.undertow.servlet.api.ErrorPage;
import java.net.URL;
import javax.servlet.ServletException;

public class UndertowController {
  public static final String MYAPP = "/api/v1";

  private UndertowController() {}

  public static void runServerController() {
    URL errorPages = ConfigFileObj.readResource("WEB-INF/errorpages/404.html");
    String errofilePath = errorPages.getPath();
    ErrorPage errorPage = new ErrorPage(errofilePath);
    try {
      DeploymentInfo servletBuilder =
          deployment()
              .setClassLoader(UndertowController.class.getClassLoader())
              .setContextPath(MYAPP)
              .setDeploymentName("Agol-Bridge.war")
              .addErrorPage(errorPage)
              .addServlets(servlet("TestServerlet", TestServerlet.class).addMapping("/"));

      DeploymentManager manager = defaultContainer().addDeployment(servletBuilder);
      manager.deploy();

      HttpHandler servletHandler = manager.start();
      PathHandler path =
          Handlers.path(Handlers.redirect(MYAPP)).addPrefixPath(MYAPP, servletHandler);
      Undertow server =
          Undertow.builder().addHttpListener(8080, "localhost").setHandler(servletHandler).build();
      server.start();
    } catch (ServletException e) {
      throw new RuntimeException(e);
    }
  }
}
