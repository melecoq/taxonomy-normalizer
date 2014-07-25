package org.gbif.taxonomy.norm;

import org.gbif.taxonomy.norm.NormClassification;

import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

// sanity test as this is so important
public class NormClassificationTest {

  @Test
  public void testEqualsObject() {
    NormClassification n1 = new NormClassification(null, 1, "a", null, "s");
    NormClassification n2 = new NormClassification(null, 1, "a", null, "s");
    NormClassification n3 = new NormClassification(0, 1, "a", null, "s");
    assertEquals(n1, n2);
    assertEquals(n2, n1);
    assertFalse(n1.equals(n3));
    assertFalse(n3.equals(n1));
    assertFalse(n2.equals(n3));
    assertFalse(n3.equals(n2));
  }
}
