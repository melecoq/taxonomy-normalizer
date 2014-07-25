package org.gbif.taxonomy.norm;

import org.gbif.dwc.record.DarwinCoreRecord;
import org.gbif.dwc.text.Archive;
import org.gbif.dwc.text.ArchiveFactory;
import org.gbif.dwc.text.UnsupportedArchiveException;
import org.gbif.utils.file.ClosableIterator;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import com.beust.jcommander.IStringConverter;
import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.internal.Lists;
import com.google.common.base.Throwables;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DwcaNormalizer {

  public static class FileConverter implements IStringConverter<File> {

    @Override
    public File convert(String value) {
      return new File(value);
    }
  }

  public static class URLConverter implements IStringConverter<URL> {

    @Override
    public URL convert(String value) {
      try {
        return new URL(value);
      } catch (MalformedURLException e) {
        throw Throwables.propagate(e);
      }
    }
  }

  private static final Logger LOG = LoggerFactory.getLogger(DwcaNormalizer.class);

  @Parameter(names = "-file", description = "Path of the input DwC-A", converter = FileConverter.class)
  public File sourceFile;

  @Parameter(names = "-url", description = "URL of the input DwC-A - if supplied will be used instead of the -file",
    converter = URLConverter.class)
  public URL sourceUrl;

  @Parameter(names = "-output", description = "File path for the output dir which must exist (default = /tmp)")
  public File outDir = new File("/tmp");

  public static void main(String[] args) throws Exception {
    DwcaNormalizer app = new DwcaNormalizer();
    new JCommander(app, args);
    if (app.sourceFile == null && app.sourceUrl == null) {
      System.out.println("Either sourceUrl or sourceFile must be supplied");
    } else {
      app.run();
    }
  }

  private void run() throws UnsupportedArchiveException, IOException {
    Archive archive = null;
    if (sourceUrl != null) {
      archive = ArchiveFactory.openArchive(sourceUrl, new File(System.getProperty("java.io.tmpdir")));
    } else {
      archive = ArchiveFactory.openArchive(sourceFile, new File(System.getProperty("java.io.tmpdir")));
    }

    ClosableIterator<DarwinCoreRecord> iter = archive.iteratorDwc();
    List<DenormClassification> denorm = Lists.newArrayList();
    LOG.info("Reading archive");
    int count = 0;
    try {
      while (iter.hasNext()) {
        DarwinCoreRecord record = iter.next();

        denorm.add(new DenormClassification(
          record.getKingdom(),
          record.getPhylum(),
          record.getClasss(),
          record.getOrder(),
          record.getFamily(),
          record.getGenus(),
          // the following is wrong, as it will put subspecies in here
          // consider using NameParser
          record.getScientificName(),
          record.getInfraspecificEpithet(),
          record.getScientificNameAuthorship()));

        if (++count % 10000 == 0) {
          LOG.info("Read {} records from archive", denorm.size());
          break;
        }
      }
    } finally {
      iter.close();
    }
    LOG.info("Read {} records from archive", denorm.size());

    Normalizer n = new Normalizer();
    LOG.info("Normalizing archive");
    List<NormClassification> norm = n.normalize(denorm);

    BufferedWriter bw = new BufferedWriter(new FileWriter(new File(outDir, "normalized.txt")));
    for (NormClassification taxon : norm) {
      bw.write(
        taxon.getId() + "|" +
          taxon.getParentId() + "|" +
          taxon.getRank() + "|" +
          taxon.getScientificName() + "|" +
          taxon.getAuthor() + "|\n");
    }
    bw.close();
    LOG.info("Normalized resulted in {} records", norm.size());
  }
}
