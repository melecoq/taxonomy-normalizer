package org.gbif.taxonomy.norm;

/**
 * Utility class.
 */
public class LinneanRank {

  private static final LINNEAN_RANK[] EMPTY_LINNEAN_RANKS_ARRAY = new LINNEAN_RANK[] {};

  public enum LINNEAN_RANK {K, P, C, O, F, G, S, SS}

  /**
   * Utility method.
   *
   * @param a Source rank
   * @param b Target rank
   *
   * @return true if Source rank is for a taxon higher or the same as b, otherwise false
   */
  public static boolean isHigherOrEqual(LINNEAN_RANK a, LINNEAN_RANK b) {
    switch (a) {
      case K:
        return true;
      case P:
        return b != LINNEAN_RANK.K;
      case C:
        return b != LINNEAN_RANK.K && b != LINNEAN_RANK.P;
      case O:
        return b != LINNEAN_RANK.K && b != LINNEAN_RANK.P && b != LINNEAN_RANK.C;
      case F:
        return b != LINNEAN_RANK.K && b != LINNEAN_RANK.P && b != LINNEAN_RANK.C && b != LINNEAN_RANK.O;
      case G:
        return b != LINNEAN_RANK.K && b != LINNEAN_RANK.P && b != LINNEAN_RANK.C && b != LINNEAN_RANK.O &&
          b != LINNEAN_RANK.F;
      case S:
        return b != LINNEAN_RANK.K && b != LINNEAN_RANK.P && b != LINNEAN_RANK.C && b != LINNEAN_RANK.O &&
          b != LINNEAN_RANK.F && b != LINNEAN_RANK.G;
      default:  // must be subspecies
        return b != LINNEAN_RANK.K && b != LINNEAN_RANK.P && b != LINNEAN_RANK.C && b != LINNEAN_RANK.O &&
          b != LINNEAN_RANK.F && b != LINNEAN_RANK.G && b != LINNEAN_RANK.S;
    }
  }

  /**
   * @return an order (kingdom first) array of ranks
   */
  public static LINNEAN_RANK[] ranksHigherThan(LINNEAN_RANK rank, boolean inclusive) {
    if (inclusive) {
      switch (rank) {
        case K:
          return new LINNEAN_RANK[] {LINNEAN_RANK.K};
        case P:
          return new LINNEAN_RANK[] {LINNEAN_RANK.K, LINNEAN_RANK.P};
        case C:
          return new LINNEAN_RANK[] {LINNEAN_RANK.K, LINNEAN_RANK.P, LINNEAN_RANK.C};
        case O:
          return new LINNEAN_RANK[] {LINNEAN_RANK.K, LINNEAN_RANK.P, LINNEAN_RANK.C, LINNEAN_RANK.O};
        case F:
          return new LINNEAN_RANK[] {LINNEAN_RANK.K, LINNEAN_RANK.P, LINNEAN_RANK.C, LINNEAN_RANK.O, LINNEAN_RANK.F};
        case G:
          return new LINNEAN_RANK[] {LINNEAN_RANK.K, LINNEAN_RANK.P, LINNEAN_RANK.C, LINNEAN_RANK.O, LINNEAN_RANK.F,
            LINNEAN_RANK.G};
        case S:
          return new LINNEAN_RANK[] {LINNEAN_RANK.K, LINNEAN_RANK.P, LINNEAN_RANK.C, LINNEAN_RANK.O, LINNEAN_RANK.F,
            LINNEAN_RANK.G, LINNEAN_RANK.S};
        default: // must be subspecies
          return new LINNEAN_RANK[] {LINNEAN_RANK.K, LINNEAN_RANK.P, LINNEAN_RANK.C, LINNEAN_RANK.O, LINNEAN_RANK.F,
            LINNEAN_RANK.G, LINNEAN_RANK.S, LINNEAN_RANK.SS};
      }
    } else {
      switch (rank) {
        case K:
          return EMPTY_LINNEAN_RANKS_ARRAY;
        case P:
          return new LINNEAN_RANK[] {LINNEAN_RANK.K};
        case C:
          return new LINNEAN_RANK[] {LINNEAN_RANK.K, LINNEAN_RANK.P};
        case O:
          return new LINNEAN_RANK[] {LINNEAN_RANK.K, LINNEAN_RANK.P, LINNEAN_RANK.C};
        case F:
          return new LINNEAN_RANK[] {LINNEAN_RANK.K, LINNEAN_RANK.P, LINNEAN_RANK.C, LINNEAN_RANK.O};
        case G:
          return new LINNEAN_RANK[] {LINNEAN_RANK.K, LINNEAN_RANK.P, LINNEAN_RANK.C, LINNEAN_RANK.O, LINNEAN_RANK.F};
        case S:
          return new LINNEAN_RANK[] {LINNEAN_RANK.K, LINNEAN_RANK.P, LINNEAN_RANK.C, LINNEAN_RANK.O, LINNEAN_RANK.F,
            LINNEAN_RANK.G};
        default: // must be subspecies
          return new LINNEAN_RANK[] {LINNEAN_RANK.K, LINNEAN_RANK.P, LINNEAN_RANK.C, LINNEAN_RANK.O, LINNEAN_RANK.F,
            LINNEAN_RANK.G, LINNEAN_RANK.S};
      }
    }
  }


