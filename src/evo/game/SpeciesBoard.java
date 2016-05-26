package evo.game;

import com.google.gson.*;
import evo.Constants;
import evo.game.list.CardList;
import evo.game.list.SpeciesList;
import evo.json.JUtils;

import java.lang.reflect.Type;
import java.util.*;

/**
 * Represents a SpeciesBoard in the Evolution game.
 */
public class SpeciesBoard extends EvoElement {
  private int foodSupply;
  private int bodySize;
  private int populationSize;
  private final CardList traits;
  private int fatFood;

  private SpeciesBoard(int foodSupply, int bodySize, int popSize, CardList traits, int fatFood)
  throws InvalidEvoElementException {
    this.foodSupply = foodSupply;
    this.bodySize = bodySize;
    this.populationSize = popSize;
    this.traits = Objects.requireNonNull(traits);
    this.fatFood = fatFood;
    if (!isValid()) throw new InvalidEvoElementException("Invalid SpeciesBoard: " + toString());
  }

  /**
   * todo
   * @return
   */
  public SpeciesBoard makeCopy() {
    return builder()
            .food(foodSupply)
            .body(bodySize)
            .pop(populationSize)
            .traits(traits.makeCopy())
            .fat(fatFood)
            .build();
  }

  /**
   * Performs end-of-turn actions on this {@link SpeciesBoard}, i.e. decreases its population size
   * to match its bag supply, resets its bag supply to 0, and returns the number of food tokens
   * it had eaten
   *
   * @return the number of food tokens successfully eaten by this species, this round
   */
  public int endTurn() {
    int toBag = foodSupply;
    populationSize = foodSupply;
    foodSupply = 0;
    return toBag;
  }

  /**
   * Determines whether or not this SpeciesBoard can attack the given SpeciesBoard
   *
   * @param defender The {@link SpeciesBoard} this wants to attack
   * @param leftNeighbor The {@link Optional<SpeciesBoard>} to the left of the defender
   * @param rightNeighbor The {@link Optional<SpeciesBoard>} to the right of the defender
   * @return true if this {@link SpeciesBoard} can successfully attack the defender
   */
  public boolean canAttack(SpeciesBoard defender, Optional<SpeciesBoard> leftNeighbor,
                           Optional<SpeciesBoard> rightNeighbor) {

    int attackSize = hasTrait(TraitName.PACK_HUNTING) ? this.bodySize + this.populationSize
            : this.bodySize;

    return  !(this == defender || isVeg()
            || (defender.hasTrait(TraitName.CLIMBING) && !hasTrait(TraitName.CLIMBING))
            || (defender.hasTrait(TraitName.HARD_SHELL) && attackSize -
            defender.getBodySize() < 4)
            || (defender.hasTrait(TraitName.BURROWING) && defender.getFoodSupply() ==
            defender.getPopulationSize())
            || (defender.hasTrait(TraitName.HERDING) && defender.getPopulationSize() >=
            getPopulationSize())
            || ((leftNeighbor.isPresent() && leftNeighbor.get().hasTrait(TraitName.WARNING_CALL) ||
            rightNeighbor.isPresent() && rightNeighbor.get().hasTrait(TraitName.WARNING_CALL))
            && !hasTrait(TraitName.AMBUSH))
            || (defender.hasTrait(TraitName.SYMBIOSIS) && rightNeighbor.isPresent() &&
            rightNeighbor.get().getBodySize() > defender.getBodySize()));
  }

  /**
   * Determines whether this can attack any of the {@link SpeciesBoard}s belonging to any of the
   * given {@link SpeciesList}s
   *
   * @param allBoards the list of {@link SpeciesList}s belonging to players in the game
   * @return true if there is a boards that can be attacked
   */
  public boolean canAttackAny(List<SpeciesList> allBoards) {
    for (SpeciesList sl : allBoards) {
      for (int i = 0; i < sl.size(); i++) {
        if (this.canAttack(sl.get(i), sl.leftNeighborOf(i), sl.rightNeighborOf(i)))
          return true;
      }
    }
    return false;
  }

