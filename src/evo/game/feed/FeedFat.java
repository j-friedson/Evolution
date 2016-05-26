package evo.game.feed;

import com.google.gson.*;
import evo.game.*;
import evo.game.list.PlayerList;

import java.lang.reflect.Type;

/**
 * Created by jackfriedson on 3/22/16.
 */
public final class FeedFat extends FeedResponse {
  private int toStoreIdx;
  private int numTokens;

  public FeedFat(int fatSpeciesIdx, int fatTokens) {
    this.toStoreIdx = fatSpeciesIdx;
    this.numTokens = fatTokens;
  }

  @Override
  public boolean feedAction(EvoPlayer playerToFeed, PlayerList allPlayers, WaterHole wh, Deck deck) {
    playerToFeed.feedFatSpecies(toStoreIdx, wh);
    return true;
  }

  @Override
  public boolean isValid(EvoPlayer player, PlayerList allPlayers) {
    SpeciesBoard toStore;
    try { toStore = player.idxToBoard(toStoreIdx); }
    catch (IllegalArgumentException e) { return false; }
    return toStore.canStoreMore() && numTokens <= toStore.getBodySize() - toStore.getFatFood();
  }


  /**
   * A FatTissueChoice is a [Natural, Nat+].
   */

  public static JsonSerializer serializer() { return new FeedFatSerializer(); }
  private static class FeedFatSerializer implements JsonSerializer<FeedFat> {
    @Override
    public JsonElement serialize(FeedFat feedFat, Type type,
                                 JsonSerializationContext jsonSerializationContext) {
      JsonArray fatty = new JsonArray();
      JsonPrimitive speciesIdx = new JsonPrimitive(feedFat.toStoreIdx);
      JsonPrimitive tokens = new JsonPrimitive(feedFat.numTokens);
      fatty.add(speciesIdx);
      fatty.add(tokens);
      return fatty;
    }
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    FeedFat feedFat = (FeedFat) o;

    if (toStoreIdx != feedFat.toStoreIdx) return false;
    return numTokens == feedFat.numTokens;

  }

  @Override
  public int hashCode() {
    int result = toStoreIdx;
    result = 31 * result + numTokens;
    return result;
  }

  @Override
  public String toString() {
    return "FeedFat{" +
            "toStoreIdx=" + toStoreIdx +
            ", numTokens=" + numTokens +
            '}';
  }
}
