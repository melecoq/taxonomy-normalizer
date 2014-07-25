package org.gbif.taxonomy.norm;

import org.gbif.taxonomy.norm.DenormClassification;
import org.gbif.taxonomy.norm.DenormClassificationUtils;
import org.gbif.taxonomy.norm.Normalizer;
import org.gbif.taxonomy.norm.util.TestDataFactory;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import static org.junit.Assert.fail;

/**
 * A utility to merge and sorted results streaming the result to a file
  */
public class MergeAndSortToFile {

  private static final Logger LOG = LoggerFactory.getLogger(MergeAndSortToFile.class);

  /**
   * @param args input output
   */
  @SuppressWarnings("unchecked")
  public static void main(String[] args) {
    String source = args[0];
    String output = args[1];

    try {
      LOG.info("Loading data");
      List<DenormClassification> denorm = TestDataFactory.build(source, true);
      LOG.info("Loaded data");
      Normalizer n = new Normalizer();
      n.normalize(denorm);

      // sort to help view the output
      Collections.sort(denorm, DenormClassificationUtils.FULL_COMPARATOR);

      BufferedWriter writer = new BufferedWriter(new FileWriter(output));
      for (DenormClassification d : denorm) {
        writer.write(d.toString());
        writer.write("\n");
      }
      writer.close();

    } catch (IOException e) {
      fail("TestDataFactory throwing unexpected exceptions: " + e.getMessage());
    }

  }

}
