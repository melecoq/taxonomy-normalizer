package org.gbif.taxonomy.norm;

import org.gbif.taxonomy.norm.DenormClassification;
import org.gbif.taxonomy.norm.DenormClassificationUtils;
import org.gbif.taxonomy.norm.NormClassification;
import org.gbif.taxonomy.norm.Normalizer;
import org.gbif.taxonomy.norm.LinneanRank.LINNEAN_RANK;
import org.gbif.taxonomy.norm.util.NormClassificationFactory;
import org.gbif.taxonomy.norm.util.TestDataFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class NormalizerTest {

  private static final Logger LOG = LoggerFactory.getLogger(NormalizerTest.class);

  // Basically a bootstrap to check all is working for simplest case
  @Test
  public void testSimpleMerge() {
    try {
      Normalizer n = new Normalizer();
      List<DenormClassification> input = TestDataFactory.build("/data/taxonomy/merge/1_denorm.txt", false);
      List<DenormClassification> expected = TestDataFactory.build("/data/taxonomy/merge/1_merged.txt", false);
      Map<LINNEAN_RANK, Set<String>> homonyms = n.createHomonymCache();
      n.sortAndMerge(LINNEAN_RANK.S, input, homonyms);
      assertEquivalent(expected, input);

    } catch (IOException e) {
      e.printStackTrace();
      fail("Factory throwing unexpected exceptions: " + e.getMessage());
    }
  }

  // Do an explicit subspecies test
  @Test
  public void testSimpleSubspecies() {
    Normalizer n = new Normalizer();
    List<DenormClassification> input = new ArrayList<DenormClassification>();
    input.add(new DenormClassification("a", "b", "c", "d", "e", "f", "g", "h", "i"));
    input.add(new DenormClassification(null, "b", "c", "d", "e", "f", null, "h", "i"));
    input.add(new DenormClassification(null, "b", "c", "d", "e", "f", "g", null, "i"));

    List<DenormClassification> expected = new ArrayList<DenormClassification>();
    expected.add(new DenormClassification("a", "b", "c", "d", "e", "f", "g", "h", "i"));
    expected.add(new DenormClassification("a", "b", "c", "d", "e", "f", "g", null, "i"));

    n.normalize(input);
    assertEquivalent(expected, input);
  }

  // Tests all the taxonomy merges using the file based inputs and outputs
  @Test
  public void testAllMerged() {
    // catch if someone messing with the project structure without knowing
    if (getClass().getResourceAsStream("/data/taxonomy/merge/1_denorm.txt") == null) {
      fail("No taxonomy tests exist.  Does /data/taxonomy/merge/* exist on the classpath?");
    }

    int test = 1;
    while (true) {
      // check the input and output exist, if not we are at the end
      if (getClass().getResourceAsStream("/data/taxonomy/merge/" + test + "_denorm.txt") == null
        || getClass().getResourceAsStream("/data/taxonomy/merge/" + test + "_merged.txt") == null) {
        break;
      } else {
        LOG.info("\n\nStarting test " + test);
        try {
          Normalizer n = new Normalizer();
          List<DenormClassification> input =
            TestDataFactory.build("/data/taxonomy/merge/" + test + "_denorm.txt", false);
          List<DenormClassification> expected =
            TestDataFactory.build("/data/taxonomy/merge/" + test + "_merged.txt", false);
          DenormClassificationUtils.debug(input);
          Map<LINNEAN_RANK, Set<String>> homonyms = n.createHomonymCache();

          n.sortAndMerge(LINNEAN_RANK.SS, input, homonyms);
          n.sortAndMerge(LINNEAN_RANK.S, input, homonyms);
          n.sortAndMerge(LINNEAN_RANK.G, input, homonyms);
          n.sortAndMerge(LINNEAN_RANK.F, input, homonyms);
          n.sortAndMerge(LINNEAN_RANK.O, input, homonyms);
          n.sortAndMerge(LINNEAN_RANK.C, input, homonyms);
          n.sortAndMerge(LINNEAN_RANK.P, input, homonyms);
          n.sortAndMerge(LINNEAN_RANK.K, input, homonyms);
          DenormClassificationUtils.debug(input);

          Collections.sort(expected, DenormClassificationUtils.FAMILY_COMPARATOR);
          Collections.sort(input, DenormClassificationUtils.FAMILY_COMPARATOR);
          LOG.info("Expected:");
          DenormClassificationUtils.debug(expected);
          LOG.info("Found:");
          DenormClassificationUtils.debug(input);

          assertEquivalent(expected, input);

        } catch (IOException e) {
          e.printStackTrace();
          fail("Factory throwing unexpected exceptions: " + e.getMessage());
        }
      }
      test++;
    }
  }

  // Tests some simple normalization examples
  @Test
  public void testSimpleNormalization() {
    List<DenormClassification> input = buildSimpleDenorm();
    Normalizer n = new Normalizer();
    List<NormClassification> results = n.normalize(input);

    assertEquals(5, results.size());
    assertEquals((Integer) 1, results.get(0).getId());
    assertEquals("a", results.get(0).getScientificName());
    assertEquals(LINNEAN_RANK.K.toString(), results.get(0).getRank());
    assertNull(results.get(0).getParentId());
    assertNull(results.get(0).getAuthor());

    assertEquals((Integer) 2, results.get(1).getId());
    assertEquals("f", results.get(1).getScientificName());
    assertEquals(LINNEAN_RANK.G.toString(), results.get(1).getRank());
    assertEquals((Integer) 1, results.get(1).getParentId());
    assertNull(results.get(1).getAuthor());

    // note this has the updated authorship
    assertEquals((Integer) 3, results.get(2).getId());
    assertEquals("g", results.get(2).getScientificName());
    assertEquals(LINNEAN_RANK.S.toString(), results.get(2).getRank());
    assertEquals((Integer) 2, results.get(2).getParentId());
    assertEquals("i", results.get(2).getAuthor());

    assertEquals((Integer) 4, results.get(3).getId());
    assertEquals("h", results.get(3).getScientificName());
    assertEquals(LINNEAN_RANK.SS.toString(), results.get(3).getRank());
    assertEquals((Integer) 3, results.get(3).getParentId());
    assertEquals("i", results.get(3).getAuthor());

    assertEquals((Integer) 5, results.get(4).getId());
    assertEquals("h", results.get(4).getScientificName());
    assertEquals(LINNEAN_RANK.S.toString(), results.get(4).getRank());
    assertEquals((Integer) 2, results.get(2).getParentId());
    assertEquals("i", results.get(4).getAuthor());
  }

  /**
   * @return A simply
   */
  private List<DenormClassification> buildSimpleDenorm() {
    List<DenormClassification> input = new ArrayList<DenormClassification>();
    input.add(new DenormClassification("a", null, null, null, null, "f", "g", "h", "i"));
    input.add(new DenormClassification("a", null, null, null, null, "f", "g", null, "i"));
    input.add(new DenormClassification("a", null, null, null, null, "f", "h", null, "i"));
    return input;
  }

  // This payload test uses the same as the testSimpleNormalization but tests payloads only
  @Test
  public void testNormalizationWithPayloads() {
    List<DenormClassification> input = buildSimpleDenorm();

    // add some payloads
    int id = 1;
    int i = 0;
    for (DenormClassification d : input) {
      for (int j = 0; j <= i; j++) {
        d.getPayloads().add(id++);
      }
      i++;
    }

    Normalizer n = new Normalizer();
    List<NormClassification> results = n.normalize(input);

    // first row in denorm
    assertEquals(1, results.get(3).getPayloads().size());
    assertEquals(1, results.get(3).getPayloads().get(0));

    // second row
    assertEquals(2, results.get(2).getPayloads().size());
    assertEquals(2, results.get(2).getPayloads().get(0));
    assertEquals(3, results.get(2).getPayloads().get(1));

    // third row
    assertEquals(3, results.get(4).getPayloads().size());
    assertEquals(4, results.get(4).getPayloads().get(0));
    assertEquals(5, results.get(4).getPayloads().get(1));
    assertEquals(6, results.get(4).getPayloads().get(2));
  }

  /**
   * This payload test checks that payloads are handled correctly
   * with subspecies when species are inferred
   */
  @Test
  public void testPayloadsSubspecies() {
    List<DenormClassification> input = new ArrayList<DenormClassification>();
    input.add(new DenormClassification(null, null, null, null, null, null, "Achnanthes austriaca", null, null));
    input.add(
      new DenormClassification(null, null, null, null, null, null, null, "Achnanthes austriaca var. parallela Krasske",
        null));
    input.add(
      new DenormClassification(null, null, null, null, null, null, null, "Achnanthes austriaca var. parallela Tim",
        null));
    input.get(0).getPayloads().add(new Integer(1));
    input.get(1).getPayloads().add(new Integer(2));
    input.get(2).getPayloads().add(new Integer(3));

    Normalizer n = new Normalizer();
    List<NormClassification> results = n.normalize(input);

    for (NormClassification no : results) {
      LOG.info(no.toString() + ": " + no.getPayloads());
    }

    // sanity check the results
    assertEquals(4, results.size());
    assertEquals("Achnanthes", results.get(0).getScientificName());
    assertEquals("Achnanthes austriaca", results.get(1).getScientificName());
    assertEquals("Achnanthes austriaca var. parallela Krasske", results.get(2).getScientificName());
    assertEquals("Achnanthes austriaca var. parallela Tim", results.get(3).getScientificName());

    // check that the subspecies payload does not exist on the species
    // http://code.google.com/p/gbif-occurrencestore/issues/detail?id=2
    assertEquals(1, results.get(1).getPayloads().size());
    assertEquals(1, results.get(1).getPayloads().get(0));

    // check that the correct payload exists on the subspecies
    assertEquals(1, results.get(2).getPayloads().size());
    assertEquals(2, results.get(2).getPayloads().get(0));
    assertEquals(1, results.get(3).getPayloads().size());
    assertEquals(3, results.get(3).getPayloads().get(0));
  }


  // Tests all the taxonomy merges using the file based inputs and outputs
  @Test
  public void testAllNormalization() {
    // catch if someone messing with the project structure without knowing
    if (getClass().getResourceAsStream("/data/taxonomy/norm/1_denorm.txt") == null) {
      fail("No taxonomy tests exist.  Does /data/taxonomy/norm/* exist on the classpath?");
    }

    int test = 1;
    while (true) {
      // check the input and output exist, if not we are at the end
      if (getClass().getResourceAsStream("/data/taxonomy/norm/" + test + "_denorm.txt") == null
        || getClass().getResourceAsStream("/data/taxonomy/norm/" + test + "_norm.txt") == null) {
        break;
      } else {
        LOG.info("\n\nStarting test " + test);
        try {
          Normalizer n = new Normalizer();
          List<DenormClassification> input =
            TestDataFactory.build("/data/taxonomy/norm/" + test + "_denorm.txt", false);
          List<NormClassification> expected =
            NormClassificationFactory.build("/data/taxonomy/norm/" + test + "_norm.txt");
          DenormClassificationUtils.debug(input);
          List<NormClassification> result = n.normalize(input);

          Collections.sort(input, DenormClassificationUtils.FAMILY_COMPARATOR);
          LOG.info("Input:");
          DenormClassificationUtils.debug(input);
          assertEquivalent(expected, result);

        } catch (IOException e) {
          e.printStackTrace();
          fail("Factory throwing unexpected exceptions: " + e.getMessage());
        }
      }
      test++;
    }
  }


  // utility to check line by line the classifications
  private void assertEquivalent(List<? extends Object> expected, List<? extends Object> found) {
    assertTrue(expected.size() <= found.size());
    for (int i = 0; i < expected.size(); i++) {
      if (expected.get(i) == null) {
        assertNull(found.get(i));
      } else if (found.get(i) == null) {
        assertNull(expected.get(i));
      } else {
        if (expected.get(i) != null && found.get(i) != null) {
          assertEquals(expected.get(i).toString(), found.get(i).toString());
        } else {
          assertEquals(expected.get(i), found.get(i));
        }
      }
    }
    // log any that are wrong
    if (expected.size() != found.size()) LOG.info(" Unexpected taxa found(" + (found.size() - expected.size()) + "):");
    for (int i = expected.size() - 1; i < found.size() - 1; i++) {
      LOG.info("  " + found.get(i));
    }
    assertEquals(expected.size(), found.size());
  }
}
