package evo.game.feed;

import com.google.gson.*;
import evo.game.Deck;
import evo.game.EvoPlayer;
import evo.game.list.PlayerList;
import evo.game.WaterHole;

import java.lang.reflect.Type;

/**
 * Created by jackfriedson on 3/22/16.
 */
public final class FeedNothing extends FeedResponse {

  public FeedNothing() { }

  @Override
  public boolean feedAction(EvoPlayer playerToFeed, PlayerList allPlayers, WaterHole wh, Deck deck) {
    return false;
  }

  @Override
  public boolean isValid(EvoPlayer player, PlayerList allPlayers) {
    return true;
  }

  public static JsonSerializer serializer() { return new FeedNothingSerializer(); }
  private static class FeedNothingSerializer implements JsonSerializer<FeedNothing> {
    @Override
    public JsonElement serialize(FeedNothing feedNothing, Type type,
                                 JsonSerializationContext jsonSerializationContext) {
      return new JsonPrimitive(false);
    }
  }

  @Override
  public String toString() {
    return "FeedNothing{}";
  }
}