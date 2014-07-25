package org.gbif.taxonomy.norm.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A very simple CSV reader to be used for tests. Does not handle any escaping nor does it handle encodings, and the
 * first line must be the header line.
 */
public class CSVReader {

  private static final Logger LOG = LoggerFactory.getLogger(CSVReader.class);
  protected static final Pattern DELIM = Pattern.compile("\t");

  /**
   * @param input The input data
   *
   * @return An ordered list of Map<ColumnName, ColumnValue>
   */
  public static List<Map<String, String>> parse(Reader input) throws IOException {
    BufferedReader b = new BufferedReader(input);
    List<Map<String, String>> output = new ArrayList<Map<String, String>>();
    try {
      String l = b.readLine();
      String[] headerRow = DELIM.split(l);
      l = b.readLine();
      int line = 0;
      while (l != null && !l.isEmpty()) {
        Map<String, String> row = new HashMap<String, String>();
        output.add(row);
        String[] fields = DELIM.split(l);
        line++;
        for (int i = 0; i < fields.length; i++) {
          try {
            // ignore nulls (show up as zero length strings without this test)
            if (!fields[i].isEmpty()) {
              row.put(headerRow[i], fields[i]);
            }
          } catch (ArrayIndexOutOfBoundsException e) {
            // swallow bad lines
            LOG.warn("More records on row[{}] than header declares", line);

          }
        }
        // sometimes the last record is empty
        for (int j = fields.length; j < headerRow.length; j++) {
          row.put(headerRow[j], null);
        }

        l = b.readLine();
      }


    } finally {
      b.close();
    }
    return output;
  }
}
