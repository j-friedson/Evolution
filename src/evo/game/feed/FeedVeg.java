package evo.game.feed;

import com.google.gson.*;
import evo.game.*;
import evo.game.list.PlayerList;

import java.lang.reflect.Type;

/**
 * Created by jackfriedson on 3/22/16.
 */
public final class FeedVeg extends FeedResponse {
  private int toFeedIdx;

  public FeedVeg(int vegetarianIdx) {
    this.toFeedIdx = vegetarianIdx;
  }

  @Override
  public boolean feedAction(EvoPlayer playerToFeed, PlayerList allPlayers, WaterHole wh, Deck deck) {
    playerToFeed.feedSpecies(toFeedIdx, wh);
    return true;
  }

  @Override
  public boolean isValid(EvoPlayer player, PlayerList allPlayers) {
    SpeciesBoard toFeed;
    try { toFeed = player.idxToBoard(toFeedIdx); }
    catch (IllegalArgumentException e) { return false; }
    return toFeed.isVeg() && toFeed.canEatMore();
  }

  /**
   * A VegetarianChoice is a Natural.
   */

  public static JsonSerializer serializer() { return new FeedVegSerializer(); }
  private static class FeedVegSerializer implements JsonSerializer<FeedVeg> {
    @Override
    public JsonElement serialize(FeedVeg feedVeg, Type type,
                                 JsonSerializationContext jsonSerializationContext) {
      return new JsonPrimitive(feedVeg.toFeedIdx);
//      return new JsonPrimitive(Integer.toString(feedVeg.toFeedIdx));
    }
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    FeedVeg feedVeg = (FeedVeg) o;

    return toFeedIdx == feedVeg.toFeedIdx;
  }

  @Override
  public int hashCode() {
    return toFeedIdx;
  }

  @Override
  public String toString() {
    return "FeedVeg{" +
            "toFeedIdx=" + toFeedIdx +
            '}';
  }
}