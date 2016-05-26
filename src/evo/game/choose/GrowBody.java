package evo.game.choose;

import com.google.gson.*;
import evo.game.BasePlayer;
import evo.game.list.CardList;

import java.lang.reflect.Type;

/**
 * Created by jackfriedson on 3/31/16.
 */
public class GrowBody extends CardAction {
  private int speciesIdx;

  public GrowBody(int cardIdx, int speciesIdx) {
    super(cardIdx);
    this.speciesIdx = speciesIdx;
  }

  @Override
  public void doAction(BasePlayer player, CardList newHand) {
    player.idxToBoard(speciesIdx).increaseBodySize();
    newHand.remove(player.idxToCard(cardIdx));
  }

  @Override
  public void addToAction4(Action4 action4) {
    action4.addGrowBody(this);
  }

  @Override
  public boolean isValid(BasePlayer player) {
    return super.isValid(player);
  }

  public int getSpeciesIdx() {
    return speciesIdx;
  }


  /**
   * A GB is [Natural, Natural].
   */

  private static final int BRD_IDX = 0;
  private static final int CARD_IDX = 1;

  public static JsonSerializer serializer() { return new GrowBodySerializer(); }
  private static class GrowBodySerializer implements JsonSerializer<GrowBody> {
    @Override
    public JsonElement serialize(GrowBody growBody, Type type,
                                 JsonSerializationContext jsonSerializationContext) {
      JsonArray result = new JsonArray();
      result.add(new JsonPrimitive(growBody.speciesIdx));
      result.add(new JsonPrimitive(growBody.cardIdx));
      return result;
    }
  }

  public static JsonDeserializer deserializer() { return new GrowBodyDeserializer(); }
  private static class GrowBodyDeserializer implements JsonDeserializer<GrowBody> {
    @Override
    public GrowBody deserialize(JsonElement jsonElement, Type type,
                                JsonDeserializationContext jsonDeserializationContext)
            throws JsonParseException {
      JsonArray jArray = jsonElement.getAsJsonArray();
      return new GrowBody(jArray.get(CARD_IDX).getAsInt(), jArray.get(BRD_IDX).getAsInt());
    }
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    if (!super.equals(o)) return false;

    GrowBody growBody = (GrowBody) o;

    return getSpeciesIdx() == growBody.getSpeciesIdx();

  }

  @Override
  public int hashCode() {
    int result = super.hashCode();
    result = 31 * result + getSpeciesIdx();
    return result;
  }

  @Override
  public String toString() {
    return "GrowBody{" +
            "speciesIdx=" + speciesIdx +
            '}';
  }
}
