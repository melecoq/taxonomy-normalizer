package org.gbif.taxonomy.norm;

import org.gbif.ecat.model.ParsedName;
import org.gbif.ecat.parser.NameParser;
import org.gbif.ecat.parser.UnparsableException;
import org.gbif.taxonomy.norm.LinneanRank.LINNEAN_RANK;

import java.io.Serializable;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.beanutils.BeanComparator;
import org.apache.commons.collections.comparators.ComparatorChain;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Utilities related to DenormClassification objects Note: The comparators supplied in this utility are considered
 * threadsafe <b>only</b> if the clients do not modify the comparator chain. If they do all bets are off.
 */
public class DenormClassificationUtils {

  /**
   * A null aware comparison, that treats nulls as always coming after something with a value.
   */
  protected static class NullAwareStringComparator implements Comparator<String>, Serializable {

    @Override
    public int compare(String s1, String s2) {
      if (s2 == null) {
        return s1 == null ? 0 : -1;
      }
      if (s1 == null) {
        return 1;
      }
      return s1.compareTo(s2);
    }
  }

  private static final Logger LOG = LoggerFactory.getLogger(DenormClassificationUtils.class);

  private static final NameParser PARSER = new NameParser();
  // comparators are threadsafe AFTER construction, so provide useful singletons
  public static final Comparator<DenormClassification> FULL_COMPARATOR = newFullComparator();
  public static final Comparator<DenormClassification> KINGDOM_COMPARATOR = newRankComparator(LINNEAN_RANK.K);
  public static final Comparator<DenormClassification> PHYLUM_COMPARATOR = newRankComparator(LINNEAN_RANK.P);
  public static final Comparator<DenormClassification> CLASS_COMPARATOR = newRankComparator(LINNEAN_RANK.C);
  public static final Comparator<DenormClassification> ORDER_COMPARATOR = newRankComparator(LINNEAN_RANK.O);
  public static final Comparator<DenormClassification> FAMILY_COMPARATOR = newRankComparator(LINNEAN_RANK.F);
  public static final Comparator<DenormClassification> GENUS_COMPARATOR = newRankComparator(LINNEAN_RANK.G);
  public static final Comparator<DenormClassification> SPECIES_COMPARATOR = newRankComparator(LINNEAN_RANK.S);

  public static final Comparator<DenormClassification> SUBSPECIES_COMPARATOR = newRankComparator(LINNEAN_RANK.SS);

  /**
   * For each DC in the list, it is logged at debug level.
   *
   * @param denorm To log at debug level
   */
  public static void debug(List<DenormClassification> denorm) {
    if (LOG.isDebugEnabled()) {
      for (DenormClassification d : denorm) {
        LOG.debug(d.toString());
      }
    }
  }

  /**
   * Compares the classifications of the source and target and determines if they are the same.
   *
   * @param source To check with the target
   * @param target To check with the source
   *
   * @return true if they represent the same classification
   */
  public static boolean equalClassifications(DenormClassification source, DenormClassification target) {
    return new EqualsBuilder().append(source.getKingdom(), target.getKingdom())
      .append(source.getPhylum(), target.getPhylum()).append(source.getKlass(), target.getKlass())
      .append(source.getOrder(), target.getOrder()).append(source.getFamily(), target.getFamily())
      .append(source.getGenus(), target.getGenus()).append(source.getSpecies(), target.getSpecies())
      .append(source.getSubspecies(), target.getSubspecies()).append(source.getAuthor(), target.getAuthor()).isEquals();
  }

  /**
   * Checks if 2 classifications have a conflict in the higher ranks in question. A conflict is only considered as 2
   * non null values that differ. A NULL and a NON NULL value are not said to be in conflict
   *
   * @param source To compare with target
   * @param target To compare with source
   * @param rank   To check to INCLUSIVE.  Supplying FAMILY will check to FAMILY inclusive
   *
   * @return true if they conflict or false otherwise
   */
  public static boolean haveConflict(DenormClassification source, DenormClassification target, LINNEAN_RANK rank) {
    for (LINNEAN_RANK r : LinneanRank.ranksHigherThan(rank, true)) {
      String v1 = source.get(r);
      String v2 = target.get(r);
      if (!StringUtils.isBlank(v1) && !StringUtils.isBlank(v2) && !StringUtils.equals(v1, v2)) {
        return true; // there is a clash
      }
    }
    return false;
  }

