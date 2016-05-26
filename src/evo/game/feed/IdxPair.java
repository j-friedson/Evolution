package evo.game.feed;

import evo.game.SpeciesBoard;
import evo.game.list.SpeciesList;

import java.util.List;

/**
 * Created by jackfriedson on 3/26/16.
 */
public final class IdxPair {
  private final int playerIdx;
  private final int speciesIdx;

  public IdxPair(int pIdx, int sIdx) {
    this.playerIdx = pIdx;
    this.speciesIdx = sIdx;
  }

  public SpeciesBoard getSpecies(List<SpeciesList> allBoards) {
    return allBoards.get(this.playerIdx).get(this.speciesIdx);
  }

  public int getPlayerIdx() {
    return playerIdx;
  }
  public int getSpeciesIdx() {
    return speciesIdx;
  }
}
