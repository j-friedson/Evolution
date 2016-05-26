package evo.game.compare;

import evo.game.PlayerScore;

import java.util.Comparator;

/**
 * Created by jackfriedson on 4/4/16.
 */
public class HighestScoreFirst implements Comparator<PlayerScore> {

  @Override
  public int compare(PlayerScore o1, PlayerScore o2) {
    return Integer.compare(o2.getScore(), o1.getScore());
  }
}
