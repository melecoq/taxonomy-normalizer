package org.gbif.taxonomy.norm;

import org.gbif.ecat.model.ParsedName;
import org.gbif.ecat.parser.NameParser;
import org.gbif.ecat.parser.UnparsableException;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A collection of utility factories.
 */
public class DenormClassificationFactory {

  private static final Logger LOG = LoggerFactory.getLogger(DenormClassificationFactory.class);
  private static final NameParser PARSER = new NameParser();

  private DenormClassificationFactory() {
    throw new UnsupportedOperationException("Can't initialize class");
  }

  /**
   * @see #build(Iterator, boolean)
   */
  public static List<DenormClassification> build(Iterable<Map<String, String>> source,
    boolean interpretScientificName) {
    return build(source.iterator(), interpretScientificName);
  }

  /**
   * Builds from source data using the fields in the Map keyed as follows: - kingdom - phylum - class - order - family
   * - genus - species - subspecies - author and if interpretScientificName is set to true, then scientificName will be
   * used to set species, subspecies and author where missing.
   * 
   * @param source Data to build from
   * @param interpretScientificName If set to true, will attempt to infer species and subspecies from a field called
   *        scientificName
   */
  public static List<DenormClassification> build(Iterator<Map<String, String>> source,
    boolean interpretScientificName) {
    List<DenormClassification> data = new ArrayList<DenormClassification>();

    int lineCount = 0;
    while (source.hasNext()) {
      Map<String, String> record = source.next();
      DenormClassification d =
        new DenormClassification(record.get("kingdom"), record.get("phylum"), record.get("class"), record.get("order"),
          record.get("family"), record.get("genus"), record.get("species"), record.get("subspecies"),
          record.get("author"));
      if (interpretScientificName) {
        String scientificName = record.get("scientificName");
        if (StringUtils.isNotBlank(scientificName)) {
          try {
            ParsedName<?> p = PARSER.parse(scientificName);
            if (p != null && StringUtils.isNotBlank(p.getInfraSpecificEpithet())) {
              d.setSubspecies(StringUtils.trimToNull(p.fullName()));
              if (StringUtils.isBlank(d.getAuthor())) {
                d.setAuthor(StringUtils.trimToNull(p.authorshipComplete()));
              }
              d.setSpecies(null);
            } else if (p != null && p.isBinomial()) {
              d.setSpecies(StringUtils.trimToNull(p.fullName()));
              if (StringUtils.isBlank(d.getAuthor())) {
                d.setAuthor(StringUtils.trimToNull(p.authorshipComplete()));
              }
              d.setSubspecies(null);
            }
          } catch (UnparsableException e) {
            // a name we can't handle
          }
        }
      }
      data.add(d);
      lineCount++;
      if (lineCount % 1000 == 0) {
        LOG.info("Built {} records", lineCount);
      }

    }
    return data;
  }
}
