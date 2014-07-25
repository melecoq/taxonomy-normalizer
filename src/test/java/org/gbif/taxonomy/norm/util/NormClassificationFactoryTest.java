package org.gbif.taxonomy.norm.util;

import org.gbif.taxonomy.norm.NormClassification;

import java.io.IOException;
import java.util.List;

import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

public class NormClassificationFactoryTest {

  @Test
  public void testBuild() {
    String source = "/data/taxonomy/utils/norm.txt";
    try {
      List<NormClassification> data = NormClassificationFactory.build(source);
      assertEquals(7, data.size());
      assertEquals(Integer.valueOf(1), data.get(0).getId());
      assertNull(data.get(0).getParentId());
      assertEquals("Animalia", data.get(0).getScientificName());
      assertNull(data.get(0).getAuthor());
      assertEquals("kingdom", data.get(0).getRank());

      assertEquals(Integer.valueOf(2), data.get(1).getId());
      assertEquals(Integer.valueOf(1), data.get(1).getParentId());
      assertEquals("Chordata", data.get(1).getScientificName());
      assertNull(data.get(1).getAuthor());
      assertEquals("phylum", data.get(1).getRank());

      assertEquals(Integer.valueOf(7), data.get(6).getId());
      assertEquals(Integer.valueOf(6), data.get(6).getParentId());
      assertEquals("Puma concolor", data.get(6).getScientificName());
      assertEquals("Linneaus, 1771", data.get(6).getAuthor());
      assertEquals("species", data.get(6).getRank());


    } catch (IOException e) {
      fail("NormClassificationFactory throwing unexpected exceptions: " + e.getMessage());
    }
  }
}
