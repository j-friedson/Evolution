package evo.strategy;

import evo.Constants;
import evo.game.*;
import evo.game.choose.*;
import evo.game.compare.LargestSpeciesFirst;
import evo.game.feed.*;
import evo.game.list.SpeciesList;

import java.util.*;

/**
 * Created by jackfriedson on 4/24/16.
 */
public class RandomPlayer extends SillyPlayer {
  private static final int GP_CODE = 0;
  private static final int GB_CODE = 1;
  private static final int BT_CODE = 2;
  private static final int RT_CODE = 3;

  private final String name;
  private final Random random;

  public RandomPlayer() {
    this.name = "random";
    this.random = new Random();
  }

  @Override
  public void start(int wh, BasePlayer myState) {
    update(myState);
  }

  @Override
  public Action4 choose(List<SpeciesList> playersBefore, List<SpeciesList> playersAfter) {

    Set<Integer> usedCards = new HashSet<>();
    Action4 result = new Action4(randomUnusedCard(usedCards));
    int maxChoice = (canReplaceTraits() ? RT_CODE : BT_CODE) + 1;
    int numBoards = getBoards().size();
    int cardIdx, speciesIdx;

    while (usedCards.size() < getHand().size()) {
      switch (randomLessThan(maxChoice)) {
        case GP_CODE:
          cardIdx = randomUnusedCard(usedCards);
          speciesIdx = randomLessThan(numBoards);
          result.addGrowPop(new GrowPop(cardIdx, speciesIdx));
          break;
        case GB_CODE:
          cardIdx = randomUnusedCard(usedCards);
          speciesIdx = randomLessThan(numBoards);
          result.addGrowBody(new GrowBody(cardIdx, speciesIdx));
          break;
        case BT_CODE:
          List<Integer> traitIdxs = new ArrayList<>();
          cardIdx = randomUnusedCard(usedCards);
          int numTraitsToAdd = randomLessThan(Constants.MAX_TRAITS_PER_BOARD + 1);

          int i = 0;
          while (usedCards.size() < getHand().size() && i++ < numTraitsToAdd)
            traitIdxs.add(randomUnusedCard(usedCards));

          result.addBoardTrade(new BoardTrade(cardIdx, traitIdxs));
          numBoards++;
          break;
        case RT_CODE:
          // Note: Random player does not replace traits on new board trades, only on existing boards
          cardIdx = randomUnusedCard(usedCards);
          TraitName newTrait = getHand().get(cardIdx).getName();
          SpeciesList speciesWithTraits = getBoards().filterBy(s -> !s.getTraitCards().isEmpty());
          boolean replaced = false;

          for (SpeciesBoard s : speciesWithTraits) {
            speciesIdx = getBoards().indexOf(s);
            if (!s.hasTrait(newTrait) && !result.hasReplaceTrait(speciesIdx, newTrait, getHand())) {
              int toReplaceIdx = randomLessThan(s.getTraitCards().size());
              result.addReplaceTrait(new ReplaceTrait(cardIdx, toReplaceIdx, speciesIdx));
              replaced = true;
              break;
            }
          }

          if (!replaced) usedCards.remove(cardIdx);
          break;
        default:
          throw new RuntimeException("Illegal choose number");
      }
    }

    return result;
  }

  /**
   * Determines whether or not this player can choose to replace traits on his existing boards, i.e.
   * whether or not he has any existing boards with greater than 0 traits
   *
   * @return true if this player can replace traits on one or more of his boards
   */
  private boolean canReplaceTraits() {
    for (SpeciesBoard sb : getBoards())
      if (!sb.getTraitCards().isEmpty()) return true;
    return false;
  }

  /**
   * Pseudorandomly gets the index of a card in this player's hand that has not already been
   * used for a card-action
   *
   * @param usedCards the set of card indices that have already been used
   * @return the index of an unused card
   */
  private int randomUnusedCard(Set<Integer> usedCards) {
    List<Integer> unused = unusedCards(usedCards);
    int result = unused.get(randomLessThan(unused.size()));
    usedCards.add(result);
    return result;
  }

  private List<Integer> unusedCards(Set<Integer> usedCards) {
    List<Integer> result = new ArrayList<>();
    for (int i = 0; i < getHand().size(); i++)
      if (!usedCards.contains(i)) result.add(i);
    return result;
  }

  /**
   * Returns a positive pseudorandom integer on the interval [0, max)
   *
   * @param max the exclusive maximum
   * @return a pseudorandom integer
   */
  private int randomLessThan(int max) {
    if (max == 0) throw new IllegalArgumentException("Max must be greater than 0");
    return Math.abs(random.nextInt() % max);
  }

  @Override
  public FeedResponse feedNext(BasePlayer myState, List<SpeciesList> otherBoards, int tokens) {
    update(myState);
    SpeciesList hungries = getBoards().filterBy(sb -> sb.canEatMore() || sb.canStoreMore());
    SpeciesBoard toFeed = hungries.get(randomLessThan(hungries.size()));

    if (toFeed.canEatMore() && toFeed.isCarn() && toFeed.canAttackAny(otherBoards)) {
      IdxPair idxPair = Utils.getTargetAttackable(toFeed, otherBoards, new LargestSpeciesFirst());
      return new FeedCarn(getBoards().indexOf(toFeed), idxPair.getPlayerIdx(), idxPair.getSpeciesIdx());
    }
    else if (toFeed.canEatMore() && toFeed.isVeg()) return new FeedVeg(getBoards().indexOf(toFeed));
    else if (toFeed.canStoreMore()) return new FeedFat(getBoards().indexOf(toFeed), toFeed.getFatNeed());
    else return new FeedNothing();
  }

  public void update(BasePlayer newState) {
    this.boards.update(newState.getBoards());
    this.hand.update(newState.getHand());
    this.bag = newState.getBag();
  }

  @Override
  public String getName() { return name; }
}