  /**
   * Determines whether this {@link SpeciesBoard} has successfully eaten or not
   *
   * Effect: increases this {@link SpeciesBoard}'s {@code foodSupply} by one, decreases the given
   *         {@link WaterHole} by one
   *
   * @param wh the {@link WaterHole} to feedNext from
   * @return true if this {@link SpeciesBoard} ate successfully
   */
  public boolean eat(WaterHole wh) {
    if (wh.isEmpty() || !canEatMore())
      return false;
    else {
      foodSupply += wh.takeOne();
      return true;
    }
  }

  /**
   * Effect: increases this {@link SpeciesBoard}'s {@code fatFood} by the given number of tokens
   *         (or until it is full), taking them from the given {@link WaterHole}
   *
   * @param numTokens the number of tokens to store
   * @param wh the {@link WaterHole} to take from
   */
  public void store(int numTokens, WaterHole wh) {
    while (numTokens > 0 && !wh.isEmpty() && canStoreMore()) {
      fatFood += wh.takeOne();
      numTokens--;
    }
  }

  /**
   * Determines whether this {@link SpeciesBoard} has the given trait
   *
   * @param trait the {@link TraitName} to check for
   * @return true if this {@link SpeciesBoard} has the given {@link TraitName}
   */
  public boolean hasTrait(TraitName trait) {
    return traits.containsTrait(trait) != -1;
  }

  /**
   * Effect: increases this {@link SpeciesBoard}'s {@code populationSize} by one, up to the max
   */
  public void increasePopulation() {
    if (this.populationSize < Constants.MAX_POP_SIZE)
      this.populationSize++;
  }

  /**
   * Effect: decreases this {@link SpeciesBoard}'s {@code populationSize} by one, and if that
   *         would bring it lower than its {@code foodSupply}, then decreases the {@code foodSupply}
   *         to match
   */
  public void decreasePopulation() {
    this.populationSize--;

    if (this.populationSize < this.foodSupply)
      this.foodSupply = this.populationSize;
  }

  /**
   * Determines whether this {@link SpeciesBoard} is extinct
   *
   * @return true if this {@link SpeciesBoard} is extinct
   */
  public boolean isExtinct() {
    return this.populationSize == 0;
  }

  /**
   * Effect: increases this {@link SpeciesBoard}'s {@code foodSupply} by one, unless it is already
   *         equal to its {@code populationSize}
   */
  private void increaseFoodSupply() {
    if (this.foodSupply < this.populationSize)
      this.foodSupply++;
  }

  /**
   * Effect: removes one token of stored fat-bag from this {@link SpeciesBoard}
   */
  private void decreaseFatFood() {
    if (this.fatFood == Constants.MIN_FAT_FOOD)
      throw new IllegalStateException("Cannot remove more food than is stored");
    this.fatFood--;
  }

  /**
   * Effect: moves as much stored bag as possible to this {@link SpeciesBoard}'s {@code foodSupply}
   */
  public void eatStoredFood() {
    while (fatFood > Constants.MIN_FAT_FOOD && canEatMore()) {
      increaseFoodSupply();
      decreaseFatFood();
    }
  }

  /**
   * Gets the {@link TraitCard} at the given index
   *
   * Effect: replaces the old {@link TraitCard} with the new one
   *
   * @param replaceIdx
   * @param toAdd
   * @return
   */
  public TraitCard replaceCard(int replaceIdx, TraitCard toAdd) {
    if (traits.containsTrait(toAdd.getName()) != -1 &&
            traits.get(replaceIdx).getName() != toAdd.getName())
      throw new DuplicateTraitException("Species already has " + toAdd.getName().toString());

    TraitCard removed = traits.remove(replaceIdx);
    traits.add(replaceIdx, toAdd);
    if (removed.getName() == TraitName.FAT_TISSUE) fatFood = 0;
    return removed;
  }

  /**
   * Determines whether this {@link SpeciesBoard} can eat more food
   *
   * @return true if this {@link SpeciesBoard} can eat more
   */
  public boolean canEatMore() {
    return this.foodSupply < this.populationSize;
  }

