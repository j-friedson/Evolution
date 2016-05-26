package evo.game;

import evo.game.choose.*;
import evo.game.compare.LargestFatNeedFirst;
import evo.game.compare.LargestSpeciesFirst;
import evo.game.compare.TraitCardLexOrder;
import evo.game.feed.*;
import evo.game.list.CardList;
import evo.game.list.SpeciesList;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


public class SillyPlayer extends BasePlayer implements ExternalPlayer {
  private final String name;

  public SillyPlayer() {
    super();
    this.name = "silly";
  }

  public SillyPlayer(String name) {
    super();
    this.name = Objects.requireNonNull(name);
  }

  @Override
  public void gameOver() { }

  public void update(BasePlayer newState) {
    this.boards.update(newState.getBoards());
    this.hand.update(newState.getHand());
    this.bag = newState.getBag();
  }

  @Override
  public void start(int wh, BasePlayer myState) {
    update(myState);
  }

  @Override
  public Action4 choose(List<SpeciesList> playersBefore, List<SpeciesList> playersAfter) {
    Action4 result = new Action4(handIdx(0));
    List<Integer> traitIdxs = new ArrayList<>();
    traitIdxs.add(handIdx(2));
    result.addBoardTrade(new BoardTrade(handIdx(1), traitIdxs));
    result.addGrowPop(new GrowPop(handIdx(3), getBoards().size()));

    if (getHand().size() > 4)
      result.addGrowBody(new GrowBody(handIdx(4), getBoards().size()));
    if (getHand().size() > 5)
      result.addReplaceTrait(new ReplaceTrait(handIdx(5), 0, getBoards().size()));

    return result;
  }

  private int handIdx(int sortedIdx) {
    CardList sorted = getHand().makeCopy();
    sorted.sort(new TraitCardLexOrder());
    return getHand().indexOf(sorted.get(sortedIdx));
  }

  @Override
  public FeedResponse feedNext(BasePlayer myState, List<SpeciesList> otherBoards, int tokens) {
    update(myState);
    SpeciesList myBoards = myState.getBoards();

    List<Integer> fatIdxs = Utils.sortedIdxs(myBoards, SpeciesBoard::canStoreMore,
            LargestFatNeedFirst::toIdxComp);
    if (!fatIdxs.isEmpty()) {
      int toStoreIdx = fatIdxs.get(0);
      int fatNeed = idxToBoard(fatIdxs.get(0)).getFatNeed();
      int numTokens = fatNeed > tokens ? tokens : fatNeed;
      return new FeedFat(toStoreIdx, numTokens);
    }

    List<Integer> vegIdxs = Utils.sortedIdxs(myBoards, sb -> sb.canEatMore() && sb.isVeg(),
            LargestSpeciesFirst::toIdxComp);
    if (!vegIdxs.isEmpty())
      return new FeedVeg(vegIdxs.get(0));

    List<Integer> carnIdxs = Utils.sortedIdxs(myBoards, sb -> sb.canEatMore() &&
            sb.canAttackAny(otherBoards), LargestSpeciesFirst::toIdxComp);
    if (!carnIdxs.isEmpty()) {
      int attackerIdx = carnIdxs.get(0);
      IdxPair victimPair = Utils.getTargetAttackable(idxToBoard(attackerIdx), otherBoards,
              new LargestSpeciesFirst());
      return new FeedCarn(attackerIdx, victimPair.getPlayerIdx(), victimPair.getSpeciesIdx());
    }
    return new FeedNothing();
  }

  public String getName() { return name; }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    if (!super.equals(o)) return false;

    SillyPlayer that = (SillyPlayer) o;

    return getName().equals(that.getName());

  }

  @Override
  public int hashCode() {
    int result = super.hashCode();
    result = 31 * result + getName().hashCode();
    return result;
  }

  @Override
  public String toString() {
    return "SillyPlayer{" +
            "name='" + name + '\'' +
            '}';
  }
}
