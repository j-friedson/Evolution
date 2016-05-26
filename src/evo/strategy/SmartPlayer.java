package evo.strategy;

import com.google.common.collect.Sets;
import evo.Constants;
import evo.game.*;
import evo.game.choose.*;
import evo.game.feed.*;
import evo.game.list.CardList;
import evo.game.list.SpeciesList;

import java.util.*;

/**
 * Created by jackfriedson on 4/28/16.
 */
public class SmartPlayer extends SillyPlayer {
  private String name;
  private int wh;

  private CardList sortedHand;
  private SpeciesList sortedBoards;

  public SmartPlayer() {
    name = "smart-ish";
  }

  public SmartPlayer(String name) { this.name = name; }

  @Override
  public void start(int wh, BasePlayer myState) { update(wh, myState); }

  @Override
  public Action4 choose(List<SpeciesList> playersBefore, List<SpeciesList> playersAfter) {
    SortedSet<Integer> unusedCards = new TreeSet<>();
    for (int i = 0; i < sortedHand.size(); i++)
      if (!unusedCards.add(i)) throw new IllegalStateException(i + " could not be added");

    Comparator<BasePlayer> measure = new MostLikeWinnersStrategy();
    int food = unusedCards.first();
    Action4 response = new Action4(food);
    unusedCards.remove(food);

    while (!unusedCards.isEmpty())
      bestCardAction(unusedCards.last(), unusedCards, measure, response).addToAction4(response);

    return response;
  }

  private CardAction bestCardAction(Integer cardIdx, SortedSet<Integer> unusedCards,
                                    Comparator<BasePlayer> measure, Action4 response) {

    CardActionComparator comp = new CardActionComparator(super.copy(), measure);
    List<CardAction> bestOfEach = new ArrayList<>();
    bestOfEach.add(bestGrowPop(cardIdx, comp));
    bestOfEach.add(bestGrowBody(cardIdx, comp));
    bestOfEach.add(bestBoardTrade(cardIdx, unusedCards, comp));

    TraitName tn = idxToCard(cardIdx).getName();
    if (canReplaceTraits(tn)) {
      ReplaceTrait rt = bestReplaceTrait(cardIdx, comp);
      if (!response.hasReplaceTrait(rt.getSpeciesIdx(), tn, hand.makeCopy()))
        bestOfEach.add(rt);
    }

    bestOfEach.sort(comp);
    CardAction best = bestOfEach.get(0);
    best.removeCardsFrom(unusedCards);
    return best;
  }

  private boolean canReplaceTraits(TraitName tn) {
    return !boards.filterBy(sb -> !sb.getTraitCards().isEmpty() &&
            sb.getTraitCards().containsTrait(tn) == -1).isEmpty();
  }

  private GrowPop bestGrowPop(Integer cardIdx, Comparator<CardAction> comp) {
    GrowPop result = new GrowPop(cardIdx, 0);
    for (int i = 1; i < boards.size(); i++) {
      GrowPop newGP = new GrowPop(cardIdx, i);
      if (comp.compare(newGP, result) < 0)
        result = newGP;
    }
    return result;
  }

  private GrowBody bestGrowBody(Integer cardIdx, Comparator<CardAction> comp) {
    GrowBody result = new GrowBody(cardIdx, 0);
    for (int i = 1; i < boards.size(); i++) {
      GrowBody newGB = new GrowBody(cardIdx, i);
      if (comp.compare(newGB, result) < 0)
        result = newGB;
    }
    return result;
  }

  private BoardTrade bestBoardTrade(Integer cardIdx, Set<Integer> unusedCards,
                                    Comparator<CardAction> comp) {

    BoardTrade result = new BoardTrade(cardIdx);

    for (Set<Integer> s : Sets.powerSet(unusedCards)) {
      if (!s.contains(cardIdx) && s.size() < Constants.MAX_TRAITS_PER_BOARD) {
        List<Integer> traits = new ArrayList<>();
        traits.addAll(s);
        BoardTrade bt = new BoardTrade(cardIdx, traits);
        if (comp.compare(bt, result) < 0)
          result = bt;
      }
    }
    return result;
  }

  private ReplaceTrait bestReplaceTrait(Integer cardIdx, Comparator<CardAction> comp) {
    Optional<ReplaceTrait> optResult = Optional.empty();
    TraitName tn = idxToCard(cardIdx).getName();

    for (int i = 0; i < boards.size(); i++) {
      CardList traits = idxToBoard(i).getTraitCards();
      if (!traits.isEmpty() && traits.containsTrait(tn) == -1) {
        ReplaceTrait rt = new ReplaceTrait(cardIdx, 0, i);
        for (int j = 1; j < traits.size(); j++) {
          ReplaceTrait rt2 = new ReplaceTrait(cardIdx, j, i);
          if (comp.compare(rt2, rt) < 0)
            rt = rt2;
        }
        if (!optResult.isPresent()
                || (optResult.isPresent() && comp.compare(rt, optResult.get()) < 0))
          optResult = Optional.of(rt);
      }
    }
    if (optResult.isPresent()) return optResult.get();
    else throw new IllegalStateException();
  }

  @Override
  public FeedResponse feedNext(BasePlayer myState, List<SpeciesList> otherBoards, int tokens) {
    update(tokens, myState);

    List<Integer> carnIdxs = Utils.sortedIdxs(getBoards(), sb -> sb.canEatMore() &&
            sb.canAttackAny(otherBoards), StrongestFirst::toIdxComp);

    int i = 0;
    while (!carnIdxs.isEmpty()) {
      int attackerIdx = carnIdxs.get(i++);
      IdxPair victimPair = Utils.getTargetAttackable(getBoards().get(attackerIdx), otherBoards,
              new StrongestFirst());
      SpeciesBoard victim = victimPair.getSpecies(otherBoards);
      SpeciesBoard attacker = myState.idxToBoard(attackerIdx);
      if (victim.hasTrait(TraitName.HORNS) && attacker.getPopulationSize() < 2) continue;
      return new FeedCarn(attackerIdx, victimPair.getPlayerIdx(), victimPair.getSpeciesIdx());
    }

    List<Integer> vegIdxs = Utils.sortedIdxs(getBoards(), sb -> sb.canEatMore() && sb.isVeg(),
            StrongestFirst::toIdxComp);
    if (!vegIdxs.isEmpty())
      return new FeedVeg(vegIdxs.get(0));

    List<Integer> fatIdxs = Utils.sortedIdxs(getBoards(), SpeciesBoard::canStoreMore,
            StrongestFirst::toIdxComp);
    if (!fatIdxs.isEmpty())
      return new FeedFat(fatIdxs.get(0), getBoards().get(fatIdxs.get(0)).getFatNeed());

    return new FeedNothing();
  }

  private void update(int wh, BasePlayer newState) {
    this.wh = wh;
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
  public String getName() {
    return name;
  }
}
