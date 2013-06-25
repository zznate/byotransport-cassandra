package org.usergrid.byotransport.common;

import org.apache.cassandra.db.*;
import org.apache.cassandra.db.marshal.UTF8Type;
import org.apache.cassandra.dht.StringToken;
import org.apache.cassandra.utils.ByteBufferUtil;
import org.junit.Test;
import org.mockito.Mockito;
import org.vertx.java.core.json.JsonObject;

import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

/**
 * @author zznate
 */
public class CfReadResultUnitTest {

  @Test
  public void verifySingleRow() {
    CfReadResult crr = new CfReadResult(Arrays.asList(mockRow("key1",10)),
            UTF8Type.instance,
            UTF8Type.instance,
            UTF8Type.instance);
    JsonObject json = crr.next();
    assertNotNull(json);
    assertEquals("key1", json.getString("key").toString());
    assertEquals(10, json.getArray("columns").size());
    assertFalse(crr.hasNext());
  }

  @Test
  public void verifyIterationControl() {

    Map<String,Row> rows = new HashMap<>(10);
    for ( int x=0; x<10; x++) {
      rows.put("key_"+x,mockRow("key_"+x,10));
    }
    CfReadResult crr = new CfReadResult(new ArrayList<>(rows.values()),
                UTF8Type.instance,
                UTF8Type.instance,
                UTF8Type.instance);
    int y = 0;
    for ( JsonObject json : crr ) {
      assertNotNull(rows.get(json.getString("key").toString()));
    }
  }

  /**
   * Make a real row from:
   * - real list of 10 generated Column
   * - Mocked ColumnFamily containing said columns (via mocked iterator())
   *
   * @return Row object containing the mockec CF, fake key and token
   */
  private Row mockRow(String key, int colCount) {
    List<IColumn> cols = new ArrayList<>();
    for (int i=0; i<colCount;i++) {
      cols.add(new Column(ByteBufferUtil.bytes("col"+i), ByteBufferUtil.bytes("val" + i)));
    }

    ColumnFamily cf = Mockito.mock(ColumnFamily.class);
    Mockito.when(cf.iterator()).thenReturn(cols.iterator());

    return new Row(new DecoratedKey(new StringToken("faketoken_for_" + key), ByteBufferUtil.bytes(key)), cf);
  }
}
