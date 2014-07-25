package org.gbif.taxonomy.norm.util;

import org.gbif.taxonomy.norm.NormClassification;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Utility to build from CSV files.
 */
public class NormClassificationFactory {

  /**
   * @param path on the classpath for the resource
   *
   * @return The List of normalized classifications
   *
   * @throws IOException If the path cannot be read or the CSV is not valid
   */
  public static List<NormClassification> build(String path) throws IOException {
    List<NormClassification> data = new ArrayList<NormClassification>();
    List<Map<String, String>> csv =
      CSVReader.parse(new InputStreamReader(TestDataFactory.class.getResourceAsStream(path)));
    for (Map<String, String> line : csv) {
      data.add(
        new NormClassification(IntOrNull(line.get("id")), IntOrNull(line.get("parentId")), line.get("scientificName"),
          line.get("author"), line.get("rank")));
    }
    return data;
  }

  protected static Integer IntOrNull(String s) {
    try {
      return Integer.parseInt(s);
    } catch (NumberFormatException e) {
      return null;
    }
  }
}
