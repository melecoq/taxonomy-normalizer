package org.gbif.taxonomy.norm.util;

import java.io.IOException;
import java.io.StringReader;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

public class CSVReaderTest {

  /**
   * Test the CSVReader used in tests works correctly
   */
  @Test
  public void testParseBasic() {
    String source = "kingdom	scientificName\n" + "Animalia	Puma concolor\n" + "Plantae	Abies alba\n" + "Plantae	";
    try {
      List<Map<String, String>> data = CSVReader.parse(new StringReader(source));
      assertEquals(3, data.size());
      assertEquals("Animalia", data.get(0).get("kingdom"));
      assertEquals("Puma concolor", data.get(0).get("scientificName"));
      assertEquals("Plantae", data.get(1).get("kingdom"));
      assertEquals("Abies alba", data.get(1).get("scientificName"));
      assertEquals("Plantae", data.get(2).get("kingdom"));
      assertNull(data.get(2).get("scientificName"));
    } catch (IOException e) {
      fail("CSVReader throwing unexpected exceptions: " + e.getMessage());
    }

  }

}
