package org.usergrid.byotransport.mod.dynamic;

import groovy.lang.Closure;
import groovy.lang.GroovyShell;
import org.apache.cassandra.db.*;
import org.apache.cassandra.db.filter.QueryPath;
import org.apache.cassandra.service.StorageProxy;
import org.apache.cassandra.utils.ByteBufferUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vertx.java.busmods.BusModBase;
import org.vertx.java.core.Handler;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonElement;
import org.vertx.java.core.json.JsonObject;

import java.nio.ByteBuffer;
import java.util.*;


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
    JsonObject json = jsonMessage.body();
    logger.info("handling message: {}", json);
    String ks = json.getString("keyspace");
    String cf = json.getString("cf");
    String key = json.getString("key");
    String script = json.getString("filtersrc");
    Closure closure = parseGroovy(script);
    List<ReadCommand> commands = new ArrayList<ReadCommand>(1);
    // results = storageProxy.slice
    // bb rowKey
    // bb cf
    QueryPath queryPath = new QueryPath(cf);
    SliceFromReadCommand slice = new SliceFromReadCommand(ks,
            ByteBuffer.wrap(key.getBytes()),
            queryPath,
            ByteBufferUtil.EMPTY_BYTE_BUFFER,
            ByteBufferUtil.EMPTY_BYTE_BUFFER,
            false,
            100);
    commands.add(slice);
    JsonObject result = new JsonObject();
    List<Row> rows;
    try {

      rows = StorageProxy.read(commands, ConsistencyLevel.ONE);
      ColumnFamily colFam = rows.get(0).cf;
      logger.info("Found rows: {}", rows.size());
      JsonArray ja = new JsonArray();
      for ( IColumn c : colFam) {
        JsonObject jsonObject = new JsonObject()
                .putString("name", ByteBufferUtil.string(c.name().duplicate()))
                .putString("value", ByteBufferUtil.string(c.value().duplicate()));
        jsonObject = (JsonObject)closure.call(jsonObject);
        if ( jsonObject != null ) {
          ja.addObject(jsonObject);
        }
      }
      result.putArray("columns", ja);
      logger.info("Found cols: {}", result);

    } catch (Exception ex) {
      ex.printStackTrace();
      jsonMessage.reply(new JsonObject().putBoolean("handled",false).putString("error",ex.getMessage()));
      return;
    }
    jsonMessage.reply(new JsonObject().putValue("results", result));
  }

  private Closure parseGroovy(String script) {
    GroovyShell shell = new GroovyShell();
    Object result = shell.evaluate(script);
    Closure closure;
    if ( result instanceof Closure) {
      closure = (Closure)result;
      // (Map) closure.call(row);
    } else {
      throw new RuntimeException("Groovy script must return a Closure");
    }
    return closure;
  }
}
