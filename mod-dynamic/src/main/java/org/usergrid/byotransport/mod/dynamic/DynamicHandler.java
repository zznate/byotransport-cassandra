package org.usergrid.byotransport.mod.dynamic;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vertx.java.busmods.BusModBase;
import org.vertx.java.core.Handler;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.json.JsonObject;



/**
 * @author zznate
 */
public class DynamicHandler extends BusModBase implements Handler<Message<JsonObject>> {

  private final Logger logger = LoggerFactory.getLogger(DynamicHandler.class);

  @Override
  public void start() {
    logger.info("starting DynamicHandler module");
    super.start();
    vertx.eventBus().registerHandler("cassandra.handler.dynamic", this);
  }

  @Override
  public void stop() {
    logger.info("Stopping DynamicHandler module");
    super.stop();
  }

  @Override
  public void handle(Message<JsonObject> jsonMessage) {
    logger.info("handling message: {}", jsonMessage.body());
  }
}
