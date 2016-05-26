package evo.json;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import evo.game.*;
import evo.game.choose.*;
import evo.game.feed.*;
import evo.game.list.CardList;
import evo.game.list.PlayerList;
import evo.game.list.SpeciesList;

import java.io.IOException;
import java.util.Optional;

/**
 * Created by jackfriedson on 4/23/16.
 */
public class JFactory {
  private static final Gson gson = new GsonBuilder()
          .registerTypeAdapter(SpeciesBoard.class, SpeciesBoard.serializer())
          .registerTypeAdapter(SpeciesBoard.class, SpeciesBoard.deserializer())
          .registerTypeAdapter(SpeciesList.class, SpeciesList.serializer())
          .registerTypeAdapter(SpeciesList.class, SpeciesList.deserializer())
          .registerTypeAdapter(TraitName.class, TraitName.serializer())
          .registerTypeAdapter(TraitCard.class, TraitCard.serializer())
          .registerTypeAdapter(TraitCard.class, TraitCard.deserializer())
          .registerTypeAdapter(CardList.class, CardList.serializer())
          .registerTypeAdapter(CardList.class, CardList.deserializer())
          .registerTypeAdapter(BasePlayer.class, BasePlayer.serializer())
          .registerTypeAdapter(BasePlayer.class, BasePlayer.deserializer())
          .registerTypeAdapter(EvoPlayer.class, EvoPlayer.serializer())
          .registerTypeAdapter(EvoPlayer.class, EvoPlayer.deserializer())
          .registerTypeAdapter(PlayerList.class, PlayerList.serializer())
          .registerTypeAdapter(PlayerList.class, PlayerList.deserializer())
          .registerTypeAdapter(FeedResponse.class, FeedResponse.deserializer())
          .registerTypeAdapter(FeedNothing.class, FeedNothing.serializer())
          .registerTypeAdapter(FeedNothing.class, FeedNothing.deserializer())
          .registerTypeAdapter(FeedVeg.class, FeedVeg.serializer())
          .registerTypeAdapter(FeedVeg.class, FeedVeg.deserializer())
          .registerTypeAdapter(FeedFat.class, FeedFat.serializer())
          .registerTypeAdapter(FeedFat.class, FeedFat.deserializer())
          .registerTypeAdapter(FeedCarn.class, FeedCarn.serializer())
          .registerTypeAdapter(FeedCarn.class, FeedCarn.deserializer())
          .registerTypeAdapter(Action4.class, Action4.serializer())
          .registerTypeAdapter(Action4.class, Action4.deserializer())
          .registerTypeAdapter(GrowPop.class, GrowPop.serializer())
          .registerTypeAdapter(GrowPop.class, GrowPop.deserializer())
          .registerTypeAdapter(GrowBody.class, GrowBody.serializer())
          .registerTypeAdapter(GrowBody.class, GrowBody.deserializer())
          .registerTypeAdapter(BoardTrade.class, BoardTrade.serializer())
          .registerTypeAdapter(BoardTrade.class, BoardTrade.deserializer())
          .registerTypeAdapter(ReplaceTrait.class, ReplaceTrait.serializer())
          .registerTypeAdapter(ReplaceTrait.class, ReplaceTrait.deserializer())
          .registerTypeAdapter(Dealer.class, Dealer.serializer())
          .registerTypeAdapter(Dealer.class, Dealer.deserializer())
          .setPrettyPrinting()
          .create();

  /**
   * Creates a new instance of some {@link JSerializable} class from a {@link JsonElement}.
   *
   * @param jsonElement the {@link JsonElement} to create the new object from
   * @param classOfT the {@link Class} of the object to be created
   * @param <T> must extend the {@link JSerializable} interface
   * @return the new object
   */
  public static <T extends JSerializable> T fromJson(JsonElement jsonElement, Class<T> classOfT) {
    return gson.fromJson(jsonElement, classOfT);
  }

  /**
   * Creates a new instance of some {@link JSerializable} class from a {@link JsonReader}.
   *
   * @param reader the {@link JsonReader} to create the new object from
   * @param classOfT the {@link Class} of the object to be created
   * @param <T> must extend the {@link JSerializable} interface
   * @return the new object
   */
  public static <T extends JSerializable> T fromJson(JsonReader reader, Class<T> classOfT) {
      return gson.fromJson(reader, classOfT);
  }

  /**
   * Gets the custom {@link Gson} object used for serializing/deserializing  all
   * {@link JSerializable} elements of the game.
   *
   * @return the custom {@link Gson}
   */
  public static Gson getGson() { return gson; }
}
