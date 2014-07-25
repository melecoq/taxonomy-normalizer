package org.gbif.taxonomy.norm.util;

import org.gbif.taxonomy.norm.DenormClassification;
import org.gbif.taxonomy.norm.DenormClassificationFactory;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Map;

/**
 * Utility to build from CSV files.
 */
public class TestDataFactory {

  /**
   * @param path on the classpath for the resource
   * @param interpretScientificName If set to true then the scientific name will be interpreted into species and
   *        subspecies
   * @return The List of denormalized classifications
   * @throws IOException If the path cannot be read or the CSV is not valid
   */
  public static List<DenormClassification> build(String path, boolean interpretScientificName)
    throws IOException {
    Iterable<Map<String, String>> csv =
      CSVReader.parse(new InputStreamReader(NormClassificationFactory.class.getResourceAsStream(path)));
    return DenormClassificationFactory.build(csv, interpretScientificName);
  }
}
