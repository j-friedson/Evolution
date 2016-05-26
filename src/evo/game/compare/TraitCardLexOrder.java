package evo.game.compare;


import evo.game.TraitCard;

import java.util.Comparator;

/**
 * Created by jackfriedson on 4/4/16.
 */
public class TraitCardLexOrder implements Comparator<TraitCard> {

  @Override
  public int compare(TraitCard o1, TraitCard o2) {
    int nameDiff = o1.getName().toString().compareTo(o2.getName().toString());
    if (nameDiff != 0)
      return nameDiff;
    else
      return Integer.compare(o1.getFoodPoints(), o2.getFoodPoints());
  }
}
