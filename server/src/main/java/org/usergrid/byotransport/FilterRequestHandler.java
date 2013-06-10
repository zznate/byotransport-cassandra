package org.usergrid.byotransport;

import org.vertx.java.core.Handler;
import org.vertx.java.core.Vertx;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.http.HttpServerRequest;
import org.vertx.java.core.json.JsonObject;

/**
 * @author zznate
 */
public class FilterRequestHandler implements Handler<HttpServerRequest> {

  private final Vertx vertx;

  FilterRequestHandler(Vertx vertx) {
    this.vertx = vertx;
  }

  @Override
  public void handle(final HttpServerRequest request) {
    JsonObject json = formatParams(request);
    vertx.eventBus().send("cassandra.handler.dynamic",
            json,
            new Handler<Message<JsonObject>>() {
              @Override
              public void handle(Message<JsonObject> message) {
                request.response().end(message.body().toString());
              }
            });
  }

  private JsonObject formatParams(HttpServerRequest request) {
    JsonObject jsonObject = new JsonObject()
            .putString("keyspace", request.params().get("keyspace"))
            .putString("cf", request.params().get("cf"))
            .putString("lang", request.params().get("lang"))
            .putString("filtersrc", request.params().get("filtersrc"));

    return jsonObject;
  }
}
