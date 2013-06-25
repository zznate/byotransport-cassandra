package org.usergrid.byotransport.common;

import org.apache.cassandra.db.*;
import org.apache.cassandra.db.filter.QueryPath;
import org.apache.cassandra.db.marshal.AbstractType;
import org.apache.cassandra.service.StorageProxy;
import org.apache.cassandra.utils.ByteBufferUtil;
import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

/**
 * @author zznate
 */
public class CfReader<K> {

  private final String keyspace;
  private final String columnFamily;
  private final K key;
  private final AbstractType keyType;
  private final AbstractType colType;
  private final AbstractType valType;
  private final ConsistencyLevel consistencyLevel;
  private final int sliceSize;


  CfReader(ReadBuilder<K> builder) {
    // required
    this.keyspace = builder.getKeyspace();
    this.columnFamily = builder.getColumnFamily();
    this.key = builder.getKey();
    // non-required, could be defaults
    this.keyType = builder.getKeyType();
    this.consistencyLevel = builder.getConsistencyLevel();
    this.sliceSize = builder.getSliceSize();
    this.colType = builder.getColumnType();
    this.valType = builder.getValueType();
  }

  public CfReadResult read() {
    List<ReadCommand> commands = new ArrayList<ReadCommand>(1);
    QueryPath queryPath = new QueryPath(columnFamily);
    SliceFromReadCommand slice = new SliceFromReadCommand(keyspace,
            keyType.decompose(key),
            queryPath,
            ByteBufferUtil.EMPTY_BYTE_BUFFER,
            ByteBufferUtil.EMPTY_BYTE_BUFFER,
            false,
            sliceSize);
    commands.add(slice);
    List<Row> rows;
    CfReadResult result = null;
    try {
      rows = StorageProxy.read(commands, consistencyLevel);
      result = new CfReadResult(rows, keyType, colType, valType);
    } catch (Exception ex){
      // TODO push this back up to caller, or put into CfReadResult?
      //results = new JsonObject()
      //        .putString("errorMessage", ex.getMessage());
      throw new RuntimeException(ex);
    }
    return result;
  }



  // TODO move "processSingleRow" into static utils class
  // TODO mock List<Row>
  // TODO add column names
  // TODO multiple keys impl
  // TODO implements iterable?


}
