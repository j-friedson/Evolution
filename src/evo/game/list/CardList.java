package evo.game.list;

import com.google.gson.*;
import evo.game.TraitCard;
import evo.game.TraitName;
import evo.json.JFactory;

import java.lang.reflect.Type;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;

/**
 * Created by jackfriedson on 4/2/16.
 */
public class CardList extends EvoList<TraitCard> {

  public CardList() { super(false); }

  public CardList(List<TraitCard> cards) { super(cards, false); }

  public void shuffle() { Collections.shuffle(this.contents); }

  @Override
  public CardList makeCopy() {
    CardList result = new CardList();
    this.forEach(tc -> result.add(tc.makeCopy()));
    return result;
  }

  @Override
  public CardList filterBy(Predicate<TraitCard> pred) {
    CardList result = new CardList();
    this.forEach(tc -> { if (pred.test(tc)) result.add(tc); });
    return result;
  }

  /**
   * Determines the index of the given trait in this list, or returns -1 if it is not in this list
   *
   * @param trait the {@link TraitName} of the trait to be found
   * @return the index of the trait in this list, or -1 if it cannot be found
   */
  public int containsTrait(TraitName trait) {
    for (int i = 0; i < size(); i++)
      if (get(i).getName() == trait) return i;
    return -1;
  }


  /**
   * A Cards is [Card, ..., Card].
   *
   * A LOT is one of:
   * []
   * [Trait]
   * [Trait,Trait]
   * [Trait,Trait,Trait]
   */

  public static JsonDeserializer deserializer() { return new CardListDeserializer(); }

  public JsonElement namesToJson() {
    Gson gson = new GsonBuilder()
            .registerTypeAdapter(CardList.class, new CardListNameSerializer())
            .create();
    return gson.toJsonTree(this, CardList.class);
  }

  private static class CardListNameSerializer implements JsonSerializer<CardList> {
    @Override
    public JsonElement serialize(CardList traitCards, Type type,
                                 JsonSerializationContext jsonSerializationContext) {
      JsonArray result = new JsonArray();
      for (TraitCard tc : traitCards)
        result.add(tc.getName().toString());
      return result;
    }
  }

  private static class CardListDeserializer implements JsonDeserializer<CardList> {
    @Override
    public CardList deserialize(JsonElement jsonElement, Type type,
                                JsonDeserializationContext jsonDeserializationContext)
            throws JsonParseException {
      JsonArray jsonList = jsonElement.getAsJsonArray();
      CardList result = new CardList();
      for (JsonElement j : jsonList)
              result.add(JFactory.fromJson(j.getAsJsonArray(), TraitCard.class));
      if (!result.isValid()) throw new JsonParseException("Invalid CardList: " + result.toString());
      else return result;
    }
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    CardList that = (CardList) o;
    return contents.equals(that.contents);
  }


  @Override
  public int hashCode() {
    return contents.hashCode();
  }

  @Override
  public String toString() {
    return "CardList{" +
            "cards=" + contents +
            '}';
  }
}
