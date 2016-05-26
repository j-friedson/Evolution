package evo.game;

import com.google.gson.*;
import evo.Constants;
import evo.game.list.CardList;
import evo.game.list.PlayerList;
import evo.game.list.SpeciesList;
import evo.json.JFactory;
import evo.json.JUtils;

import java.lang.reflect.Type;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * Created by jackfriedson on 4/11/16.
 */
public class BasePlayer extends EvoElement {
  protected final SpeciesList boards;
  protected final CardList hand;
  protected int bag;

  protected BasePlayer() throws InvalidEvoElementException {
    this.boards = new SpeciesList();
    this.hand = new CardList();
    this.bag = Constants.MIN_FOOD_BAG;
  }

  protected BasePlayer(SpeciesList boards, CardList hand, int bag) throws InvalidEvoElementException {
    this.boards = Objects.requireNonNull(boards);
    this.hand = Objects.requireNonNull(hand);
    this.bag = bag;
  }

  /**
   * todo
   * @return
   */
  public int getScore() {
    int score = getBag();
    for (SpeciesBoard sb : getBoards())
      score += sb.getPopulationSize() + sb.getTraitCards().size();
    return score;
  }

  /**
   * Effect: directs the {@link SpeciesBoard} at the given index to eat as much of its stored bag
   *         as possible
   *
   * @param speciesIdx the index of the {@link SpeciesBoard} to eat stored bag
   */
  public void eatStoredFood(int speciesIdx) {
    boards.get(speciesIdx).eatStoredFood();
  }

  /**
   * Effect: adds the given {@link SpeciesBoard} to this {@link BasePlayer}'s list of boards
   *
   * @param speciesBoard the new {@link SpeciesBoard} to be added
   */
  public void addSpecies(SpeciesBoard speciesBoard) {
    this.boards.add(Objects.requireNonNull(speciesBoard));
  }

  /**
   * todo
   */
  public void addNewSpecies() {
    try { this.boards.add(SpeciesBoard.builder().build()); }
    catch (InvalidEvoElementException e) { throw new RuntimeException("Should never be thrown"); }
  }

  /**
   * Effect: add the given number of tokens to this {@link BasePlayer}'s bag bag
   *
   * @param foodToAdd the number of tokens to add
   */
  public void addFood(int foodToAdd) {
    this.bag += foodToAdd;
  }

  /**
   * todo
   * @return
   */
  public BasePlayer copy() {
    return builder().bag(bag).boards(boards.makeCopy()).hand(hand.makeCopy()).build();
  }

  /**
   * Gets the {@link SpeciesBoard} at the given index (in this {@link BasePlayer}'s list of
   * {@link SpeciesBoard}s)
   *
   * @param idx the index of the {@link SpeciesBoard} to get
   * @return the {@link SpeciesBoard} at that index
   */
  public SpeciesBoard idxToBoard(int idx) {
    if (idx < 0 || idx >= this.boards.size())
      throw new IllegalArgumentException("Species index out of range");

    return this.boards.get(idx);
  }

  /**
   * Gets the {@link TraitCard} at the given index (in this {@link BasePlayer}'s list of
   * {@link TraitCard}s)
   *
   * @param idx the index of the {@link TraitCard} to get
   * @return the {@link TraitCard} at that index
   */
  public TraitCard idxToCard(int idx) {
    if (idx < 0 || idx >= this.hand.size())
      throw new IllegalArgumentException("Card index out of range");

    return this.hand.get(idx);
  }

  /**
   * Effect: iterates through all of the {@link SpeciesBoard}s belonging to this {@link BasePlayer},
   *         passing the index of the current {@link SpeciesBoard} into the given {@link Consumer}
   *         iff the {@link SpeciesBoard} passes the {@link Predicate}
   *
   * @param pred the {@link Predicate} used to test each {@link SpeciesBoard}
   * @param consumer the {@link Consumer} to take the index of the {@link SpeciesBoard}
   */
  public void actOnAllWith(Predicate<SpeciesBoard> pred, Consumer<Integer> consumer) {
    for (int i = 0; i < boards.size(); i++) {
      if (pred.test(boards.get(i))) {
        consumer.accept(i);
      }
    }
  }

