package org.usergrid.byotransport;

import org.vertx.java.core.Handler;
import org.vertx.java.core.Vertx;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.http.HttpServerRequest;
import org.vertx.java.core.json.JsonObject;

/**
 * Filter out the parameters from the URL, constructing the JSON
 * which will be our payload
 *
 * @author zznate
 */
public class FilterRequestHandler implements Handler<HttpServerRequest> {

  private final Vertx vertx;
  private final String busEndpoint;

  FilterRequestHandler(Vertx vertx, String busEndpoint) {
    this.vertx = vertx;
    this.busEndpoint = busEndpoint;
  }

  @Override
  public void handle(final HttpServerRequest request) {
    // TODO load a request extractor based on busEndpoint enum
    // Message<JsonObject> message = Type.valueOf(endpoint).send();
    JsonObject json = formatParams(request);
    vertx.eventBus().send(busEndpoint,json,
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
            .putString("key", request.params().get("key"))
            .putString("filtersrc", request.params().get("filtersrc"));


    return jsonObject;
  }
}
