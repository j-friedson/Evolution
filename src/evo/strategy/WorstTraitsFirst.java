package evo.strategy;

import evo.game.list.CardList;
import evo.game.TraitCard;

import java.util.Comparator;

/**
 * Created by jackfriedson on 4/24/16.
 */
public class WorstTraitsFirst implements Comparator<TraitCard> {

  // cards at the front of the list (compare returns negative number) have the worst traits
  // and highest food values (i.e. good to put at the watering hole), cards at the end of the
  // list (compare returns positive number) have the best traits and lowest food values (i.e.
  // good to use for species traits)

  @Override
  public int compare(TraitCard o1, TraitCard o2) {
    GameData data = GameData.readDataFile();
    int cardVal1 = data.getTraitValue(o1);
    int cardVal2 = data.getTraitValue(o2);
    data.saveToFile();

    return cardVal2 - cardVal1;
  }

  public static Comparator<Integer> toIdxComp(CardList cards) {
    return new WorstTraitsFirstIdx(cards);
  }

  private static class WorstTraitsFirstIdx implements Comparator<Integer> {
    private CardList list;

    private WorstTraitsFirstIdx(CardList list) {
      this.list = list;
    }

    @Override
    public int compare(Integer o1, Integer o2) {
      return new WorstTraitsFirst().compare(list.get(o1), list.get(o2));
    }
  }
}
