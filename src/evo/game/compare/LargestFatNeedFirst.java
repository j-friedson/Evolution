package evo.game.compare;


import evo.game.SpeciesBoard;
import evo.game.list.SpeciesList;

import java.util.Comparator;

/**
 * Created by jackfriedson on 3/12/16.
 */
public class LargestFatNeedFirst extends LargestSpeciesFirst {

  @Override
  public int compare(SpeciesBoard o1, SpeciesBoard o2) {
    int fatNeed1 = o1.getBodySize() - o1.getFatFood();
    int fatNeed2 = o2.getBodySize() - o2.getFatFood();

    int diff = fatNeed2 - fatNeed1;
    return diff == 0 ? super.compare(o1, o2) : diff;
  }

  public static Comparator<Integer> toIdxComp(SpeciesList boards) {
    return new LargestFatNeedIdxFirst(boards);
  }

  private static class LargestFatNeedIdxFirst implements Comparator<Integer> {
    private final SpeciesList list;

    private LargestFatNeedIdxFirst(SpeciesList list) {
      this.list = list;
    }

    @Override
    public int compare(Integer o1, Integer o2) {
      return new LargestFatNeedFirst().compare(list.get(o1), list.get(o2));
    }
  }
}