  /**
   * Determines whether this {@link SpeciesBoard} can store more food
   *
   * @return true if this {@link SpeciesBoard} can store more
   */
  public boolean canStoreMore() {
    return this.isFat() && getFatNeed() != 0;
  }

  /**
   * Determines the maximum number of fat-bag tokens this {@link SpeciesBoard} can store.
   *
   * @return the number of tokens to store
   */
  public int getFatNeed() { return isFat() ? this.getBodySize() - this.getFatFood() : 0;}

  /**
   * Effect: increases this {@link SpeciesBoard}'s {@code bodySize} by one, up to a max of 7
   */
  public void increaseBodySize() {
    if (this.bodySize < Constants.MAX_BODY_SIZE) this.bodySize++;
  }

  /**
   * Determines whether this {@link SpeciesBoard} is comes before than the given
   * {@link SpeciesBoard} in the order specified by the given {@link Comparator}.
   *
   * @param sb The {@link SpeciesBoard} to compare this one against
   * @return true if this {@link SpeciesBoard} comes before the other
   */
  public boolean compareToThat(SpeciesBoard sb, Comparator<SpeciesBoard> comp) {
    return comp.compare(this, sb) < 0;
  }

  public boolean isFat() {
    return hasTrait(TraitName.FAT_TISSUE);
  }
  public boolean isVeg() {
    return !hasTrait(TraitName.CARNIVORE);
  }
  public boolean isCarn() {
    return hasTrait(TraitName.CARNIVORE);
  }

  @Override
  public boolean isValid() {
    /**
     * A species board represents a species, specifically its population size and the body size of
     * its members. Both population and body size range between 0 and 7 (inclusive). During the
     * game, each species board may be associated with up to three trait cards.
     *
     * Fat Tissue allows a species to store as many food tokens as its body-size count.
     */
    if (traits.size() > Constants.MAX_TRAITS_PER_BOARD || !traits.isValid()) return false;
    if (hasTrait(TraitName.FAT_TISSUE) && (fatFood > bodySize || fatFood < 0)) return false;
    if (!hasTrait(TraitName.FAT_TISSUE) && fatFood != 0) return false;

    return populationSize >= Constants.MIN_POP_SIZE  && populationSize <= Constants.MAX_POP_SIZE
            && bodySize >= Constants.MIN_BODY_SIZE && bodySize <= Constants.MAX_BODY_SIZE
            && foodSupply >= Constants.MIN_FOOD_SIZE && foodSupply <= populationSize;
  }

  /******************
   * Getter methods *
   ******************/
  public int getBodySize() {
    return this.bodySize;
  }
  public int getFoodSupply() {
    return this.foodSupply;
  }
  public int getFatFood() {
    return this.fatFood;
  }
  public int getPopulationSize() {
    return this.populationSize;
  }
  public CardList getTraitCards() {
    return this.traits;
  }

  /**
   * A Species+ is one of:
   * - a regular SpeciesM
   * - a Species with a "fat-food" field:
   *   [["food", Nat],
   *    ["body", Nat],
   *    ["population", Nat+],
   *    ["traits", LOT]
   *    ["fat-food", Nat]]
   */

  private static final String FOOD_STR = "food";
  private static final int FOOD_IDX = 0;
  private static final String BODY_STR = "body";
  private static final int BODY_IDX = 1;
  private static final String POP_STR = "population";
  private static final int POP_IDX = 2;
  private static final String TRAIT_STR = "traits";
  private static final int TRAIT_IDX = 3;
  private static final String FAT_STR = "fat-food";
  private static final int FAT_IDX = 4;


  public static SpeciesBoardSerializer serializer() { return new SpeciesBoardSerializer(); }

  private static class SpeciesBoardSerializer implements JsonSerializer<SpeciesBoard> {
    @Override
    public JsonElement serialize(SpeciesBoard speciesBoard, Type type,
                                 JsonSerializationContext jsonSerializationContext) {
      JsonArray result = new JsonArray();
      result.add(JUtils.makeIntField(FOOD_STR, speciesBoard.foodSupply));
      result.add(JUtils.makeIntField(BODY_STR, speciesBoard.bodySize));
      result.add(JUtils.makeIntField(POP_STR, speciesBoard.populationSize));
      result.add(JUtils.makeJsonField(TRAIT_STR, speciesBoard.traits.namesToJson()));

      if (speciesBoard.isFat() && speciesBoard.getFatFood() != 0)
        result.add(JUtils.makeIntField(FAT_STR, speciesBoard.fatFood));

      return result;
    }
  }

