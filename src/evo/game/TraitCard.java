package evo.game;

import com.google.gson.*;
import evo.Constants;

import java.lang.reflect.Type;
import java.util.Objects;

public class TraitCard extends EvoElement {
  private final int foodPoints;
  private final TraitName name;

  public TraitCard(int foodPoints, TraitName name) {
    this.foodPoints = foodPoints;
    this.name = Objects.requireNonNull(name);
  }

  public TraitCard makeCopy() {
    return new TraitCard(foodPoints, name);
  }

  @Override
  public boolean isValid() {
    if (name == TraitName.CARNIVORE)
      return foodPoints >= Constants.MIN_CARN_FOODVAL && foodPoints <= Constants.MAX_CARN_FOODVAL;
    else
      return foodPoints >= Constants.MIN_VEG_FOODVAL && foodPoints <= Constants.MAX_VEG_FOODVAL;
  }

  /******************
   * Getter methods *
   ******************/
  public TraitName getName() {
    return this.name;
  }
  public int getFoodPoints() {
    return this.foodPoints;
  }

  /**
   * A Card is [FoodValue, Trait].
   *
   * A FoodValue is a JSON number interpretable as an integer between -8 and 8 (inclusive).
   *
   * A Trait is one of: "carnivore", "ambush", "burrowing", "climbing", "cooperation",
   *                    "fat-tissue", "fertile", "foraging", "hard-shell", "herding",
   *                    "horns", "long-neck", "pack-hunting", "scavenger", "symbiosis",
   *                    or "warning-call".
   */

  private static final int FOODVAL_IDX = 0;
  private static final int NAME_IDX = 1;

  public static JsonSerializer serializer() { return new TraitCardSerializer(); }
  public static JsonDeserializer deserializer() { return new TraitCardDeserializer(); }

  private static class TraitCardSerializer implements JsonSerializer<TraitCard> {
    @Override
    public JsonElement serialize(TraitCard traitCard, Type type,
                                 JsonSerializationContext jsonSerializationContext) {
      JsonArray result = new JsonArray();
      result.add(new JsonPrimitive(traitCard.foodPoints));
      result.add(new JsonPrimitive(traitCard.name.toString()));
      return result;
    }
  }

  private static class TraitCardDeserializer implements JsonDeserializer<TraitCard> {
    @Override
    public TraitCard deserialize(JsonElement jsonElement, Type type,
                                 JsonDeserializationContext jsonDeserializationContext)
            throws JsonParseException {
      JsonArray jsonCard = jsonElement.getAsJsonArray();
      TraitCard result = new TraitCard(jsonCard.get(FOODVAL_IDX).getAsInt(),
              TraitName.fromString(jsonCard.get(NAME_IDX).getAsString()));
      if (!result.isValid()) throw new JsonParseException("Invalid TraitCard: " + result.toString());
      else return result;
    }
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    TraitCard traitCard = (TraitCard) o;

    if (getFoodPoints() != traitCard.getFoodPoints()) return false;
    return getName() == traitCard.getName();

  }

  @Override
  public int hashCode() {
    int result = getFoodPoints();
    result = 31 * result + getName().hashCode();
    return result;
  }

  @Override
  public String toString() {
    return "TraitCard{" +
            "foodPoints=" + foodPoints +
            ", name=" + name +
            '}';
  }
}
