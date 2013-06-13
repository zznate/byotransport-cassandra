# Overview
This project demonstrates how to create your own transport for Apache Cassandra. It was created in support of a lightning talk at the 2013 Cassandra Summit in San Francisco, CA. 


## Quick Start
In this demo, we use Vert.x and Groovy to apply a column level filter to our results. 

If you are super impatient, download the source, use maven to install:

    mvn install

Then use maven executor to start the daemon implementation thusly:

    mvn -e exec:java -Dexec.mainClass="org.usergrid.byotransport.ByoDaemon"

This is a demo, so we insert some test data by default under the keyspace 'myks' and the column family 'mycf'. This demo will apply the groovy filter against each column 
returned from cassandra. In our case, we are getting the columns under the row key 'zznate' and filtering for values greater than 21. The general form of this example is: 

    curl 'http://localhost:8080/myks/mycf/zznate/filter/groovy/[url encoded groovy script goes here]'

The raw groovy script we are applying is:

    { json -> if (json.getString('value').toInteger() > 21) return json else return null }

The complete curl command against the default localhost transport started above is:

    curl 'http://localhost:8080/myks/mycf/zznate/filter/groovy/%7B%20json%20-%3E%20if%20%28json.getString%28%27value%27%29.toInteger%28%29%20%3E%2021%29%20return%20json%20else%20return%20null%20%7D'

To quickly encode this (or your own script) use the following python:

    import urllib; urllib.quote("{ json -> if (json.getString('value').toInteger() > 21) return json else return null }")

## Details
There are two aspects to which we'll need to pay attention: configuraiton and implemementation. First we'll discuss configuration.

### Configuration
Useful properties to control in your Daemon implementation (and the defaults in this project):

Property Name | Description | Default
--------------|-------------|--------
cassandra-foreground | Keep process in foreground | true
log4j.defaultInitOverride | Override the log4j configuration | true
log4j.configuration | Name of the log4j properties file | log4j.properties
cassandra.start_rpc | Start the thrift server | false 
cassandra.start_native_transport | Start the CQL3 transport | false

These properties are best set in the main method of the Daemon implementation described below.

###Implementation
There are basically two classes you will be required to extend in order to build your own transport. Specifically:

- o.a.c.service.CassandraDaemon ("Daemon")
- o.a.c.service.CassandraDaemon.Server ("Server")

#### Daemon
You should override the following methods in the Daemon implementation:

- start
- stop
- setup

In each one of these methods, the very first thing we should do is invoke super to ensure the rest of the Cassandra machinery is started correctly. 

##### setup()
[ByoDaemon#setup](https://github.com/zznate/byotransport-cassandra/blob/master/server/src/main/java/org/usergrid/byotransport/ByoDaemon.java#L32)

This is where we instantiate the Server object. This is the place to acquire properties specific to your server and instantiate any other resources needed by such. 

##### start()
[ByoDaemon#start](https://github.com/zznate/byotransport-cassandra/blob/master/server/src/main/java/org/usergrid/byotransport/ByoDaemon.java#L43)

This is where you would actually start the transports your server is supporting. 

##### stop()
[ByoDaemon#stop](https://github.com/zznate/byotransport-cassandra/blob/master/server/src/main/java/org/usergrid/byotransport/ByoDaemon.java#L49)

Clean up any resources and shut down the transport(s) you started in in the start and setup methods. 

#### Server
The Server implementation really just has two life cycle methods: start and stop. They do precisely what they say on the tin so we'll not go in to detail on them. As you really define the Server - in effect the transport(s) itself - Server is just an implementation of an interface. 

In the case of this project, the start method is where we programatically initialize Vert.x and load the modules. See [ByoCassandraServer#start](https://github.com/zznate/byotransport-cassandra/blob/master/server/src/main/java/org/usergrid/byotransport/ByoCassandraServer.java#L46) for details.
