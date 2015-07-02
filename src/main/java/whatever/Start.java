package whatever;

import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.ext.dropwizard.DropwizardMetricsOptions;
import io.vertx.ext.dropwizard.Match;
import io.vertx.ext.dropwizard.MatchType;



public class Start {

  public static void main(String[] args) {
    // Enable Metrics
    VertxOptions options = new VertxOptions();
    DropwizardMetricsOptions metricsOptions = new DropwizardMetricsOptions();
    metricsOptions.setEnabled(true);
    metricsOptions.addMonitoredHttpServerUri(new Match().setValue("/*").setType(MatchType.REGEX));
    options.setMetricsOptions(metricsOptions);
    
    Vertx vertx = Vertx.vertx(options);
    vertx.deployVerticle(Server.class.getName());
  }
}
