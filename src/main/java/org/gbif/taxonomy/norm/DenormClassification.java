package org.gbif.taxonomy.norm;

import org.gbif.taxonomy.norm.LinneanRank.LINNEAN_RANK;

import java.util.ArrayList;
import java.util.List;

/**
 * The denormalised classification.
 *
 * @param <T> refers to the type of object in the payload
 */
public class DenormClassification<T> {

  private String kingdom;
  private String phylum;
  private String klass;
  private String order;
  private String family;
  private String genus;

  // DarwinCore gives us scientificName, but for a true denormalized view we desire definitive ranks
  private String species;
  private String subspecies;
  // scientific name is therefore not accessible in this class
  // see the TestDataFactory as an example for circumventing this
  // by using inference through parsing
  // protected String scientificName;

  private String author; // considered the author of the lowest rank if provided

  // we allow an arbitrary list payload objects to be associated with
  // the denormalized classification.  Typically this would be some
  // identifiers which ultimately are used as foreign keys to complete
  // a final join.  For example 10 occurrence records might have
  // the same taxonomy, and this would hold the 10 occurrence IDs
  private final List<T> payloads = new ArrayList<T>();

  public DenormClassification() {
    // default constructor which doesn't initialize any fields
  }

  public DenormClassification(String kingdom, String phylum, String klass, String order, String family, String genus,
    String species, String subspecies, String author) {
    this.kingdom = kingdom;
    this.phylum = phylum;
    this.klass = klass;
    this.order = order;
    this.family = family;
    this.genus = genus;
    this.species = species;
    this.subspecies = subspecies;
    this.author = author;
  }

  @Override
  public String toString() {
    String[] parts = {kingdom, phylum, klass, order, family, genus, species, subspecies, author};

    StringBuilder sb = new StringBuilder();
    boolean firstWritten = false;
    for (String part : parts) {
      if (part == null) {
        if (firstWritten) {
          sb.append("|");
        }
        sb.append("--");
        firstWritten = true;
      } else {
        if (firstWritten) {
          sb.append("|");
        }
        sb.append(part);
        firstWritten = true;
      }
    }
    return sb.toString();
  }

  /**
   * Utility getter.
   *
   * @param rank To return
   *
   * @return The value for the rank
   */
  public String get(LINNEAN_RANK rank) {
    switch (rank) {
      case K:
        return getKingdom();
      case P:
        return getPhylum();
      case C:
        return getKlass();
      case O:
        return getOrder();
      case F:
        return getFamily();
      case G:
        return getGenus();
      case S:
        return getSpecies();
      case SS:
        return getSubspecies();
      default:
        return null;
    }
  }

  /**
   * Utility setter.
   *
   * @param rank  To set
   * @param value The value to set
   */
  public void set(LINNEAN_RANK rank, String value) {
    switch (rank) {
      case K:
        setKingdom(value);
        break;
      case P:
        setPhylum(value);
        break;
      case C:
        setKlass(value);
        break;
      case O:
        setOrder(value);
        break;
      case F:
        setFamily(value);
        break;
      case G:
        setGenus(value);
        break;
      case S:
        setSpecies(value);
        break;
      case SS:
        setSubspecies(value);
        break;
    }
  }

  public String getKingdom() {
    return kingdom;
  }

  public void setKingdom(String kingdom) {
    this.kingdom = kingdom;
  }

  public String getPhylum() {
    return phylum;
  }

  public void setPhylum(String phylum) {
    this.phylum = phylum;
  }

  public String getKlass() {
    return klass;
  }

  public void setKlass(String klass) {
    this.klass = klass;
  }

  public String getOrder() {
    return order;
  }

  public void setOrder(String order) {
    this.order = order;
  }

  public String getFamily() {
    return family;
  }

  public void setFamily(String family) {
    this.family = family;
  }

  public String getGenus() {
    return genus;
  }

  public void setGenus(String genus) {
    this.genus = genus;
  }

  public String getAuthor() {
    return author;
  }

  public void setAuthor(String author) {
    this.author = author;
  }

  public List<T> getPayloads() {
    return payloads;
  }

  public String getSpecies() {
    return species;
  }

  public void setSpecies(String species) {
    this.species = species;
  }

  public String getSubspecies() {
    return subspecies;
  }

  public void setSubspecies(String subspecies) {
    this.subspecies = subspecies;
  }
}
