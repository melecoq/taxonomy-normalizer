package org.gbif.taxonomy.norm;

import org.gbif.taxonomy.norm.LinneanRank.LINNEAN_RANK;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An implementation of the normalizer that works as follows: - starting at the lowest rank (e.g. scientific name)
 * group
 * by the value - then for each group, start at the highest rank and attempt to fill sparse records - move down each
 * rank grouping where possible - once all ranks are merged, move on to the next group - once all groups are merged
 * move
 * up to the next rank and start again
 * <p/>
 * By starting at the lowest rank, homonyms are detected as we go, and by iterating back up to the highest rank in the
 * outer loop, we fill higher voids (e.g. records sparse at higher taxa)
 * <p/>
 * To understand the process, see the test cases which capture specific examples
 * <p/>
 * This class is not thread safe.
 */
public class Normalizer {

  private static final Logger LOG = LoggerFactory.getLogger(Normalizer.class);

  /**
   * A homonym cache is used to store known homonyms as we go to ensure no inferences are made at higher taxa in the
   * later stages of the routine.
   * 
   * @return a newly created homonym cache
   */
  public Map<LINNEAN_RANK, Set<String>> createHomonymCache() {
    Map<LINNEAN_RANK, Set<String>> homonyms = new EnumMap<LINNEAN_RANK, Set<String>>(LINNEAN_RANK.class);
    homonyms.put(LINNEAN_RANK.P, new HashSet<String>());
    homonyms.put(LINNEAN_RANK.C, new HashSet<String>());
    homonyms.put(LINNEAN_RANK.O, new HashSet<String>());
    homonyms.put(LINNEAN_RANK.F, new HashSet<String>());
    homonyms.put(LINNEAN_RANK.G, new HashSet<String>());
    homonyms.put(LINNEAN_RANK.S, new HashSet<String>());
    homonyms.put(LINNEAN_RANK.SS, new HashSet<String>());
    return homonyms;
  }

  /**
   * This will group by the rank, inspect higher taxa and attempt to merge where possible. In simple terms, grouped at
   * rank with common values d: a,-,c,d a,b,-,d -,b,-,d will merge into a,b,c,d where NO CONFLICTS exist. Introducing a
   * conflict: a,-,c,d a,b,-,d -,b,-,d e,-,-,d will produce: a,b,c,d e,-,-,d
   * 
   * @param rank The rank being operated on
   * @param denorm To merge across. Must be sorted to the rank declared before calling
   */
  public void merge(final LINNEAN_RANK rank, List<DenormClassification> denorm,
    Map<LINNEAN_RANK, Set<String>> homonyms) {
    if (LOG.isDebugEnabled()) {
      LOG.debug("Starting merging classifications[{}] at rank[{}]", denorm.size(), rank);
      DenormClassificationUtils.debug(denorm);
    }

    // holds the group of things to merge
    List<DenormClassification> workingGroup = new ArrayList<DenormClassification>();

    // the previous record
    DenormClassification prev = null;

    for (DenormClassification curr : denorm) {
      if (prev == null) { // first record
        workingGroup.add(curr);
      } else {
        if (!StringUtils.equals(curr.get(rank), prev.get(rank))) {
          // there is a change, so perform any necessary merging and copy to merged
          inferHigherTaxa(denorm, workingGroup, rank, homonyms);

          // refresh the working group
          workingGroup.clear();
        }
        workingGroup.add(curr);
      }
      prev = curr;
    }

    // ensure the last row is handled
    inferHigherTaxa(denorm, workingGroup, rank, homonyms);
  }

