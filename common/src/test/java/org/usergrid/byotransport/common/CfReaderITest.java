package org.usergrid.byotransport.common;

import org.vertx.java.core.json.JsonObject;
import org.junit.Test;

/**
 * @author zznate
 */
public class CfReaderITest {


  @Test
  public void verifySingleRowReadOk() throws Exception {
    CfReader rr = ReadBuilder.newReadBuilder("myks","mycf")
            .key("key")
            .build();


  }


}
