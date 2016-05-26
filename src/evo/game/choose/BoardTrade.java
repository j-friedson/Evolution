package evo.game.choose;

import com.google.gson.*;
import evo.game.*;
import evo.game.list.CardList;

import java.lang.reflect.Type;
import java.util.*;

/**
 * Created by jackfriedson on 3/31/16.
 */
public class BoardTrade extends CardAction {
  private List<Integer> traitIdxs;

  public BoardTrade(int cardIdx, List<Integer> traitsToAdd) {
    super(cardIdx);
    this.traitIdxs = Objects.requireNonNull(traitsToAdd);
    if (traitIdxs.size() > 3) throw new IllegalArgumentException("Max 3 traits");
  }

  public BoardTrade(int cardIdx, Integer... traitsToAdd) {
    super(cardIdx);
    this.traitIdxs = Arrays.asList(traitsToAdd);
    if (traitIdxs.size() > 3) throw new IllegalArgumentException("Max 3 traits");
  }

  @Override
  public void doAction(BasePlayer player, CardList newHand) {
    TraitCard toPlay = player.idxToCard(cardIdx);
    List<TraitCard> traits = Utils.map(traitIdxs, player::idxToCard);
    SpeciesBoard toAdd = SpeciesBoard.builder().traits(traits).build();
    player.addSpecies(toAdd);

    traits.forEach(newHand::remove);
    newHand.remove(toPlay);
  }

  @Override
  public void removeCardsFrom(Collection<Integer> cards) {
    super.removeCardsFrom(cards);
    for (int i : traitIdxs)
      if (!cards.remove(i))
        throw new IllegalStateException("Trait cards not found");
  }

  @Override
  public void addToAction4(Action4 action4) {
    action4.addBoardTrade(this);
  }

  @Override
  public boolean isValid(BasePlayer player) {
    for (int n : traitIdxs)
      if (n < 0 || n >= player.getHand().size() || n == cardIdx)
        return false;

    return super.isValid(player);
  }

  public List<Integer> getTraitIdxs() {
    return traitIdxs;
  }


  /**
   * A BT is one of:
   *  [Natural]
   *  [Natural, Natural]
   *  [Natural, Natural, Natural]
   *  [Natural, Natural, Natural, Natural]
   */

  public static JsonSerializer serializer() { return new BoardTradeSerializer(); }
  private static class BoardTradeSerializer implements JsonSerializer<BoardTrade> {
    @Override
    public JsonElement serialize(BoardTrade boardTrade, Type type,
                                 JsonSerializationContext jsonSerializationContext) {
      JsonArray result = new JsonArray();
      result.add(new JsonPrimitive(boardTrade.cardIdx));
      for (int n : boardTrade.traitIdxs) result.add(new JsonPrimitive(n));
      return result;
    }
  }

  public static JsonDeserializer deserializer() { return new BoardTradeDeserializer(); }
  private static class BoardTradeDeserializer implements JsonDeserializer<BoardTrade> {
    @Override
    public BoardTrade deserialize(JsonElement jsonElement, Type type,
                                  JsonDeserializationContext jsonDeserializationContext)
            throws JsonParseException {
      JsonArray jArray = jsonElement.getAsJsonArray();
      List<Integer> traitsToAdd = new ArrayList<>();

      for (int i = 1; i < jArray.size(); i++)
        traitsToAdd.add(jArray.get(i).getAsInt());

      return new BoardTrade(jArray.get(0).getAsInt(), traitsToAdd);
    }
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    if (!super.equals(o)) return false;

    BoardTrade that = (BoardTrade) o;

    return getTraitIdxs().equals(that.getTraitIdxs());
  }

  @Override
  public int hashCode() {
    int result = super.hashCode();
    result = 31 * result + getTraitIdxs().hashCode();
    return result;
  }

  @Override
  public String toString() {
    return "BoardTrade{" +
            "cardIdx=" + cardIdx +
            ", traitIdxs=" + traitIdxs +
            '}';
  }
}