  /**
   * This implementation does the following: a) infers missing genera b) sorts to the scientific name c) does a homonym
   * aware merge to fill holes in a classification: a,-,c,d a,b,-,d would merge to a,b,c,d.
   */
  public List<NormClassification> normalize(List<DenormClassification> denorm) {
    List<NormClassification> result = new ArrayList<NormClassification>();

    // infer missing values
    DenormClassificationUtils.inferSpecies(denorm);
    DenormClassificationUtils.inferGenera(denorm);

    Map<LINNEAN_RANK, Set<String>> homonyms = createHomonymCache();

    // respecting homonymns, merge higher classification into as few as possible
    // a,-,c,d
    // a,b,-,d
    // would merge to a,b,c,d for example
    long time = System.currentTimeMillis();
    sortAndMerge(LINNEAN_RANK.SS, denorm, homonyms);
    sortAndMerge(LINNEAN_RANK.S, denorm, homonyms);
    sortAndMerge(LINNEAN_RANK.G, denorm, homonyms);
    sortAndMerge(LINNEAN_RANK.F, denorm, homonyms);
    sortAndMerge(LINNEAN_RANK.O, denorm, homonyms);
    sortAndMerge(LINNEAN_RANK.C, denorm, homonyms);
    sortAndMerge(LINNEAN_RANK.P, denorm, homonyms);
    sortAndMerge(LINNEAN_RANK.K, denorm, homonyms);
    LOG.info("Completed classification merging at all ranks in {} sec(s)",
      (1 + System.currentTimeMillis() - time) / 1000);

    // now resort to ensure correct ordering from the bottom up
    Collections.sort(denorm, DenormClassificationUtils.FULL_COMPARATOR);

    time = System.currentTimeMillis();
    LOG.info("Building normalized tree structure for {} classifications", denorm.size());
    int id = 1;
    Map<Integer, NormClassification> norm = new HashMap<Integer, NormClassification>();
    Map<LINNEAN_RANK, Integer> parentIds = new HashMap<LINNEAN_RANK, Integer>();
    DenormClassification prev = null;
    for (DenormClassification curr : denorm) {
      // capture first row
      boolean change = prev == null;

      // find where they differ
      LINNEAN_RANK deviation = change ? LINNEAN_RANK.K : DenormClassificationUtils.rankOfDeviation(curr, prev);
      if (LOG.isDebugEnabled()) {
        LOG.debug("Deviation with previous is at rank[{}] for curr[{}] prev[{}]", new Object[] {deviation, curr, prev});
      }

      for (LINNEAN_RANK r : LinneanRank.ranksLowerThan(deviation, true)) {
        // clear parentIds not of interest now
        parentIds.put(r, null);

        String name = curr.get(r);
        if (StringUtils.isNotBlank(name)) {
          // find the parent id to use for this taxon
          Integer parentId = null;
          for (LINNEAN_RANK p : LinneanRank.ranksHigherThan(r, false)) {
            parentId = parentIds.get(p) == null ? parentId : parentIds.get(p);
          }

          // create the taxon
          String author = null;
          if (LINNEAN_RANK.S == r && StringUtils.isBlank(curr.get(LINNEAN_RANK.SS)) || LINNEAN_RANK.SS == r) {
            author = curr.getAuthor();
          }
          NormClassification nc = new NormClassification(id, parentId, name, author, r.toString());
          norm.put(id, nc);
          parentIds.put(r, id);
          id++;

          // we have just created the concept, but if this is the most significant taxa,
          // then we need to track any payloads on the newly create concept
          boolean more = false;
          for (LINNEAN_RANK r2 : LinneanRank.ranksLowerThan(r, false)) {
            more |= StringUtils.isNotBlank(curr.get(r2));
          }
          if (!more) {
            LOG.debug("Adding payloads from [{}] into [{}]", curr.toString(), nc.toString());
            nc.getPayloads().addAll(curr.getPayloads());
          }
        }
      }

      // handle the special case when you have
      // "a",null,null,null,null,"f","g","h","i"));
      // "a",null,null,null,null,"f","g","j","i"));
      // "a",null,null,null,null,"f","g",null,"i"));
      // on the 3rd row, we have already created the species, but need to apply the author and update the payloads
      if (LINNEAN_RANK.SS == deviation && prev != null &&
        StringUtils.isBlank(curr.getSubspecies()) && // we don't want second row to go in here
        StringUtils.equals(curr.get(LINNEAN_RANK.S), prev.get(LINNEAN_RANK.S))) {
        NormClassification prevNorm = norm.get(id - 1);

        // iterate back to the species concept
        while (prevNorm != null && !StringUtils.equals(prevNorm.getRank(), LINNEAN_RANK.S.toString())) {
          if (prevNorm.getParentId() == null) {
            prevNorm = null;
            break;
          } else {
            prevNorm = norm.get(prevNorm.getParentId());
          }
        }
        LOG.debug("Previous species: " + prevNorm);
        if (prevNorm != null && StringUtils.equals(prevNorm.getRank(), LINNEAN_RANK.S.toString())) {
          LOG.debug("Updating previous species concept with new author[{}]: {}", curr.getAuthor(), prevNorm);
          prevNorm.setAuthor(curr.getAuthor());
          LOG.debug("Updating previous payloads from [{}] into previous [{}]", curr.toString(), prevNorm.toString());
          prevNorm.getPayloads().addAll(curr.getPayloads());
        }
      }

      prev = curr;
    }
    result.addAll(norm.values());
    Collections.sort(result, new Comparator<NormClassification>() {

      @Override
      public int compare(NormClassification o1, NormClassification o2) {
        return o1.getId().compareTo(o2.getId());
      }

    });
    LOG.info("Built normalized tree structure for {} classifications in {} sec(s)", denorm.size(),
      (1 + System.currentTimeMillis() - time) / 1000);

    return result;
  }

