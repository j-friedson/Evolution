package evo.strategy;

import evo.game.BasePlayer;
import evo.game.SpeciesBoard;
import evo.game.TraitCard;

import java.util.Comparator;

/**
 * Created by jackfriedson on 4/28/16.
 */
public class MostLikeWinnersStrategy implements Comparator<BasePlayer> {

  @Override
  public int compare(BasePlayer o1, BasePlayer o2) {
    double score1, score2;

    GameData data = GameData.readDataFile();
    score1 = getScore(o1, data);
    score2 = getScore(o2, data);
    data.saveToFile();

    return Double.compare(score2, score1);
  }

  private double getScore(BasePlayer b, GameData data) {
    double result = b.getScore();

    double brdsScore = 0.0;
    for (SpeciesBoard sl : b.getBoards()) {
      double spScore = 0.0;
      for (TraitCard tc : sl.getTraitCards()) {
        spScore += data.getTraitValueDbl(tc);
      }
      spScore /= 300;
      brdsScore += spScore;
    }
    brdsScore /= data.getAvgNumBoards();
    result += brdsScore;
    return result;
  }
}
