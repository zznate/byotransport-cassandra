package org.usergrid.byotransport.common;

import org.apache.cassandra.db.IColumn;
import org.apache.cassandra.db.Row;
import org.apache.cassandra.db.marshal.AbstractType;
import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;

import java.util.Iterator;
import java.util.List;

/**
 * Creates JsonObject representing each row on demand.
 * @author zznate
 */
public class CfReadResult implements Iterable<JsonObject>, Iterator<JsonObject> {

  private final Iterator<Row> rows;
  private final AbstractType keyType;
  private final AbstractType colType;
  private final AbstractType valType;
  private Row active;

  CfReadResult(List<Row> rows, AbstractType keyType,
               AbstractType colType, AbstractType valType) {
    this.rows = rows.iterator();
    this.keyType = keyType;
    this.colType = colType;
    this.valType = valType;
  }

  @Override
  public boolean hasNext() {
    return rows.hasNext();
  }

  /**
   * Converts the results in the current Row of the underlying Iterator
   * into a JsonObject on demand
   *
   * @return
   */
  @Override
  public JsonObject next() {
    active = rows.next();
    JsonObject result = new JsonObject();
    JsonArray ja = new JsonArray();
    for ( IColumn c : active.cf) {
      ja.addObject( new JsonObject()
              .putString(NAME_ATTRIB, colType.getString(c.name()))
              .putString(VAL_ATTRIB, valType.getString(c.value())));
    }
    result.putString(KEY_ATTRIB, keyType.getString(active.key.key));
    result.putArray(COLS_ATTRIB, ja);
    return result;
  }

  @Override
  public void remove() {
    rows.remove();
  }

  @Override
  public Iterator<JsonObject> iterator() {
    return this;
  }

  // statics for JSON creation
  private static final String NAME_ATTRIB = "name";
  private static final String VAL_ATTRIB = "value";
  private static final String COLS_ATTRIB = "columns";
  private static final String KEY_ATTRIB = "key";
}
