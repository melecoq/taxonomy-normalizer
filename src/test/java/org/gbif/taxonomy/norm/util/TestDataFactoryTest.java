package org.gbif.taxonomy.norm.util;

import org.gbif.taxonomy.norm.DenormClassification;

import java.io.IOException;
import java.util.List;

import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

public class TestDataFactoryTest {

  @Test
  public void testBuild() {
    String source = "/data/taxonomy/utils/bootstrap.txt";
    try {
      List<DenormClassification> data = TestDataFactory.build(source, false);
      assertEquals(1, data.size());
      assertEquals("Animalia", data.get(0).getKingdom());
      assertEquals("Chordata", data.get(0).getPhylum());
      assertEquals("Mammalia", data.get(0).getKlass());
      assertEquals("Carnivora", data.get(0).getOrder());
      assertEquals("Felidae", data.get(0).getFamily());
      assertEquals("Puma", data.get(0).getGenus());
      assertEquals("Puma concolor", data.get(0).getSpecies());
      assertNull(data.get(0).getSubspecies());
      assertEquals("Linneaus, 1771", data.get(0).getAuthor());

    } catch (IOException e) {
      fail("TestDataFactory throwing unexpected exceptions: " + e.getMessage());
    }
  }

  @Test
  public void testBuildWithInference() {
    String source = "/data/taxonomy/utils/bootstrap2.txt";
    try {
      List<DenormClassification> data = TestDataFactory.build(source, true);
      assertEquals(1, data.size());
      assertEquals("Animalia", data.get(0).getKingdom());
      assertEquals("Chordata", data.get(0).getPhylum());
      assertEquals("Mammalia", data.get(0).getKlass());
      assertEquals("Carnivora", data.get(0).getOrder());
      assertEquals("Felidae", data.get(0).getFamily());
      assertEquals("Puma", data.get(0).getGenus());
      assertEquals("Puma concolor Linneaus, 1771", data.get(0).getSpecies());
      assertNull(data.get(0).getSubspecies());
      assertEquals("Linneaus, 1771", data.get(0).getAuthor());

    } catch (IOException e) {
      fail("TestDataFactory throwing unexpected exceptions: " + e.getMessage());
    }
  }
}
