This project demonstrates how to create your own transport for Apache Cassandra. In this demo, we use Vert.x and Groovy to apply a column level filter to our results. 

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
