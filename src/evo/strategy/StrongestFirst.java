package evo.strategy;

import evo.game.SpeciesBoard;
import evo.game.list.SpeciesList;
import evo.game.TraitCard;

import java.util.Comparator;

/**
 * Created by jackfriedson on 4/24/16.
 */
public class StrongestFirst implements Comparator<SpeciesBoard> {

  @Override
  public int compare(SpeciesBoard o1, SpeciesBoard o2) {
    GameData data = GameData.readDataFile();
    int sum1 = o1.getPopulationSize();
    int sum2 = o2.getPopulationSize();

    for (TraitCard tc : o1.getTraitCards()) sum1 += data.getTraitValue(tc);
    for (TraitCard tc : o2.getTraitCards()) sum2 += data.getTraitValue(tc);
    data.saveToFile();

    return sum1 - sum2;
  }

  public static Comparator<Integer> toIdxComp(SpeciesList boards) {
    return new StrongestFirstIdx(boards);
  }

  private static class StrongestFirstIdx implements Comparator<Integer> {
    private SpeciesList list;

    private StrongestFirstIdx(SpeciesList list) {
      this.list = list;
    }

    @Override
    public int compare(Integer o1, Integer o2) {
      return new StrongestFirst().compare(list.get(o1), list.get(o2));
    }
  }

}