  /**
   * Gets an optional containing the  {@link SpeciesBoard} to the right of this
   * {@link SpeciesBoard} if one exists, or an empty optional if none exists
   *
   * @param speciesIdx the index of the {@link SpeciesBoard} to find the neighbor of
   * @return an {@link Optional<SpeciesBoard>} containing the neighbor (or empty if none)
   */
  public Optional<SpeciesBoard> rightNeighborOf(int speciesIdx) {
    return boards.rightNeighborOf(speciesIdx);
  }

  /**
   * Gets an optional containing the {@link SpeciesBoard} to the left of this {@link SpeciesBoard}
   * if one exists, or an empty optional if none exists
   *
   * @param speciesIdx the index of the {@link SpeciesBoard} to find the neighbor of
   * @return an {@link Optional<SpeciesBoard>} containing the neighbor (or empty if none)
   */
  public Optional<SpeciesBoard> leftNeighborOf(int speciesIdx) {
    return boards.leftNeighborOf(speciesIdx);
  }

  /**
   * Effect: Directs this {@link EvoPlayer} to perform end-of-turn actions on all of its
   *         boards. This includes decreasing boards populations to match their bag supply,
   *         removing extinct boards, and moving bag tokens to its food bag.
   */
  public void endTurn(Deck deck) {
    List<SpeciesBoard> extinctBoards = new ArrayList<>();
    int toBag = 0;

    for (int i = 0; i < boards.size(); i++) {
      SpeciesBoard s = idxToBoard(i);
      int eaten = s.endTurn();
      if (s.getPopulationSize() == 0) extinctBoards.add(s);
      else toBag += eaten;
    }

    extinctBoards.forEach(sb -> removeBoard(sb, deck));
    addFood(toBag);
  }

  /**
   * todo
   * @return
   */
  public int cardsNeeded() {
    int numBoards = boards.isEmpty() ? 1 : boards.size();
    return Constants.CARDS_PER_TURN + numBoards;
  }

  /**
   * Determines whether or not this {@link BasePlayer} has any {@link SpeciesBoard}s that can be fed
   *
   * @param otherPlayers the list of the other {@link BasePlayer}s in this game
   * @return true if this {@link BasePlayer} has feedable boards
   */
  public boolean canFeed(PlayerList otherPlayers) {
    for (SpeciesBoard s : getBoards()) {
      if (s.canStoreMore() || (s.canEatMore() && s.isVeg())
              || (s.canEatMore() && s.isCarn() && s.canAttackAny(otherPlayers.getAllBoards())))
        return true;
    }
    return false;
  }

  /**
   * Finds the index of the unique {@link SpeciesBoard} that passes the given {@link Predicate}.
   * Returns -1 if there are more than one such board, or -2 if there are 0 such boards.
   *
   * @return the index of the unique {@link SpeciesBoard} in this {@link BasePlayer}'s list of
   *         {@link SpeciesBoard}s, -1 if there are more than 1 such board, -2 if there are none
   */
  protected int findUniqueIdx(Predicate<Integer> pred) {
    Optional<Integer> optHungryIdx = Optional.empty();

    for (int i = 0; i < getBoards().size(); i++) {
      if (pred.test(i)) {
        if (optHungryIdx.isPresent()) return -1;
        else optHungryIdx = Optional.of(i);
      }
    }

    if (!optHungryIdx.isPresent()) return -2;
    else return optHungryIdx.get();
  }

  /**
   * Determines whether or not the initial feed of the {@link SpeciesBoard} at the given index
   * was successful (not including any subsequent feedings due to traits)
   *
   * Effect: directs the {@link SpeciesBoard} at the given index to eat one token, accounting for
   *         the foraging and cooperation {@link TraitCard}s if necessary
   *
   * @param toFeedIdx the index of the {@link SpeciesBoard} to be fed in this {@link BasePlayer}'s
   *                  list of boards
   * @param wh the {@link WaterHole} to feed from
   * @return true if the {@link SpeciesBoard} was able to successfully eat one token
   */
  public boolean feedSpecies(int toFeedIdx, WaterHole wh) {
    SpeciesBoard toFeed = idxToBoard(toFeedIdx);

    if (toFeed.eat(wh)) {
      if (toFeed.hasTrait(TraitName.FORAGING) && toFeed.eat(wh))
          cooperate(toFeedIdx, wh);
      cooperate(toFeedIdx, wh);
      return true;
    }
    else return false;
  }