  /**
   * Infer genera where missing.
   *
   * @param denorm To iterate and infer over
   */
  public static void inferGenera(List<DenormClassification> denorm) {
    LOG.debug("Inferring genera for {} classifications", denorm.size());
    for (DenormClassification d : denorm) {
      if (StringUtils.isBlank(d.getGenus()) && StringUtils.isNotBlank(d.getSpecies())) {

        try {
          ParsedName<Object> pn = PARSER.parse(d.getSpecies());
          if (pn != null && pn.getGenusOrAbove() != null) {
            // make sure we are not mistaking it as a higher taxa
            String tentativeGenus = pn.getGenusOrAbove();
            if (!StringUtils.equalsIgnoreCase(tentativeGenus, d.getKingdom()) &&
              !StringUtils.equalsIgnoreCase(tentativeGenus, d.getPhylum()) &&
              !StringUtils.equalsIgnoreCase(tentativeGenus, d.getKlass()) &&
              !StringUtils.equalsIgnoreCase(tentativeGenus, d.getOrder()) &&
              !StringUtils.equalsIgnoreCase(tentativeGenus, d.getFamily())) {
              // looks like a candidate to be a genus, or we have some very poor data
              d.setGenus(tentativeGenus);
            }
          }
        } catch (UnparsableException e) {
          // a name we can't handle
        }
      }
    }
    LOG.debug("Inferred genera for {} classifications", denorm.size());
  }

  /**
   * Infer species binomials where missing but a subspecies is known.
   *
   * @param denorm To iterate and infer over
   */
  public static void inferSpecies(List<DenormClassification> denorm) {
    LOG.debug("Inferring species for {} classifications", denorm.size());
    for (DenormClassification d : denorm) {
      if (StringUtils.isBlank(d.getSpecies()) && StringUtils.isNotBlank(d.getSubspecies())) {

        try {
          ParsedName<Object> pn = PARSER.parse(d.getSubspecies());
          if (pn != null && pn.getGenusOrAbove() != null && pn.getSpecificEpithet() != null) {
            // make sure we are not mistaking a monomial as a higher taxa due to bad data
            String tentativeGenus = pn.getGenusOrAbove();
            String tentativeSpecificEpithet = pn.getSpecificEpithet();

            boolean duplicate = false;
            for (LINNEAN_RANK r : LinneanRank.ranksHigherThan(LINNEAN_RANK.F, true)) {
              duplicate = StringUtils.equalsIgnoreCase(tentativeGenus, d.get(r)) || StringUtils
                .equalsIgnoreCase(tentativeSpecificEpithet, d.get(r));
              if (duplicate) {
                break;
              }
            }

            // neither the specific epithet or generic epithet are repeated in the higher taxa, so
            // construct a binomial
            if (!duplicate) {
              d.setSpecies(tentativeGenus + ' ' + tentativeSpecificEpithet);
            }
          }
        } catch (UnparsableException e) {
          // a name we can't handle
        }
      }
    }
    LOG.debug("Inferred species for {} classifications", denorm.size());
  }

  /**
   * This will merge DenormClassification that have equivalent classifications into a single object.
   *
   * @param denorm the list of items to merge
   */
  public static void mergeDuplicates(List<DenormClassification> denorm) {
    Collections.sort(denorm, FULL_COMPARATOR);

    // the previous record
    DenormClassification prev = null;
    Iterator<DenormClassification> iter = denorm.iterator();
    while (iter.hasNext()) {
      DenormClassification curr = iter.next();
      if (prev != null && equalClassifications(curr, prev)) {
        LOG.debug("Merging duplicate classification after inference: {}", curr);

        mergeInto(curr, prev);
        iter.remove(); // and keep previous as is, since we merged this one into previous

      } else {
        prev = curr;
      }
    }
    if (LOG.isDebugEnabled()) {
      LOG.debug("Finished merging classifications[{}]", denorm.size());
      debug(denorm);
    }
  }

  /**
   * Merges the source data into the target object.
   *
   * @param source To merge from
   * @param target To merge into
   */
  public static void mergeInto(DenormClassification source, DenormClassification target) {
    LOG.debug("Adding payloads from [{}] into [{}]", source.toString(), target.toString());
    target.getPayloads().addAll(source.getPayloads());
  }

  /**
   * Does a full classification comparison.
   *
   * @return A new instance of a comparator
   */
  protected static Comparator<DenormClassification> newFullComparator() {
    ComparatorChain cc = new ComparatorChain();
    NullAwareStringComparator nasc = new NullAwareStringComparator();
    cc.addComparator(new BeanComparator("kingdom", nasc));
    cc.addComparator(new BeanComparator("phylum", nasc));
    cc.addComparator(new BeanComparator("klass", nasc));
    cc.addComparator(new BeanComparator("order", nasc));
    cc.addComparator(new BeanComparator("family", nasc));
    cc.addComparator(new BeanComparator("genus", nasc));
    cc.addComparator(new BeanComparator("species", nasc));
    cc.addComparator(new BeanComparator("subspecies", nasc));
    cc.addComparator(new BeanComparator("author", nasc));
    return cc;
  }

