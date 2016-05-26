package evo.game.feed;

import com.google.gson.*;
import com.google.gson.stream.JsonWriter;
import evo.game.*;
import evo.game.list.PlayerList;
import evo.json.JFactory;

import java.io.IOException;
import java.lang.reflect.Type;

/**
 * Created by jackfriedson on 3/22/16.
 */
public abstract class FeedResponse implements PlayerResponse {

  /**
   * Determines whether a feed action was carried out or not
   *
   * Effect: directs the given {@link EvoPlayer} to take the appropriate feed action
   *
   * @param playerToFeed the {@link EvoPlayer} to feedNext a {@link SpeciesBoard}
   * @param allPlayers the {@link PlayerList} of all players in the game
   * @param wh the {@link WaterHole} to feedNext from
   * @param deck the {@link Deck} to draw cards from if necessary
   * @return true if a feed action was carried out, false if no boards was fed (i.e. this is
   *         a {@link FeedNothing})
   */
  public abstract boolean feedAction(EvoPlayer playerToFeed, PlayerList allPlayers, WaterHole wh,
                                     Deck deck);

  public abstract boolean isValid(EvoPlayer player, PlayerList allPlayers);

  /**
   *
   * A FeedingChoice is one of:
   * - VegetarianChoice
   * - FatTissueChoice
   * - CarnivoreChoice
   * - false
   *
   * A VegetarianChoice is a Natural.
   * A FatTissueChoice is a [Natural, Nat+].
   * A CarnivoreChoice is [Natural, Natural, Natural].
   *
   */

  @Override
  public JsonElement toJson() {
    return JFactory.getGson().toJsonTree(this, this.getClass());
  }

  @Override
  public void toJson(JsonWriter writer) throws IOException {
    JFactory.getGson().toJson(this, this.getClass(), writer);
    writer.flush();
  }

  public static JsonDeserializer deserializer() { return new FeedResponseDeserializer(); }
  private static class FeedResponseDeserializer implements JsonDeserializer<FeedResponse> {
    private static final int FAT_SIZE = 2;
    private static final int BRD_IDX = 0;
    private static final int TKN_IDX = 1;

    private static final int CARN_SIZE = 3;
    private static final int ATKR_IDX = 0;
    private static final int OWNR_IDX = 1;
    private static final int DFNDR_IDX = 2;

    @Override
    public FeedResponse deserialize(JsonElement jsonElement, Type type,
                                    JsonDeserializationContext jsonDeserializationContext)
            throws JsonParseException {
      JsonArray ja;
      if (jsonElement.isJsonPrimitive()) {
        if (jsonElement.getAsJsonPrimitive().isBoolean()) return new FeedNothing();
        else return new FeedVeg(jsonElement.getAsInt());
      }
      else if ((ja = jsonElement.getAsJsonArray()).size() == FAT_SIZE)
        return new FeedFat(ja.get(BRD_IDX).getAsInt(), ja.get(TKN_IDX).getAsInt());
      else if (ja.size() == CARN_SIZE)
        return new FeedCarn(ja.get(ATKR_IDX).getAsInt(), ja.get(OWNR_IDX).getAsInt(),
                ja.get(DFNDR_IDX).getAsInt());
      else throw new JsonParseException("Invalid JSON FeedResponse");
    }
  }
}
