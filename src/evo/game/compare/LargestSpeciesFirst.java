package evo.game.compare;

import evo.game.SpeciesBoard;
import evo.game.list.SpeciesList;

import java.util.Comparator;

/**
 * Created by chris on 2/16/16.
 */
public class LargestSpeciesFirst implements Comparator<SpeciesBoard> {

  @Override
  public int compare(SpeciesBoard o1, SpeciesBoard o2) {

    int popDiff;
    int foodDiff;

    if ((popDiff = o2.getPopulationSize() - o1.getPopulationSize()) != 0)
      return popDiff;
    else if ((foodDiff = o2.getFoodSupply() - o1.getFoodSupply()) != 0)
      return foodDiff;
    else return o2.getBodySize() - o1.getBodySize();
  }

  public static Comparator<Integer> toIdxComp(SpeciesList boards) {
    return new LargestSpeciesIdxFirst(boards);
  }

  private static class LargestSpeciesIdxFirst implements Comparator<Integer> {
    private final SpeciesList list;

    private LargestSpeciesIdxFirst(SpeciesList list) {
      this.list = list;
    }

    @Override
    public int compare(Integer o1, Integer o2) {
      return new LargestSpeciesFirst().compare(list.get(o1), list.get(o2));
    }
  }
}