  /**
   * Gets a comparator for the specified linnean rank only.
   *
   * @return A new instance of a comparator
   */
  protected static Comparator<DenormClassification> newRankComparator(LINNEAN_RANK rank) {

    NullAwareStringComparator nasc = new NullAwareStringComparator();
    switch (rank) {
      case K:
        return new BeanComparator("kingdom", nasc);
      case P:
        return new BeanComparator("phylum", nasc);
      case C:
        return new BeanComparator("klass", nasc);
      case O:
        return new BeanComparator("order", nasc);
      case F:
        return new BeanComparator("family", nasc);
      case G:
        return new BeanComparator("genus", nasc);
      case S:
        ComparatorChain cc = new ComparatorChain();
        cc.addComparator(new BeanComparator("species", nasc));
        cc.addComparator(new BeanComparator("author", nasc));
        return cc;
      default:
        ComparatorChain cc2 = new ComparatorChain();
        cc2.addComparator(new BeanComparator("subspecies", nasc));
        cc2.addComparator(new BeanComparator("author", nasc));
        return cc2;
    }
  }

  /**
   * @param rank At which comparison should <b>ONLY</b> take place
   *
   * @return a comparator for the given rank
   */
  public static Comparator<DenormClassification> rankComparator(LINNEAN_RANK rank) {
    switch (rank) {
      case K:
        return DenormClassificationUtils.KINGDOM_COMPARATOR;
      case P:
        return DenormClassificationUtils.PHYLUM_COMPARATOR;
      case C:
        return DenormClassificationUtils.CLASS_COMPARATOR;
      case O:
        return DenormClassificationUtils.ORDER_COMPARATOR;
      case F:
        return DenormClassificationUtils.FAMILY_COMPARATOR;
      case G:
        return DenormClassificationUtils.GENUS_COMPARATOR;
      case S:
        return DenormClassificationUtils.SPECIES_COMPARATOR;
      default:
        return DenormClassificationUtils.SUBSPECIES_COMPARATOR;
    }
  }

