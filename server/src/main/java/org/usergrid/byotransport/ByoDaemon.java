package org.usergrid.byotransport;

import org.apache.cassandra.service.CassandraDaemon;

import java.io.IOException;

/**
 * @author zznate
 */
public class ByoDaemon extends CassandraDaemon {

  private static final ByoDaemon instance = new ByoDaemon();

  public Server byoServer;

  public static void main(String[] args) {
    // keep us in the forground for now
 		System.setProperty("cassandra-foreground", "true");

    // overrid the log4j props so we only need one file
 		System.setProperty("log4j.defaultInitOverride", "true");
 		System.setProperty("log4j.configuration", "log4j.properties");

    // what to do with the other transports?
    System.setProperty("cassandra.start_rpc","true");
    System.setProperty("cassandra.start_native_transport","true");

     instance.activate();
 	}

  @Override
  protected void setup() {
    super.setup();
    byoServer = new ByoCassandraServer();
  }

  @Override
  public void init(String[] arguments) throws IOException {
    super.init(arguments);
  }

  @Override
  public void start() {
    super.start();
    byoServer.start();
  }

  @Override
  public void stop() {
    super.stop();
    byoServer.stop();
  }
}
