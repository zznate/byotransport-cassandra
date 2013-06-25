package org.usergrid.byotransport.common;

import org.apache.cassandra.db.ConsistencyLevel;
import org.apache.cassandra.db.marshal.AbstractType;
import org.apache.cassandra.db.marshal.BytesType;

/**
 * @author zznate
 */
public class ReadBuilder<K> {

  private K key;
  private String keyspace;
  private String columnFamily;
  private AbstractType keyType = BytesType.instance;
  private ConsistencyLevel consistencyLevel = ConsistencyLevel.ONE;
  private int sliceSize = 100;
  private AbstractType columnType = BytesType.instance;
  private AbstractType valueType = BytesType.instance;

  ReadBuilder(String keyspace, String columnFamily) {
    this.keyspace = keyspace;
    this.columnFamily = columnFamily;
  }

  /**
   * Both keyspace and column family parameters are required
   * @param keyspace
   * @param columnFamily
   * @return
   */
  public static ReadBuilder<Object> newReadBuilder(String keyspace, String columnFamily) {
    return new ReadBuilder(keyspace, columnFamily);
  }

  public ReadBuilder<K> key(K key) {
    this.key = key;
    return this;
  }

  public ReadBuilder<K> keyType(AbstractType type) {
    this.keyType = type;
    return this;
  }

  public ReadBuilder<K> consistencyLevel(ConsistencyLevel cl) {
    this.consistencyLevel = cl;
    return this;
  }

  public ReadBuilder<K> sliceSize(int sliceSize) {
    this.sliceSize = sliceSize;
    return this;
  }

  public ReadBuilder<K> columnType(AbstractType colType) {
    this.columnType = colType;
    return this;
  }

  public ReadBuilder<K> valueType(AbstractType valType) {
    this.valueType = valType;
    return this;
  }

  public CfReader build() {
    return new CfReader<K>(this);
  }

  String getKeyspace() {
    return this.keyspace;
  }

  String getColumnFamily() {
    return this.columnFamily;
  }

  K getKey() {
    return key;
  }

  AbstractType getKeyType() {
    return keyType;
  }

  ConsistencyLevel getConsistencyLevel() {
    return this.consistencyLevel;
  }

  int getSliceSize() {
    return sliceSize;
  }

  AbstractType getColumnType() {
    return columnType;
  }

  AbstractType getValueType() {
    return valueType;
  }

}