  /**
   * Effect: directs the {@link SpeciesBoard} at the given index to cooperate with the
   *         {@link SpeciesBoard} to its right, if possible
   *
   * @param cooperatorIdx the index of the cooperator {@link SpeciesBoard}
   * @param wh the {@link WaterHole} to feedNext from
   */
  protected void cooperate(int cooperatorIdx, WaterHole wh) {
    SpeciesBoard cooperator = idxToBoard(cooperatorIdx);

    if (cooperator.hasTrait(TraitName.COOPERATION) && cooperatorIdx < getBoards().size() - 1)
      feedSpecies(cooperatorIdx + 1, wh);
  }

  /**
   * Effect: directs the {@link SpeciesBoard} at the given index to store the given number of tokens
   *  @param toStoreIdx the index of the {@link SpeciesBoard} to store bag in this {@link BasePlayer}'s
   *                  list of boards
   * @param wh the {@link WaterHole} to feed from
   */
  public void feedFatSpecies(int toStoreIdx, WaterHole wh) {
    SpeciesBoard fat = idxToBoard(toStoreIdx);
    int numTokens = fat.getFatNeed() < wh.getFood() ? fat.getFatNeed() : wh.getFood();
    if (fat.isFat()) fat.store(numTokens, wh);
  }

  /**
   * Determines whether or not the {@link SpeciesBoard} at the given index successfully ate a bag
   * token
   *
   * Effect: directs the {@link SpeciesBoard} at the given index to attack the {@link SpeciesBoard}
   *         at the given index of the given {@link EvoPlayer} and then eat one token
   *
   * @param attackerIdx the index of the {@link SpeciesBoard} to be fed in this {@link BasePlayer}'s
   *                    list of boards
   * @param victimOwner the {@link BasePlayer} that owns the {@link SpeciesBoard} to be attacked
   * @param victimIdx the index of the {@link SpeciesBoard} to be attacked in the
   *                  {@code victimOwner}'s list of boards
   * @param wh the {@link WaterHole} to feed from
   * @param deck the {@link Deck} to draw cards from if necessary
   * @return true if this {@link SpeciesBoard} was able to successfully eat one token
   */
  public boolean feedCarnivore(int attackerIdx, EvoPlayer victimOwner, int victimIdx,
                               WaterHole wh, Deck deck) {
    SpeciesBoard attacker = idxToBoard(attackerIdx);
    SpeciesBoard victim = victimOwner.idxToBoard(victimIdx);

    if (attacker.isCarn()) {
      victimOwner.decreasePopulationOf(victimIdx, deck);
      if (victim.hasTrait(TraitName.HORNS)) {
        decreasePopulationOf(attackerIdx, deck);
        if (attacker.isExtinct()) return false;
      }
      return feedSpecies(attackerIdx, wh);
    }
    else return false;
  }

  /**
   * Effect: decreases the {@code populationSize} of the {@link SpeciesBoard} at the given index
   *
   * @param idx the index of the {@link SpeciesBoard} to decrease
   * @param deck the {@link Deck} to draw cards from if necessary
   */
  protected void decreasePopulationOf(int idx, Deck deck) {
    SpeciesBoard toDecrease = idxToBoard(idx);
    toDecrease.decreasePopulation();
    if (toDecrease.isExtinct()) removeBoard(idx, deck);
  }

  /**
   * Effect: removes the given {@link SpeciesBoard} from the game and discards all
   *         {@link TraitCard}s associated with it
   *
   * @param boardIdx the index of the {@link SpeciesBoard} to remove
   * @param deck the {@link Deck} to draw from/discard to
   */
  protected SpeciesBoard removeBoard(int boardIdx, Deck deck) {
    SpeciesBoard toRemove = getBoards().remove(boardIdx);
    getHand().addAll(deck.draw(Constants.CARDS_PER_BOARD));
    return toRemove;
  }

  /**
   * todo
   * @param board
   * @param deck
   */
  protected void removeBoard(SpeciesBoard board, Deck deck) {
    getBoards().remove(board);
    getHand().addAll(deck.draw(Constants.CARDS_PER_BOARD));
  }

