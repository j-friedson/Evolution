package evo.strategy;


import evo.game.BasePlayer;

import java.util.Comparator;

/**
 * Created by jackfriedson on 4/28/16.
 */
public class HighestScoreStrategy implements Comparator<BasePlayer> {

  @Override
  public int compare(BasePlayer o1, BasePlayer o2) {
    return Integer.compare(o2.getScore(), o1.getScore());
  }
}
