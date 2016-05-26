package evo.game.choose;

import com.google.gson.*;
import evo.Constants;
import evo.game.*;
import evo.game.list.CardList;

import java.lang.reflect.Type;

/**
 * Created by jackfriedson on 3/31/16.
 */
public class ReplaceTrait extends CardAction {
  private int toReplaceIdx;
  private int speciesIdx;

  public ReplaceTrait(int cardIdx, int toReplaceIdx, int speciesIdx) {
    super(cardIdx);
    this.toReplaceIdx = toReplaceIdx;
    this.speciesIdx = speciesIdx;
  }

  @Override
  public void doAction(BasePlayer player, CardList newHand) {
    TraitCard toPlay = player.idxToCard(cardIdx);
    SpeciesBoard toMod = player.idxToBoard(speciesIdx);
    toMod.replaceCard(toReplaceIdx, toPlay);
    newHand.remove(toPlay);
  }

  @Override
  public void addToAction4(Action4 action4) {
    action4.addReplaceTrait(this);
  }

  @Override
  public boolean isValid(BasePlayer player) {
    return super.isValid(player)
            && toReplaceIdx >= 0
            && toReplaceIdx < Constants.MAX_TRAITS_PER_BOARD;
  }

  public int getToReplaceIdx() {
    return toReplaceIdx;
  }

  public int getSpeciesIdx() {
    return speciesIdx;
  }

  /**
   * An RT is [Natural, Natural, Natural].
   */

  public static JsonSerializer serializer() { return new ReplaceTraitSerializer(); }
  private static class ReplaceTraitSerializer implements JsonSerializer<ReplaceTrait> {
    @Override
    public JsonElement serialize(ReplaceTrait replaceTrait, Type type,
                                 JsonSerializationContext jsonSerializationContext) {
      JsonArray result = new JsonArray();
      result.add(new JsonPrimitive(replaceTrait.speciesIdx));
      result.add(new JsonPrimitive(replaceTrait.toReplaceIdx));
      result.add(new JsonPrimitive(replaceTrait.cardIdx));
      return result;
    }
  }

  public static JsonDeserializer deserializer() { return new ReplaceTraitDeserializer(); }
  private static class ReplaceTraitDeserializer implements JsonDeserializer<ReplaceTrait> {
    @Override
    public ReplaceTrait deserialize(JsonElement jsonElement, Type type,
                                    JsonDeserializationContext jsonDeserializationContext)
            throws JsonParseException {
      JsonArray jArray = jsonElement.getAsJsonArray();
      return new ReplaceTrait(jArray.get(2).getAsInt(), jArray.get(1).getAsInt(),
              jArray.get(0).getAsInt());
    }
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    if (!super.equals(o)) return false;

    ReplaceTrait that = (ReplaceTrait) o;

    if (getToReplaceIdx() != that.getToReplaceIdx()) return false;
    return getSpeciesIdx() == that.getSpeciesIdx();

  }

  @Override
  public int hashCode() {
    int result = super.hashCode();
    result = 31 * result + getToReplaceIdx();
    result = 31 * result + getSpeciesIdx();
    return result;
  }

  @Override
  public String toString() {
    return "ReplaceTrait{" +
            "toReplaceIdx=" + toReplaceIdx +
            ", speciesIdx=" + speciesIdx +
            '}';
  }
}