  /**
   * @return an order (kingdom first) array of ranks
   */
  public static LINNEAN_RANK[] ranksLowerThan(LINNEAN_RANK rank, boolean inclusive) {
    if (inclusive) {
      switch (rank) {
        case K:
          return new LINNEAN_RANK[] {LINNEAN_RANK.K, LINNEAN_RANK.P, LINNEAN_RANK.C, LINNEAN_RANK.O, LINNEAN_RANK.F,
            LINNEAN_RANK.G, LINNEAN_RANK.S, LINNEAN_RANK.SS};
        case P:
          return new LINNEAN_RANK[] {LINNEAN_RANK.P, LINNEAN_RANK.C, LINNEAN_RANK.O, LINNEAN_RANK.F, LINNEAN_RANK.G,
            LINNEAN_RANK.S, LINNEAN_RANK.SS};
        case C:
          return new LINNEAN_RANK[] {LINNEAN_RANK.C, LINNEAN_RANK.O, LINNEAN_RANK.F, LINNEAN_RANK.G, LINNEAN_RANK.S,
            LINNEAN_RANK.SS};
        case O:
          return new LINNEAN_RANK[] {LINNEAN_RANK.O, LINNEAN_RANK.F, LINNEAN_RANK.G, LINNEAN_RANK.S, LINNEAN_RANK.SS};
        case F:
          return new LINNEAN_RANK[] {LINNEAN_RANK.F, LINNEAN_RANK.G, LINNEAN_RANK.S, LINNEAN_RANK.SS};
        case G:
          return new LINNEAN_RANK[] {LINNEAN_RANK.G, LINNEAN_RANK.S, LINNEAN_RANK.SS};
        case S:
          return new LINNEAN_RANK[] {LINNEAN_RANK.S, LINNEAN_RANK.SS};
        default: // must be subspecies
          return new LINNEAN_RANK[] {LINNEAN_RANK.SS};
      }
    } else {
      switch (rank) {
        case K:
          return new LINNEAN_RANK[] {LINNEAN_RANK.P, LINNEAN_RANK.C, LINNEAN_RANK.O, LINNEAN_RANK.F, LINNEAN_RANK.G,
            LINNEAN_RANK.S, LINNEAN_RANK.SS};
        case P:
          return new LINNEAN_RANK[] {LINNEAN_RANK.C, LINNEAN_RANK.O, LINNEAN_RANK.F, LINNEAN_RANK.G, LINNEAN_RANK.S,
            LINNEAN_RANK.SS};
        case C:
          return new LINNEAN_RANK[] {LINNEAN_RANK.O, LINNEAN_RANK.F, LINNEAN_RANK.G, LINNEAN_RANK.S, LINNEAN_RANK.SS};
        case O:
          return new LINNEAN_RANK[] {LINNEAN_RANK.F, LINNEAN_RANK.G, LINNEAN_RANK.S, LINNEAN_RANK.SS};
        case F:
          return new LINNEAN_RANK[] {LINNEAN_RANK.G, LINNEAN_RANK.S, LINNEAN_RANK.SS};
        case G:
          return new LINNEAN_RANK[] {LINNEAN_RANK.S, LINNEAN_RANK.SS};
        case S:
          return new LINNEAN_RANK[] {LINNEAN_RANK.SS};
        default: // must be subspecies
          return EMPTY_LINNEAN_RANKS_ARRAY;
      }
    }
  }

  private LinneanRank() {
    throw new UnsupportedOperationException("Can't initialize class");
  }

}
