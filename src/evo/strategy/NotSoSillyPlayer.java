package evo.strategy;

import evo.Constants;
import evo.game.*;
import evo.game.choose.Action4;
import evo.game.choose.BoardTrade;
import evo.game.choose.GrowPop;
import evo.game.choose.ReplaceTrait;
import evo.game.feed.*;
import evo.game.list.CardList;
import evo.game.list.SpeciesList;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

/**
 * Created by jackfriedson on 4/22/16.
 */
public class NotSoSillyPlayer extends SillyPlayer {
  private CardList sortedHand;
  private SpeciesList sortedBoards;
  private int currentWH;
  private final String name;

  public NotSoSillyPlayer() {
    super();
    this.currentWH = 0;
    this.name = "not-so-silly";
  }

  @Override
  public void start(int wh, BasePlayer myState) { update(wh, myState); }

  @Override
  public Action4 choose(List<SpeciesList> playersBefore, List<SpeciesList> playersAfter) {
    Set<Integer> usedIdxs = new HashSet<>();
    Action4 response = new Action4(handIdx(0));
    usedIdxs.add(handIdx(0));

    startCooperationChainIfPossible(response, usedIdxs);
    pairMatchingTraits(response, usedIdxs);

    int i = 0;
    while (usedIdxs.size() < getHand().size())
      nextCardAction(response, usedIdxs, i++);

    return response;
  }

  private void startCooperationChainIfPossible(Action4 response, Set<Integer> usedIdxs) {
    int coopIdx = getHand().containsTrait(TraitName.COOPERATION);
    if (coopIdx != -1 && !usedIdxs.contains(coopIdx)) {
      int newCoopIdx = getBoards().getFirstWith(sb -> !sb.hasTrait(TraitName.COOPERATION) &&
              sb.getTraitCards().size() > 0);
      if (newCoopIdx != -1) {
        SpeciesBoard newCoop = getBoards().get(newCoopIdx);
        response.addReplaceTrait(new ReplaceTrait(coopIdx, newCoop.worstTraitIdx(), newCoopIdx));
        usedIdxs.add(coopIdx);
      }
    }
  }

  private void giveTraitToSpeciesWith(Action4 response, Set<Integer> usedIdxs, TraitName toGive,
                                      TraitName has) {
    int toGiveIdx = getHand().containsTrait(toGive);
    if (toGiveIdx != -1 && !usedIdxs.contains(toGiveIdx)) {
      int hasIdx = getBoards().getFirstWith(sb -> sb.hasTrait(has) && !sb.hasTrait(toGive) &&
              sb.getTraitCards().size() > 1);
      if (hasIdx != -1) {
        SpeciesBoard coop = getBoards().get(hasIdx);
        response.addReplaceTrait(new ReplaceTrait(toGiveIdx, coop.worstTraitIdxButNot(has), hasIdx));
        usedIdxs.add(toGiveIdx);
      }
    }
  }

  private void pairMatchingTraits(Action4 response, Set<Integer> usedIdxs) {
    giveTraitToSpeciesWith(response, usedIdxs, TraitName.LONG_NECK, TraitName.COOPERATION);
    giveTraitToSpeciesWith(response, usedIdxs, TraitName.FORAGING, TraitName.COOPERATION);
    giveTraitToSpeciesWith(response, usedIdxs, TraitName.SCAVENGER, TraitName.COOPERATION);
    giveTraitToSpeciesWith(response, usedIdxs, TraitName.LONG_NECK, TraitName.FORAGING);
    giveTraitToSpeciesWith(response, usedIdxs, TraitName.FORAGING, TraitName.LONG_NECK);
    giveTraitToSpeciesWith(response, usedIdxs, TraitName.SCAVENGER, TraitName.FORAGING);
    giveTraitToSpeciesWith(response, usedIdxs, TraitName.FORAGING, TraitName.SCAVENGER);
    giveTraitToSpeciesWith(response, usedIdxs, TraitName.AMBUSH, TraitName.CARNIVORE);
    giveTraitToSpeciesWith(response, usedIdxs, TraitName.CARNIVORE, TraitName.AMBUSH);
    giveTraitToSpeciesWith(response, usedIdxs, TraitName.FERTILE, TraitName.FORAGING);
  }

  private int handIdx(int sortedIdx) {
    return getHand().indexOf(sortedHand.get(sortedIdx));
  }

  private int boardsIdx(int sortedIdx) {
    return getBoards().indexOf(sortedBoards.get(sortedIdx));
  }

  private void nextCardAction(Action4 response, Set<Integer> usedIdxs, int i) {
    switch (i) {
      case 0:
        addNewBoard(response, usedIdxs);
        break;
      case 1:
        increasePopOf(response, usedIdxs, s -> s.hasTrait(TraitName.COOPERATION));
        break;
      default:
        increaseStrongestPop(response, usedIdxs);
        break;
    }
  }