  public static SpeciesBoardDeserializer deserializer() { return new SpeciesBoardDeserializer(); }

  private static class SpeciesBoardDeserializer implements JsonDeserializer<SpeciesBoard> {
    @Override
    public SpeciesBoard deserialize(JsonElement jsonElement, Type type,
                                    JsonDeserializationContext jsonDeserializationContext)
            throws JsonParseException {

      JsonArray jsonSpecies = jsonElement.getAsJsonArray();
      SpeciesBuilder builder = builder();
      builder.food(JUtils.getIntFromField(jsonSpecies, FOOD_STR, FOOD_IDX));
      builder.body(JUtils.getIntFromField(jsonSpecies, BODY_STR, BODY_IDX));
      builder.pop(JUtils.getIntFromField(jsonSpecies, POP_STR, POP_IDX));

      JsonArray traitsArray = JUtils.getArrayFromField(jsonSpecies, TRAIT_STR, TRAIT_IDX);
      List<TraitCard> traits = new ArrayList<>();
      for (JsonElement je : traitsArray)
        traits.add(new TraitCard(Constants.DEFAULT_FOOD_VAL, TraitName.fromString(je.getAsString())));
      builder.traits(traits);

      if (jsonSpecies.size() == 5)
        builder.fat(JUtils.getIntFromField(jsonSpecies, FAT_STR, FAT_IDX));

      try { return builder.build(); }
      catch (InvalidEvoElementException e) { throw new JsonParseException(e); }
    }
  }

  public static SpeciesBuilder builder() { return new SpeciesBuilder(); }
  public static class SpeciesBuilder {
    private int food = Constants.MIN_FOOD_SIZE;
    private int body = Constants.MIN_BODY_SIZE;
    private int pop = Constants.MIN_POP_SIZE;
    private CardList traits = new CardList();
    private int fat = Constants.MIN_FAT_FOOD;

    public SpeciesBoard build() throws InvalidEvoElementException {
      return new SpeciesBoard(food, body, pop, traits, fat);
    }

    public SpeciesBuilder pop(int pop) {
      this.pop = pop;
      return this;
    }

    public SpeciesBuilder body(int body) {
      this.body = body;
      return this;
    }

    public SpeciesBuilder food(int food) {
      this.food = food;
      return this;
    }

    public SpeciesBuilder traits(String... traits) {
      for (String s : traits) {
        this.traits.add(new TraitCard(0, TraitName.fromString(s)));
      }
      return this;
    }

    public SpeciesBuilder traits(CardList traits) {
      this.traits = Objects.requireNonNull(traits);
      return this;
    }

    public SpeciesBuilder traits(List<TraitCard> traits) {
      this.traits = new CardList(Objects.requireNonNull(traits));
      return this;
    }

    public SpeciesBuilder fat(int fat) {
      this.fat = fat;
      return this;
    }
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    SpeciesBoard that = (SpeciesBoard) o;

    if (getFoodSupply() != that.getFoodSupply()) return false;
    if (getBodySize() != that.getBodySize()) return false;
    if (getPopulationSize() != that.getPopulationSize()) return false;
    if (getFatFood() != that.getFatFood()) return false;
    return traits.equals(that.traits);

  }

  @Override
  public int hashCode() {
    int result = getFoodSupply();
    result = 31 * result + getBodySize();
    result = 31 * result + getPopulationSize();
    result = 31 * result + traits.hashCode();
    result = 31 * result + getFatFood();
    return result;
  }

  @Override
  public String toString() {
    return "SpeciesBoard{" +
            "foodSupply=" + foodSupply +
            ", bodySize=" + bodySize +
            ", populationSize=" + populationSize +
            ", traits=" + traits +
            ", fatFood=" + fatFood +
            '}';
  }
}