  /**
   * Utility to perform a sort() and then a merge() and then removeDuplicates().
   * 
   * @param rank To operate at
   * @param denorm To sort and merge.
   */
  public void sortAndMerge(final LINNEAN_RANK rank, List<DenormClassification> denorm,
    Map<LINNEAN_RANK, Set<String>> homonyms) {
    LOG.info("Starting taxonomy[classifications: {}] at rank[{}] with homonyms[p:{},c:{},o:{},f:{},g:{},s:{},ss:{}]",
      new Object[] {denorm.size(), rank, homonyms.get(LINNEAN_RANK.P).size(), homonyms.get(LINNEAN_RANK.C).size(),
        homonyms.get(LINNEAN_RANK.O).size(), homonyms.get(LINNEAN_RANK.F).size(), homonyms.get(LINNEAN_RANK.G).size(),
        homonyms.get(LINNEAN_RANK.S).size(), homonyms.get(LINNEAN_RANK.SS).size()});
    long time = System.currentTimeMillis();
    LOG.info("Sorting {} classifications", denorm.size());
    DenormClassificationUtils.sort(rank, denorm);
    LOG.info("Sorted {} classifications in {} sec(s)", denorm.size(), (1 + System.currentTimeMillis() - time) / 1000);

    time = System.currentTimeMillis();
    LOG.info("Inferring taxa across {} classifications", denorm.size());
    merge(rank, denorm, homonyms);
    LOG.info("Inferred taxa across {} classifications in {} sec(s)", denorm.size(),
      (1 + System.currentTimeMillis() - time) / 1000);

    time = System.currentTimeMillis();
    LOG.info("Merging duplicates in {} classifications", denorm.size());
    DenormClassificationUtils.mergeDuplicates(denorm);
    LOG.info("Merge of duplicates resulted in {} classifications in {} sec(s)", denorm.size(),
      (1 + System.currentTimeMillis() - time) / 1000);
  }

  /**
   * @param group To extract from
   * @param rank The rank at which we are working. Pass Genus and anything higher than Genus will be extracted
   * @return The distinct classifications
   */
  private Map<String, DenormClassification> distinctClassifications(List<DenormClassification> group,
    final LINNEAN_RANK rank) {
    Map<String, DenormClassification> distinctClassifications = new HashMap<String, DenormClassification>();
    for (DenormClassification d : group) {
      // build a string key to use in the distinct
      StringBuilder key = new StringBuilder();
      for (LINNEAN_RANK r : LinneanRank.ranksHigherThan(rank, false)) {
        if (StringUtils.isBlank(d.get(r))) {
          key.append("|--");
        } else {
          key.append('|').append(d.get(r));
        }
      }
      // only add a single representative sample
      if (!distinctClassifications.containsKey(key.toString())) {
        distinctClassifications.put(key.toString(), d);
      }
    }
    LOG.debug("Group of {} provided {} distinct higher classifications at rank[{}] for: {}",
      new Object[] {group.size(), distinctClassifications.size(), rank, group.get(0).get(rank)});

    return distinctClassifications;
  }

  /**
   * Checks the lower ranks for any homonyms
   * 
   * @param rank To work below
   * @param homonyms To check within
   * @param d The classification which we are concerned might have a homonym
   * @return True if a homonym is found
   */
  private boolean homonymInLowerRank(final LINNEAN_RANK rank, Map<LINNEAN_RANK, Set<String>> homonyms,
    DenormClassification d) {
    boolean homonymFound = false;
    for (LINNEAN_RANK r1 : LinneanRank.ranksLowerThan(rank, false)) {
      // if the value at the rank is in the homonym list, then there is a reason the
      // rank we are operating on has not already been put into the same group,
      // e.g. why the 3rd row in our example does not have a c
      if (homonyms.get(r1).contains(d.get(r1))) {
        LOG.debug("Homonym[{}] found at rank[{}]", d.get(r1), r1);
        homonymFound = true;
      } else {
        LOG.debug("No homonyms found at rank[{}]", r1);
      }
      if (homonymFound)
      {
        break; // no point looking for more
      }
    }
    return homonymFound;
  }

