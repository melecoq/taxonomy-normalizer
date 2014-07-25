package org.gbif.taxonomy.norm;

import org.gbif.taxonomy.norm.DenormClassification;
import org.gbif.taxonomy.norm.DenormClassificationUtils;
import org.gbif.taxonomy.norm.LinneanRank;
import org.gbif.taxonomy.norm.LinneanRank.LINNEAN_RANK;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class DenormClassificationUtilsTest {

  private static final Logger LOG = LoggerFactory.getLogger(DenormClassificationUtilsTest.class);

  @Test
  public void testShareHigherTaxonomy() {
    DenormClassification d1 = new DenormClassification("a", "b", "c", "d", "e", "f", "g", "h", "i");
    DenormClassification d2 = new DenormClassification("a", "b", "c", "d", "e", "f", "g", "h", "i");
    DenormClassification d3 = new DenormClassification(null, "b", "c", "d", "e", "f", "g", "h", "i");
    DenormClassification d4 = new DenormClassification("a", "b", "c", "d", "e", "F1", "G1", "H1", "i");
    assertTrue(DenormClassificationUtils.shareHigherTaxonomy(d1, d2, LINNEAN_RANK.S));
    assertTrue(DenormClassificationUtils.shareHigherTaxonomy(d2, d1, LINNEAN_RANK.S));
    assertFalse(DenormClassificationUtils.shareHigherTaxonomy(d1, d3, LINNEAN_RANK.S));
    assertTrue(DenormClassificationUtils.shareHigherTaxonomy(d1, d4, LINNEAN_RANK.K));
    assertTrue(DenormClassificationUtils.shareHigherTaxonomy(d1, d4, LINNEAN_RANK.P));
    assertTrue(DenormClassificationUtils.shareHigherTaxonomy(d1, d4, LINNEAN_RANK.C));
    assertTrue(DenormClassificationUtils.shareHigherTaxonomy(d1, d4, LINNEAN_RANK.O));
    assertTrue(DenormClassificationUtils.shareHigherTaxonomy(d1, d4, LINNEAN_RANK.F));
    assertTrue(DenormClassificationUtils.shareHigherTaxonomy(d1, d4, LINNEAN_RANK.G)); // it is exclusive of genera
    assertFalse(DenormClassificationUtils.shareHigherTaxonomy(d1, d4, LINNEAN_RANK.S));
  }

  @Test
  public void testEqualClassifications() {
    DenormClassification d1 = new DenormClassification("a", "b", "c", "d", "e", "f", "g", "h", "i");
    DenormClassification d2 = new DenormClassification("a", "b", "c", "d", "e", "f", "g", "h", "i");
    DenormClassification d3 = new DenormClassification(null, "b", "c", "d", "e", "f", "g", "h", "i");
    DenormClassification d4 = new DenormClassification("a", "b", "c", "d", "e", "f", "g", "H1", "i");
    assertTrue(DenormClassificationUtils.equalClassifications(d1, d2));
    assertTrue(DenormClassificationUtils.equalClassifications(d2, d1));
    assertFalse(DenormClassificationUtils.equalClassifications(d1, d3));
    assertFalse(DenormClassificationUtils.equalClassifications(d1, d4));
  }

  @Test
  public void testMergeInto() {
    DenormClassification<Integer> d1 = new DenormClassification<Integer>("a", "b", "c", "d", "e", "f", "g", "h", "i");
    DenormClassification<Integer> d2 = new DenormClassification<Integer>("a", "b", "c", "d", "e", "f", "g", "h", "i");

    d1.getPayloads().add(1);
    d1.getPayloads().add(2);
    d1.getPayloads().add(3);
    d2.getPayloads().add(1);
    d2.getPayloads().add(4);

    DenormClassificationUtils.mergeInto(d1, d2);
    assertEquals(5, d2.getPayloads().size());
    assertEquals((Integer) 1, d2.getPayloads().get(0));
    assertEquals((Integer) 4, d2.getPayloads().get(1));
    assertEquals((Integer) 1, d2.getPayloads().get(2));
    assertEquals((Integer) 2, d2.getPayloads().get(3));
    assertEquals((Integer) 3, d2.getPayloads().get(4));
  }

  @Test
  public void testMergeDuplicates() {
    List<DenormClassification> list = new ArrayList<DenormClassification>();
    list.add(new DenormClassification("a", "b", "c", "d", "e", "f", "g", "h", "i"));
    list.add(new DenormClassification("a", "b", "c", "d", "e", "f", "g", "h", "i"));
    list.add(new DenormClassification("a", "b", "c", "d", "e", "f", "g", "h", "i"));
    list.add(new DenormClassification("a", "b", "c", "d", "e", "f", "G1", "H1", "i"));
    DenormClassificationUtils.mergeDuplicates(list);
    // sort it to make sure the merge did not affect ordering
    Collections.sort(list, DenormClassificationUtils.FULL_COMPARATOR);
    assertEquals(2, list.size());
    assertEquals("a", list.get(0).getKingdom());
    assertEquals("b", list.get(0).getPhylum());
    assertEquals("c", list.get(0).getKlass());
    assertEquals("d", list.get(0).getOrder());
    assertEquals("e", list.get(0).getFamily());
    assertEquals("f", list.get(0).getGenus());
    assertEquals("G1", list.get(0).getSpecies());
    assertEquals("H1", list.get(0).getSubspecies());
    assertEquals("i", list.get(0).getAuthor());

    assertEquals("a", list.get(1).getKingdom());
    assertEquals("b", list.get(1).getPhylum());
    assertEquals("c", list.get(1).getKlass());
    assertEquals("d", list.get(1).getOrder());
    assertEquals("e", list.get(1).getFamily());
    assertEquals("f", list.get(1).getGenus());
    assertEquals("g", list.get(1).getSpecies());
    assertEquals("h", list.get(1).getSubspecies());
    assertEquals("i", list.get(1).getAuthor());

  }

  @Test
  public void testHaveConflict() {
    DenormClassification d1 = new DenormClassification("a", "b", "c", "d", "e", "f", "g", "h", "i");
    DenormClassification d2 = new DenormClassification("a", "b", "c", "d", "e", "f", "g", "h", "i");
    DenormClassification d3 = new DenormClassification(null, "b", "c", "d", "e", "f", "g", "h", "i");
    DenormClassification d4 = new DenormClassification("a", "b", "c", "d", "e", "f", "G1", "H1", "i");
    assertFalse(DenormClassificationUtils.haveConflict(d1, d2, LINNEAN_RANK.S));
    assertFalse(DenormClassificationUtils.haveConflict(d1, d3, LINNEAN_RANK.S)); // null is not conflict
    assertFalse(DenormClassificationUtils.haveConflict(d1, d4, LINNEAN_RANK.G));
    assertTrue(DenormClassificationUtils.haveConflict(d1, d4, LINNEAN_RANK.S));

  }

  @Test
  public void testInferSpecies() {
    List<DenormClassification> list = new ArrayList<DenormClassification>();
    list.add(new DenormClassification("a", "b", "c", "d", "e", "Gus", "Gus dus", "Gus dus dus", "i"));
    list.add(new DenormClassification("a", "b", "c", "d", "e", "Fus", "Gus dus", "Gus dus dus", "i"));
    list.add(new DenormClassification("a", "b", "c", "d", "e", "Fus", "Gus dus", "RUBBISHICA-SUBSPECIES", "i"));
    list.add(new DenormClassification("a", "b", "c", "d", "e", "Fus", null, "RUBBISHICA-SUBSPECIES", "i"));
    list.add(new DenormClassification("a", "b", "c", "d", "e", "Fus", null, "Gus dus dus", "i"));
    list.add(new DenormClassification("a", "b", "c", "d", "e", "Fus", null, "Gus dus var. dus", "i"));
    DenormClassificationUtils.inferSpecies(list);
    assertEquals(6, list.size());
    assertEquals("Gus dus", list.get(0).getSpecies());
    assertEquals("Gus dus", list.get(1).getSpecies());
    assertEquals("Gus dus", list.get(2).getSpecies());
    assertNull(list.get(3).getSpecies());
    assertEquals("Gus dus", list.get(4).getSpecies());
    assertEquals("Gus dus", list.get(5).getSpecies());
  }

  @Test
  public void testInferGenera() {
    List<DenormClassification> list = new ArrayList<DenormClassification>();
    list.add(new DenormClassification("a", "b", "c", "d", "e", "Gus", "Gus dus", "h", "i"));
    list.add(new DenormClassification("a", "b", "c", "d", "e", "Fus", "Gus dus", "h", "i"));
    list.add(new DenormClassification("a", "b", "c", "d", "e", "Fus", "FUS-NONSENSICUS", "h", "i"));
    list.add(new DenormClassification("a", "b", "c", "d", "e", null, "Gus dus", "h", "i"));
    list
      .add(new DenormClassification("a", "b", "c", "d", "Eus", null, "Eus", "h", "i")); // species is actually a family
    list.add(new DenormClassification("a", "b", "c", "d", "e", null, "Eus", "h", "i")); // species is genus
    DenormClassificationUtils.inferGenera(list);
    assertEquals(6, list.size());
    assertEquals("Gus", list.get(0).getGenus());
    assertEquals("Fus", list.get(1).getGenus());
    assertEquals("Fus", list.get(2).getGenus());
    assertEquals("Gus", list.get(3).getGenus());
    assertNull(list.get(4).getGenus());
    assertEquals("Eus", list.get(5).getGenus());
  }

  @Test
  public void testFullComparator() {
    DenormClassification d1 = new DenormClassification("a", "b", "c", "d", "e", "f", "g", "h", "i");
    DenormClassification d2 = new DenormClassification("a", "b", "c", "d", "e", "f", "g", "h", "i");
    assertEquals(0, DenormClassificationUtils.FULL_COMPARATOR.compare(d1, d2));
    assertEquals(0, DenormClassificationUtils.FULL_COMPARATOR.compare(d2, d1));

    d2 = new DenormClassification("a", "b", "c", "d", "e", "f", "g", null, "i");
    assertEquals(-1, DenormClassificationUtils.FULL_COMPARATOR.compare(d1, d2));
    assertEquals(1, DenormClassificationUtils.FULL_COMPARATOR.compare(d2, d1));

    d2 = new DenormClassification("a", "b", "c", "d", "e", "f", null, null, "i");
    assertEquals(-1, DenormClassificationUtils.FULL_COMPARATOR.compare(d1, d2));
    assertEquals(1, DenormClassificationUtils.FULL_COMPARATOR.compare(d2, d1));

    d2 = new DenormClassification("a", "b", "c", "d", "e", "f1", null, null, "i");
    assertEquals(-1, DenormClassificationUtils.FULL_COMPARATOR.compare(d1, d2));
    assertEquals(1, DenormClassificationUtils.FULL_COMPARATOR.compare(d2, d1));

    d2 = new DenormClassification("a", "b", "c", "d", "e1", "f1", null, null, "i");
    assertEquals(-1, DenormClassificationUtils.FULL_COMPARATOR.compare(d1, d2));
    assertEquals(1, DenormClassificationUtils.FULL_COMPARATOR.compare(d2, d1));

    d2 = new DenormClassification("a", "b", "c", "d1", "e1", "f1", null, null, "i");
    assertEquals(-1, DenormClassificationUtils.FULL_COMPARATOR.compare(d1, d2));
    assertEquals(1, DenormClassificationUtils.FULL_COMPARATOR.compare(d2, d1));

    d2 = new DenormClassification("a", "b", null, "d1", "e1", "f1", null, null, "i");
    assertEquals(-1, DenormClassificationUtils.FULL_COMPARATOR.compare(d1, d2));
    assertEquals(1, DenormClassificationUtils.FULL_COMPARATOR.compare(d2, d1));

    d2 = new DenormClassification("a", "c", null, "d1", "e1", "f1", null, null, "i");
    assertEquals(-1, DenormClassificationUtils.FULL_COMPARATOR.compare(d1, d2));
    assertEquals(1, DenormClassificationUtils.FULL_COMPARATOR.compare(d2, d1));

    d2 = new DenormClassification("a", null, null, "d1", "e1", "f1", null, null, "i");
    assertEquals(-1, DenormClassificationUtils.FULL_COMPARATOR.compare(d1, d2));
    assertEquals(1, DenormClassificationUtils.FULL_COMPARATOR.compare(d2, d1));

    d2 = new DenormClassification(null, null, null, "d1", "e1", "f1", null, null, "i");
    assertEquals(-1, DenormClassificationUtils.FULL_COMPARATOR.compare(d1, d2));
    assertEquals(1, DenormClassificationUtils.FULL_COMPARATOR.compare(d2, d1));
  }

  @Test
  public void testRankComparators() {
    DenormClassification d1 = new DenormClassification("a1", "b1", "c1", "d1", "e1", "f1", "g1", "h1", "i2");
    DenormClassification d2 = new DenormClassification("a2", "b2", "c2", "d2", "e2", "f2", "g1", "h2", "i1");
    assertEquals(-1, DenormClassificationUtils.KINGDOM_COMPARATOR.compare(d1, d2));
    assertEquals(1, DenormClassificationUtils.KINGDOM_COMPARATOR.compare(d2, d1));
    assertEquals(-1, DenormClassificationUtils.rankComparator(LINNEAN_RANK.K).compare(d1, d2));
    assertEquals(1, DenormClassificationUtils.rankComparator(LINNEAN_RANK.K).compare(d2, d1));

    assertEquals(-1, DenormClassificationUtils.PHYLUM_COMPARATOR.compare(d1, d2));
    assertEquals(1, DenormClassificationUtils.PHYLUM_COMPARATOR.compare(d2, d1));
    assertEquals(-1, DenormClassificationUtils.rankComparator(LINNEAN_RANK.P).compare(d1, d2));
    assertEquals(1, DenormClassificationUtils.rankComparator(LINNEAN_RANK.P).compare(d2, d1));

    assertEquals(-1, DenormClassificationUtils.CLASS_COMPARATOR.compare(d1, d2));
    assertEquals(1, DenormClassificationUtils.CLASS_COMPARATOR.compare(d2, d1));
    assertEquals(-1, DenormClassificationUtils.rankComparator(LINNEAN_RANK.C).compare(d1, d2));
    assertEquals(1, DenormClassificationUtils.rankComparator(LINNEAN_RANK.C).compare(d2, d1));

    assertEquals(-1, DenormClassificationUtils.ORDER_COMPARATOR.compare(d1, d2));
    assertEquals(1, DenormClassificationUtils.ORDER_COMPARATOR.compare(d2, d1));
    assertEquals(-1, DenormClassificationUtils.rankComparator(LINNEAN_RANK.O).compare(d1, d2));
    assertEquals(1, DenormClassificationUtils.rankComparator(LINNEAN_RANK.O).compare(d2, d1));

    assertEquals(-1, DenormClassificationUtils.FAMILY_COMPARATOR.compare(d1, d2));
    assertEquals(1, DenormClassificationUtils.FAMILY_COMPARATOR.compare(d2, d1));
    assertEquals(-1, DenormClassificationUtils.rankComparator(LINNEAN_RANK.F).compare(d1, d2));
    assertEquals(1, DenormClassificationUtils.rankComparator(LINNEAN_RANK.F).compare(d2, d1));

    assertEquals(-1, DenormClassificationUtils.GENUS_COMPARATOR.compare(d1, d2));
    assertEquals(1, DenormClassificationUtils.GENUS_COMPARATOR.compare(d2, d1));
    assertEquals(-1, DenormClassificationUtils.rankComparator(LINNEAN_RANK.G).compare(d1, d2));
    assertEquals(1, DenormClassificationUtils.rankComparator(LINNEAN_RANK.G).compare(d2, d1));

    // note that this is reversed deliberately to test
    assertEquals(1, DenormClassificationUtils.SPECIES_COMPARATOR.compare(d1, d2));
    assertEquals(-1, DenormClassificationUtils.SPECIES_COMPARATOR.compare(d2, d1));
    assertEquals(1, DenormClassificationUtils.rankComparator(LINNEAN_RANK.S).compare(d1, d2));
    assertEquals(-1, DenormClassificationUtils.rankComparator(LINNEAN_RANK.S).compare(d2, d1));

    assertEquals(-1, DenormClassificationUtils.SUBSPECIES_COMPARATOR.compare(d1, d2));
    assertEquals(1, DenormClassificationUtils.SUBSPECIES_COMPARATOR.compare(d2, d1));
    assertEquals(-1, DenormClassificationUtils.rankComparator(LINNEAN_RANK.SS).compare(d1, d2));
    assertEquals(1, DenormClassificationUtils.rankComparator(LINNEAN_RANK.SS).compare(d2, d1));
  }

  @Test
  public void testRankOfDeviation() {
    DenormClassification d1 = new DenormClassification("a", "a", "a", "a", "a", "a", "a", "a", "a");
    LINNEAN_RANK[] all = LinneanRank.ranksLowerThan(LINNEAN_RANK.K, true);
    int index = 0;
    for (LINNEAN_RANK r : LinneanRank.ranksLowerThan(LINNEAN_RANK.K, true)) {
      DenormClassification d2 = new DenormClassification();
      for (int i = 0; i < all.length; i++) {
        d2.set(all[i], "a");
        if (all[i] == r) break;
      }

      LOG.debug("Comparing deviation between:");
      LOG.debug(d2.toString());
      LOG.debug(d2.toString());
      assertNull(DenormClassificationUtils.rankOfDeviation(d2, d2));

      LOG.debug("Comparing deviation between:");
      LOG.debug(d1.toString());
      LOG.debug(d2.toString());
      index++;
      if (index < all.length) assertEquals(all[index], DenormClassificationUtils.rankOfDeviation(d1, d2));
    }

    // do species tests at species and subspecies level to test the FSM
    d1 = new DenormClassification("a", "a", "a", "a", "a", "a", "a", "a", "a");
    DenormClassification d2 = new DenormClassification("a", "a", "a", "a", "a", "a", "a", "a", "a");
    assertNull(DenormClassificationUtils.rankOfDeviation(d1, d2));
    d2 = new DenormClassification("a", "a", "a", "a", "a", "a", "a", "a", null);
    assertEquals(LINNEAN_RANK.SS, DenormClassificationUtils.rankOfDeviation(d1, d2));
    d2 = new DenormClassification("a", "a", "a", "a", "a", "a", "a", null, null);
    assertEquals(LINNEAN_RANK.SS, DenormClassificationUtils.rankOfDeviation(d1, d2));
    d2 = new DenormClassification("a", "a", "a", "a", "a", "a", "a", "b", null);
    assertEquals(LINNEAN_RANK.SS, DenormClassificationUtils.rankOfDeviation(d1, d2));
    d2 = new DenormClassification("a", "a", "a", "a", "a", "a", "b", "b", null);
    assertEquals(LINNEAN_RANK.S, DenormClassificationUtils.rankOfDeviation(d1, d2));

    d1 = new DenormClassification("a", "a", "a", "a", "a", "a", "a", null, "a");
    d2 = new DenormClassification("a", "a", "a", "a", "a", "a", "a", "b", null);
    assertEquals(LINNEAN_RANK.SS, DenormClassificationUtils.rankOfDeviation(d1, d2));
    d2 = new DenormClassification("a", "a", "a", "a", "a", "a", "b", "b", null);
    assertEquals(LINNEAN_RANK.S, DenormClassificationUtils.rankOfDeviation(d1, d2));
    d2 = new DenormClassification("a", "a", "a", "a", "a", "a", "a", "b", null);
    assertEquals(LINNEAN_RANK.SS, DenormClassificationUtils.rankOfDeviation(d1, d2));
    d2 = new DenormClassification("a", "a", "a", "a", "a", null, null, null, null);
    assertEquals(LINNEAN_RANK.G, DenormClassificationUtils.rankOfDeviation(d1, d2));

    d1 = new DenormClassification(null, "Chordata", "Mammalia", null, "Muridae", null, null, null, null);
    d2 = new DenormClassification(null, "Chordata", "Mammalia", null, "Muridae", null, null, null, "Pleuronectiformes");
    assertEquals(LINNEAN_RANK.F, DenormClassificationUtils.rankOfDeviation(d1, d2));

  }
}