  /**
   * todo
   * @param newHand
   */
  public void updateHand(CardList newHand) {
    this.hand.update(newHand);
  }

  public CardList getHand() { return hand; }
  public SpeciesList getBoards() { return boards; }

  @Override
  public boolean isValid() {
    return boards.isValid() && hand.isValid() && bag >= Constants.MIN_FOOD_BAG;
  }


  /**
   * A Player is:
   *  [Nat, [Species+, ..., Species+], Cards]
   */

  private static final String BAG_STR = "bag";
  private static final int BAG_IDX = 0;
  private static final String BRDS_STR = "species";
  private static final int BRDS_IDX = 1;
  private static final String HAND_STR = "cards";
  private static final int HAND_IDX = 2;

  public int getBag() {
    return this.bag;
  }

  public static JsonSerializer serializer() { return new BasePlayerSerializer(); }
  public static JsonDeserializer deserializer() { return new BasePlayerDeserializer(); }
  public static BasePlayerBuilder builder() { return new BasePlayerBuilder(); }

  private static class BasePlayerSerializer implements JsonSerializer<BasePlayer> {
    @Override
    public JsonElement serialize(BasePlayer basePlayer, Type type,
                                 JsonSerializationContext jsonSerializationContext) {
      JsonArray result = new JsonArray();
      result.add(JUtils.makeJsonField("species", basePlayer.getBoards().toJson()));
      result.add(basePlayer.getBag());
      if (!basePlayer.getHand().isEmpty()) result.add(basePlayer.getHand().toJson());
      return result;
    }
  }

  private static class BasePlayerDeserializer implements JsonDeserializer<BasePlayer> {
    @Override
    public BasePlayer deserialize(JsonElement jsonElement, Type type,
                                  JsonDeserializationContext jsonDeserializationContext)
            throws JsonParseException {
      BasePlayerBuilder builder = builder();
      JsonArray jsonPlayer = jsonElement.getAsJsonArray();
      builder.bag(JUtils.getIntFromField(jsonPlayer, BAG_STR, BAG_IDX));
      builder.boards(JFactory.fromJson(JUtils.getArrayFromField(jsonPlayer, BRDS_STR, BRDS_IDX),
              SpeciesList.class));
      builder.hand(JFactory.fromJson(JUtils.getArrayFromField(jsonPlayer, HAND_STR, HAND_IDX),
              CardList.class));
      try { return builder.build(); }
      catch (InvalidEvoElementException e) { throw new JsonParseException(e); }
    }
  }

  public static class BasePlayerBuilder {
    protected SpeciesList boards = new SpeciesList();
    protected CardList hand = new CardList();
    protected int bag = Constants.MIN_FOOD_BAG;

    public BasePlayer build() throws InvalidEvoElementException {
      return new BasePlayer(boards, hand, bag);
    }

    public BasePlayerBuilder boards(SpeciesBoard... species) {
      this.boards.addAll(Arrays.asList(species));
      return this;
    }

    public BasePlayerBuilder boards(SpeciesList species) {
      this.boards = Objects.requireNonNull(species);
      return this;
    }

    public BasePlayerBuilder boards(List<SpeciesBoard> species) {
      this.boards = new SpeciesList(Objects.requireNonNull(species));
      return this;
    }

    public BasePlayerBuilder hand(CardList hand) {
      this.hand = Objects.requireNonNull(hand);
      return this;
    }

    public BasePlayerBuilder hand(List<TraitCard> hand) {
      this.hand = new CardList(Objects.requireNonNull(hand));
      return this;
    }

    public BasePlayerBuilder bag(int food) {
      this.bag = food;
      return this;
    }
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    BasePlayer that = (BasePlayer) o;

    if (getBag() != that.getBag()) return false;
    if (!getBoards().equals(that.getBoards())) return false;
    return getHand().equals(that.getHand());
  }

  @Override
  public int hashCode() {
    int result = getBoards().hashCode();
    result = 31 * result + getHand().hashCode();
    result = 31 * result + getBag();
    return result;
  }

  @Override
  public String toString() {
    return "BasePlayer{" +
            "boards=" + boards +
            ", hand=" + hand +
            ", bag=" + bag +
            '}';
  }
}
