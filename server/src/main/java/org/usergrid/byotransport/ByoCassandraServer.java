package org.usergrid.byotransport;

import org.apache.cassandra.config.CFMetaData;
import org.apache.cassandra.config.KSMetaData;
import org.apache.cassandra.db.ConsistencyLevel;
import org.apache.cassandra.db.IMutation;
import org.apache.cassandra.db.RowMutation;
import org.apache.cassandra.db.filter.QueryPath;
import org.apache.cassandra.db.marshal.UTF8Type;
import org.apache.cassandra.exceptions.AlreadyExistsException;
import org.apache.cassandra.exceptions.ConfigurationException;
import org.apache.cassandra.service.CassandraDaemon;
import org.apache.cassandra.service.MigrationManager;
import org.apache.cassandra.service.StorageProxy;
import org.apache.cassandra.thrift.CfDef;
import org.apache.cassandra.thrift.KsDef;
import org.apache.cassandra.utils.ByteBufferUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.Handler;
import org.vertx.java.core.Vertx;
import org.vertx.java.core.buffer.Buffer;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.http.HttpServerRequest;
import org.vertx.java.core.http.RouteMatcher;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.platform.PlatformLocator;
import org.vertx.java.platform.PlatformManager;

import java.util.*;
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
    // Move this all into VertxCassandraLauncher
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
    rm.get("/:keyspace/:cf/:key/filter/:lang/:filtersrc",
            new FilterRequestHandler(vertx, "cassandra.handler.dynamic"));
    rm.post("/:keyspace/:cf/:key", new Handler<HttpServerRequest>() {
      @Override
      public void handle(HttpServerRequest request) {
        request.bodyHandler(new Handler<Buffer>() {
          @Override
          public void handle(Buffer buffer) {
            // send the buffer directly over the wire
          }
        });
        // pull the JSON from the body
        //  req = mapper.readValue(buffer.getBytes(), IntraReq.class);
      }
    });
    vertx.createHttpServer().requestHandler(rm).listen(8080);

    running.set(true);
    logger.info("ByoCassandraServer has started");

    setupDemo();
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

  private void setupDemo() {
    Collection<CFMetaData> cfDefs = new ArrayList<CFMetaData>(0);
    KsDef def = new KsDef();
    def.setName("myks");
    def.setStrategy_class("SimpleStrategy");
    Map<String, String> strat = new HashMap<String, String>();
    strat.put("replication_factor", "1");
    def.setStrategy_options(strat);
    CfDef cfDef = new CfDef();
    cfDef.setName("mycf");
    cfDef.setKeyspace("myks");
    cfDef.setComparator_type("UTF8Type");
    cfDef.setDefault_validation_class("UTF8Type");
    cfDef.unsetId();
    KSMetaData ksm = null;
    try {
      cfDefs.add(CFMetaData.fromThrift(cfDef));
        ksm = KSMetaData.fromThrift(def,
            cfDefs.toArray(new CFMetaData[1]));
      MigrationManager.announceNewKeyspace(ksm);
    } catch(AlreadyExistsException aee) {
      // do nothing
      logger.info("keyspace already existed, continuing");
    } catch (Exception e) {
        throw new RuntimeException(e);
    }

    RowMutation rowMutation = new RowMutation("myks", ByteBufferUtil.bytes("zznate"));
    rowMutation.add(new QueryPath("mycf", null, ByteBufferUtil.bytes("amorton")),
            ByteBufferUtil.bytes("vale"), System.currentTimeMillis()*1000);
    rowMutation.add(new QueryPath("mycf", null, ByteBufferUtil.bytes("tnine")),
                ByteBufferUtil.bytes("vale"), System.currentTimeMillis()*1000);
    rowMutation.add(new QueryPath("mycf", null, ByteBufferUtil.bytes("edanuff")),
                    ByteBufferUtil.bytes("vale"), System.currentTimeMillis()*1000);
    rowMutation.add(new QueryPath("mycf", null, ByteBufferUtil.bytes("scottganyo")),
                    ByteBufferUtil.bytes("vale"), System.currentTimeMillis()*1000);
    rowMutation.add(new QueryPath("mycf", null, ByteBufferUtil.bytes("rockerston")),
                    ByteBufferUtil.bytes("vale"), System.currentTimeMillis()*1000);
    rowMutation.add(new QueryPath("mycf", null, ByteBufferUtil.bytes("thobbs")),
                    ByteBufferUtil.bytes("vale"), System.currentTimeMillis()*1000);
    List<IMutation> mutations = new ArrayList<IMutation>();
        mutations.add(rowMutation);
    try {
      StorageProxy.mutate(mutations, ConsistencyLevel.ONE);
    } catch (Exception ex) {
      ex.printStackTrace();
    }

  }
}