  /**
   * Takes 2 classifications and returns the rank at which they differ.
   * Consider:
   * source: k,p,c,o,f,g,s,ss
   * target: k,p,c,o,-,-,S,--
   * <p/>
   * Will return the higher rank of deviation, which is Family.
   * <p/>
   * Note that Null is treated as a deviation.
   * <p/>
   * Note also that special handling occurs at species and subspecies level:
   * source:-,-,-,-,-,Aus,Aus bus,-,L. 1771
   * target:-,-,-,-,-,Aus,Aus bus,Aus bus cus,L. 1771
   * Are said to differ at subspecies, as the authorship is only compared at equivalent ranks.
   * The target record is at rank subspecies, and therefore the species level comparison, does
   * not include authorship comparison. This is depicted in the state machine:
   * <p/>
   * s1 s2 s3 s4
   * n  n  n  n  - null
   * n  n  n  y  - return subspecies
   * n  n  y  n  - return subspecies
   * n  n  y  y  - return subspecies
   * <p/>
   * comparison with authorship
   * n  y  n  n  - return species
   * n  y  n  y  - return species
   * n  y  y  n  - return species
   * n  y  y  y  - return species
   * y  n  n  n  - return species
   * y  n  n  y  - return species
   * y  n  y  n  - return species
   * y  n  y  y  - return species
   * y  y  n  n  - return species
   * <p/>
   * comparison with author
   * y  y  n  y  - return species
   * <p/>
   * comparison without author, if same, subspecies
   * y  y  y  n  - return species
   * <p/>
   * comparison without author, if same, subspecies
   * y  y  y  y  - return species
   * <p/>
   * comparison without author, if same, subspecies comparison
   *
   * @param source Of the comparison
   * @param target Of the comparison
   *
   * @return The rank of deviation or null if they are the same
   */
  public static LINNEAN_RANK rankOfDeviation(DenormClassification source, DenormClassification target) {
    LINNEAN_RANK deviation = null;
    for (LINNEAN_RANK r : LinneanRank.ranksLowerThan(LINNEAN_RANK.K, true)) {
      String s = source.get(r);
      String t = target.get(r);
      // Kingdom to genera is simple
      if (LinneanRank.isHigherOrEqual(r, LINNEAN_RANK.G)) {
        if (!StringUtils.equals(s, t)) {
          deviation = r;
        }

        // if they are the same but everything below each is null and there is an author
        // then they differ at the rank
        //   Animalia,-,-,-,Felidae,-,-,-,-
        //   Animalia,-,-,-,Felidae,-,-,-,L. 1771
        if (StringUtils.equals(s, t)) {

          boolean areNull = true;
          for (LINNEAN_RANK r1 : LinneanRank.ranksLowerThan(r, false)) {
            if (source.get(r1) != null || target.get(r1) != null) {
              areNull = false;
            }
          }

          if (areNull && !StringUtils.equals(source.getAuthor(), target.getAuthor())) {
            // everything below this rank is null, and the author differs.
            // this rank therefore differs
            deviation = r;
          }
        }

      } else if (r == LINNEAN_RANK.S || r == LINNEAN_RANK.SS) {
        // a small FSM based on the state of values in species and subspecies
        int state =
          (StringUtils.isBlank(source.getSpecies()) ? 0 : 0x8) | (StringUtils.isBlank(target.getSpecies()) ? 0 : 0x4) |
            (StringUtils.isBlank(source.getSubspecies()) ? 0 : 0x2) |
            (StringUtils.isBlank(target.getSubspecies()) ? 0 : 0x1);

        switch (state) {
          case 0:
            LOG.debug("Handling state 0: {}", state);
            break;  // no values in anything

          case 0x1: // both sparse in species, differing in subspecies
          case 0x2:
            LOG.debug("Handling state 1-2: {}", state);
            deviation = LINNEAN_RANK.SS;
            break;

          case 0x3: // both sparse in species, both have subspecies
            LOG.debug("Handling state 3: {}", state);
            if (!StringUtils.equals(source.getSubspecies(), target.getSubspecies()) || !StringUtils
              .equals(source.getAuthor(), target.getAuthor())) {
              deviation = LINNEAN_RANK.SS;
            }
            break;

          case 0x4:   // one with value in species
          case 0x5:
          case 0x6:
          case 0x7:
          case 0x8:
          case 0x9:
          case 0xA:
          case 0xB:
            LOG.debug("Handling state 4-11: {}", state);
            deviation = LINNEAN_RANK.S;
            break;

          case 0xC: // both have a species only
            LOG.debug("Handling state 12: {}", state);
            if (!StringUtils.equals(source.getSpecies(), target.getSpecies()) || !StringUtils
              .equals(source.getAuthor(), target.getAuthor())) {
              deviation = LINNEAN_RANK.S;
            }
            break;

          case 0xD: // both with species, one with subspecies
          case 0xE:
            LOG.debug("Handling state 13");
            // authorship is ignored, as the author on one refers to subspecies
            deviation = StringUtils.equals(source.getSpecies(), target.getSpecies()) ? LINNEAN_RANK.SS : LINNEAN_RANK.S;
            break;

          case 0xF: // both have species and subspecies
            // authorship is ignored, as the author refers to subspecific author
            LOG.debug("Handling state 15");
            if (StringUtils.equals(source.getSpecies(), target.getSpecies())) {
              if (StringUtils.equals(source.getSubspecies(), target.getSubspecies()) && StringUtils
                .equals(source.getAuthor(), target.getAuthor())) {
                // they are the same
              } else {
                deviation = LINNEAN_RANK.SS;
              }
            } else { // differ in canonical species
              deviation = LINNEAN_RANK.S;
            }
            break;

          default:
            LOG.info("Handling unknown state");
            break;
        }
      }

      // don't continue when we know the deviation point
      if (deviation != null) {
        break;
      }
    }
    return deviation;
  }

  /**
   * Utility method to determine if the source shares the same higher taxonomy up to and EXCLUDING the rank provided.
   *
   * @param source To compare with the target
   * @param target To compare with the source
   * @param rank   To compare to, exclusive (e.g. pass GENUS and it will compare to FAMILY only)
   *
   * @return true if the higher taxonomy is identical.  NULLs must match in this comparison
   */
  public static boolean shareHigherTaxonomy(DenormClassification source, DenormClassification target,
    LINNEAN_RANK rank) {
    for (LINNEAN_RANK r : LinneanRank.ranksHigherThan(rank, false)) {
      String v1 = source.get(r);
      String v2 = target.get(r);
      LOG.debug("Comparing rank[{}] with value1[{}] and value2[]", new Object[] {r, v1, v2});
      if (!StringUtils.equals(v1, v2)) {
        return false; // there is a clash
      }
    }
    return true;
  }

  /**
   * Rank aware sorting.
   *
   * @param sortRank To sort by
   * @param denorm   To sort
   */
  public static void sort(final LINNEAN_RANK sortRank, List<DenormClassification> denorm) {
    LOG.debug("Sorting classifications[{}] at rank[{}]", denorm.size(), sortRank);
    Collections.sort(denorm, rankComparator(sortRank));
    LOG.debug("Sorted classifications[{}] at rank[{}]", denorm.size(), sortRank);
  }

  private DenormClassificationUtils() {
    throw new UnsupportedOperationException("Can't initialize class");
  }

}