  private void increasePopOf(Action4 response, Set<Integer> usedIdxs, Predicate<SpeciesBoard> pred) {
    int brdIdx = getBoards().getFirstWith(pred);
    if (brdIdx != -1) {
      response.addGrowPop(new GrowPop(idxFromLeft(usedIdxs), brdIdx));
    }
  }

  private void increaseForagingPop(Action4 response, Set<Integer> usedIdxs) {
    increasePopOf(response, usedIdxs, s -> s.hasTrait(TraitName.FORAGING) &&
            s.getPopulationSize() < Constants.MAX_POP_SIZE);
  }

  private void addNewBoard(Action4 response, Set<Integer> usedIdxs) {
    List<Integer> traits = new ArrayList<>();
    int tradeIdx = idxFromLeft(usedIdxs);
    int j = 0;
    while (usedIdxs.size() < getHand().size() && j < 3) {
      traits.add(idxFromRight(usedIdxs));
      j++;
    }
    response.addBoardTrade(new BoardTrade(tradeIdx, traits));
  }

  private void increaseStrongestPop(Action4 response, Set<Integer> usedIdxs) {
    int strongestIdx = boardsIdx(0);
    response.addGrowPop(new GrowPop(idxFromLeft(usedIdxs), strongestIdx));
  }

  private int idxFromLeft(Set<Integer> usedIdxs) {
    for (int i = 0; i < sortedHand.size(); i++) {
      int idx = handIdx(i);
      if (!usedIdxs.contains(idx)) {
        usedIdxs.add(idx);
        return idx;
      }
    }
    throw new IllegalStateException("No unused indices");
  }

  private int idxFromRight(Set<Integer> usedIdxs) {
    for (int i = sortedHand.size() - 1; i >= 0; i--) {
      int idx = handIdx(i);
      if (!usedIdxs.contains(idx)) {
        usedIdxs.add(idx);
        return idx;
      }
    }
    throw new IllegalStateException("No unused indices");
  }


  @Override
  public FeedResponse feedNext(BasePlayer myState, List<SpeciesList> otherBoards, int tokens) {
    update(tokens, myState);

    List<Integer> coopIdxs = Utils.sortedIdxs(getBoards(), sb -> sb.canEatMore() &&
            sb.hasTrait(TraitName.COOPERATION), StrongestFirst::toIdxComp);
    if (!coopIdxs.isEmpty()) {
      int coopIdx = coopIdxs.get(0);
      SpeciesBoard coop = getBoards().get(coopIdx);
      if (coop.isVeg()) return new FeedVeg(coopIdx);
      if (coop.isCarn()) {
        IdxPair victimPair = Utils.getTargetAttackable(coop, otherBoards,
                new StrongestFirst());
        return new FeedCarn(coopIdx, victimPair.getPlayerIdx(), victimPair.getSpeciesIdx());
      }
    }

    List<Integer> carnIdxs = Utils.sortedIdxs(getBoards(), sb -> sb.canEatMore() &&
            sb.canAttackAny(otherBoards), StrongestFirst::toIdxComp);
    if (!carnIdxs.isEmpty()) {
      int attackerIdx = carnIdxs.get(0);
      IdxPair victimPair = Utils.getTargetAttackable(getBoards().get(attackerIdx), otherBoards,
              new StrongestFirst());
      return new FeedCarn(attackerIdx, victimPair.getPlayerIdx(), victimPair.getSpeciesIdx());
    }

    List<Integer> fatIdxs = Utils.sortedIdxs(getBoards(), SpeciesBoard::canStoreMore,
            StrongestFirst::toIdxComp);
    if (!fatIdxs.isEmpty())
      return new FeedFat(fatIdxs.get(0), getBoards().get(fatIdxs.get(0)).getFatNeed());

    List<Integer> vegIdxs = Utils.sortedIdxs(getBoards(), sb -> sb.canEatMore() && sb.isVeg(),
            StrongestFirst::toIdxComp);
    if (!vegIdxs.isEmpty())
      return new FeedVeg(vegIdxs.get(0));

    return new FeedNothing();
  }

  private void update(int wh, BasePlayer newState) {
    this.currentWH = wh;
    this.bag = newState.getBag();
    this.boards.update(newState.getBoards());
    this.hand.update(newState.getHand());
    this.sortedHand = getHand().makeCopy();
    this.sortedBoards = getBoards().makeCopy();
    sortedHand.sort(new WorstTraitsFirst());
    sortedBoards.sort(new StrongestFirst());
  }

  @Override
  public void gameOver() { }

  @Override
  public String getName() { return name; }
}