  /**
   * Inspects the group which should all have the same value at the declared rank. This will infer higher taxa for each
   * row where there are no conflicts. For each rank starting kingdom then phylum etc: a) for each that is null at that
   * rank, consider any with a non-null value at that rank as candidate for copying the rank value b) check each
   * candidate and remove it from the potential candidates if there is a conflict c) if there is 1 candidate at the end
   * use it, otherwise, it cannot be used
   * 
   * @param source To infer what is possible
   * @param rank the most significant rank being operated on (inclusive) Passing rank of genus, means you infer
   *        k,p,g,o,f and genus
   */
  private void inferHigherTaxa(List<DenormClassification> taxonomy, List<DenormClassification> group,
    final LINNEAN_RANK rank, Map<LINNEAN_RANK, Set<String>> homonyms) {

    if (group.size() <= 1) {
      LOG.debug("Nothing to merge at rank[{}] since there is/are {} classification(s)", rank, group.size());

    } else if (StringUtils.isBlank(group.get(0).get(rank))) {
      LOG.debug("Skipping merging of group since the group represents a group with null at the rank");
    } else {
      LOG.info("Merging classifications[{}] at rank[{}] for group: {}",
        new Object[] {group.size(), rank, group.get(0).get(rank)});
      if (LOG.isDebugEnabled()) {
        DenormClassificationUtils.debug(group);
      }

      // we know we will receive a lot of duplicates, so extract them for performance
      Map<String, DenormClassification> distinctClassifications = distinctClassifications(group, rank);

      // inspect from the highest rank to the working rank in order, inferring as we go
      for (LINNEAN_RANK r : LinneanRank.ranksHigherThan(rank, false)) {

        // get the "sparse" classifications (e.g. with a null at the rank in question)
        List<DenormClassification> sparseRecords = new ArrayList<DenormClassification>();
        for (DenormClassification d : group) {
          if (StringUtils.isBlank(d.get(r))) {
            sparseRecords.add(d);
          }
        }

        // don't continue if there are no sparse records
        if (sparseRecords.size() < 1) {
          if (LOG.isDebugEnabled()) {
            LOG.debug("No classification(s) is/are empty at rank[{}]", r);
            DenormClassificationUtils.debug(group);
          }

        } else {
          if (LOG.isDebugEnabled()) {
            LOG.debug(sparseRecords.size() + " classification(s) is/are empty at rank[{}]", r);
            DenormClassificationUtils.debug(group);
          }

          // a classification with a value is a candidate
          List<DenormClassification> candidates = new ArrayList<DenormClassification>();
          for (DenormClassification dc : distinctClassifications.values()) {
            if (StringUtils.isNotBlank(dc.get(r))) {
              candidates.add(dc);
            }
          }
          LOG.debug("{} classification(s) is/are potential candidate(s) from which rank[{}] might be inferred",
            candidates.size(), r);

          // for each, check against those with values at the rank
          if (!candidates.isEmpty()) {
            for (DenormClassification d : sparseRecords) {
              LOG.debug("Attempting to infer rank[{}] for: {}", r, d);

              // Check each candidate, and add it to the options
              Set<String> potentials = new HashSet<String>();
              for (DenormClassification candidate : candidates) {

                // if they conflict in higher taxonomy, remove it from the candidate list
                if (DenormClassificationUtils.haveConflict(d, candidate, r)) {
                  LOG.debug("Ignoring candidate from potential options due to a conflict: {}", candidate);

                } else if (DenormClassificationUtils.shareHigherTaxonomy(d, candidate, r)) {
                  LOG.debug("Adding option[{}] for rank[{}] from candidate: {}",
                    new Object[] {candidate.get(r), r, candidate});
                  potentials.add(candidate.get(r));
                } else {
                  // since we are doing ranks in order, the higher taxa must be identical, or
                  // we have ambiguity. Consider:
                  // a,b,c
                  // -,-,c
                  // d,-,c
                  // If we are on the middle rank and the second row, we see no conflict to
                  // a,b,c but cannot assume b
                  LOG.debug("Ignoring candidate from potential options due to ambiguity: {}", candidate);
                }
              }

              // within this rank we have now the candidates, but consider working at the 3rd column in:
              // a - c d -> this row could be "b" but not definitely because:
              // - b c d -> this row could be "e" or "a"
              // e - - d
              // we would now set the a or b happily, ignorant of the homonym at a lower rank with empty
              // 3rd column
              // To counter this, we hunt for homonyms at any lower rank with a null at the rank we are working at
              boolean homonymFound = false;
              if (StringUtils.isNotBlank(d.get(rank))) {
                // homonymFound = homonymInLowerRankScan(taxonomy,rank, d);
                homonymFound = homonymInLowerRank(rank, homonyms, d);
              }

              if (homonymFound) { // if homonyms exist, one cannot make inferences
                LOG.debug("Homonyms found, so rank[{}] cannot be inferred for: {}", r, d);
              } else if (potentials.size() == 1) { // if there is only one option, use it
                String value = potentials.iterator().next();
                LOG.debug("{} classification(s) unanimously provided option[{}] at rank[{}] for: {}",
                  new Object[] {candidates.size(), value, r, d});
                d.set(r, value);
              } else {
                LOG.debug("{} classification(s) provided {} options at rank[{}], so cannot be inferred for: {}",
                  new Object[] {candidates.size(), potentials.size(), r, d});
                // this means this group represents a homonym which should be stored for future decisions merging
                // higher taxa
                if (StringUtils.isNotBlank(d.get(rank))) {
                  LOG.debug("Adding homonym[{}] to rank[{}]", d.get(rank), rank);
                  homonyms.get(rank).add(d.get(rank));
                }
              }
            }
          }
        }
      }
    }
  }
}
