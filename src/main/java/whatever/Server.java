package whatever;

import java.util.Properties;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServer;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.AuthProvider;
import io.vertx.ext.dropwizard.MetricsService;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.AuthHandler;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.CookieHandler;
import io.vertx.ext.web.handler.FormLoginHandler;
import io.vertx.ext.web.handler.RedirectAuthHandler;
import io.vertx.ext.web.handler.SessionHandler;
import io.vertx.ext.web.handler.UserSessionHandler;
import io.vertx.ext.web.handler.sockjs.BridgeOptions;
import io.vertx.ext.web.handler.sockjs.PermittedOptions;
import io.vertx.ext.web.handler.sockjs.SockJSHandler;
import io.vertx.ext.web.sstore.LocalSessionStore;
import io.vertx.ext.web.sstore.SessionStore;

public class Server extends AbstractVerticle {

  /*
   * Open the URL http://localhost:8888/view
   */

  @Override
  public void start() {
    MetricsService metricsService = MetricsService.create(vertx);

    Properties properties = System.getProperties();
    properties.setProperty("vertx.disableFileCaching", "true");

    Router router = Router.router(vertx);

    // Cookie Handler
    CookieHandler cookieHandler = CookieHandler.create();
    router.route().handler(cookieHandler);

    // Session Handler
    SessionStore store = LocalSessionStore.create(vertx);
    SessionHandler sessionHandler = SessionHandler.create(store);
    router.route().handler(sessionHandler);

    // SockJS setup
    SockJSHandler sockJS = SockJSHandler.create(vertx);
    router.route("/eventbus/*").handler(sockJS);
    router.route("/js/vertxbus.js").handler(routingContext -> {
      routingContext.response().sendFile("webroot/js/vertxbus.js");
    });
    router.route("/view").handler(routingContext -> {
      routingContext.response().sendFile("webroot/test.html");
    });

    // Inbount Permitted
    BridgeOptions bridgeOptions = new BridgeOptions();
    PermittedOptions inboundPermitted = new PermittedOptions();
    inboundPermitted.setAddress("inbound.address");
    bridgeOptions.addInboundPermitted(inboundPermitted);
    sockJS.bridge(bridgeOptions);


    HttpServer server = vertx.createHttpServer();
    server.requestHandler(router::accept).listen(8888, ar -> {
      if (ar.succeeded()) {
        System.out.println("Server is up and running. Now open the URL");
        System.out.println("http://localhost:8888/view");
      }
    });

    // Consumer
    vertx.eventBus().consumer("inbound.address", msg -> {
      System.out.println("The server has received the message " + msg.body() + " from the client.");
      msg.reply("OK, just reply something");

      vertx.setTimer(1000l, l -> {
        printOpenWebsockets(metricsService);
      });
    });
  }

  private void printOpenWebsockets(MetricsService metricsService) {
    JsonObject snapshot = metricsService.getMetricsSnapshot(vertx);
    String key = "vertx.http.servers.0.0.0.0:8888.open-websockets";
    JsonObject openWebsockets = snapshot.getJsonObject(key);
    System.out.println("\""+key+"\": " + openWebsockets);
  }

}
