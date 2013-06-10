package org.usergrid.byotransport;

import org.apache.cassandra.service.CassandraDaemon;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.Handler;
import org.vertx.java.core.Vertx;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.http.HttpServerRequest;
import org.vertx.java.core.http.RouteMatcher;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.platform.PlatformLocator;
import org.vertx.java.platform.PlatformManager;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author zznate
 */
public class ByoCassandraServer implements CassandraDaemon.Server {

  private static Logger logger = LoggerFactory.getLogger(ByoCassandraServer.class);

  private static final AtomicBoolean running = new AtomicBoolean(false);
  private static Vertx vertx;
  private static RouteMatcher rm;

  @Override
  public void start() {
    PlatformLocator pl = new PlatformLocator();
    PlatformManager manager = pl.factory.createPlatformManager();
    vertx = manager.vertx();
    manager.deployModule("org.usergrid.byotransport~byotransport-cassandra-mod-dynamic~1.0-SNAPSHOT",
            new JsonObject(), 1, new Handler<AsyncResult<String>>() {
      @Override
      public void handle(AsyncResult<String> stringAsyncResult) {
        logger.info("deployed dynamic module");
      }
    });
    rm = new RouteMatcher();
    rm.get("/:keyspace/:cf/:key/filter/:lang/:filtersrc", new FilterRequestHandler(vertx));
    vertx.createHttpServer().requestHandler(rm).listen(8080);


    running.set(true);
    logger.info("ByoCassandraServer has started");
  }

  @Override
  public void stop() {
    boolean stopped = running.compareAndSet(true, false);
    logger.info("ByoCassandraServer has stopped.");
  }

  @Override
  public boolean isRunning() {
    return running.get();
  }
}
