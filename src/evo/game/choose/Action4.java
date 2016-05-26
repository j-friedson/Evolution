package evo.game.choose;

import com.google.gson.*;
import com.google.gson.stream.JsonWriter;
import evo.game.*;
import evo.game.list.CardList;
import evo.json.JFactory;
import evo.json.JUtils;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.*;

/**
 * Created by jackfriedson on 3/31/16.
 */
public class Action4 implements PlayerResponse {
  private final int foodCardIdx;
  private final List<BoardTrade> bts;
  private final List<ReplaceTrait> rts;
  private final List<GrowPop> gps;
  private final List<GrowBody> gbs;


  public Action4(int foodCardIdx) {
    this.foodCardIdx = foodCardIdx;
    this.bts = new ArrayList<>();
    this.rts = new ArrayList<>();
    this.gps = new ArrayList<>();
    this.gbs = new ArrayList<>();
  }

  public Action4(int foodCardIdx, List<GrowPop> gps, List<GrowBody> gbs, List<BoardTrade> bts,
                 List<ReplaceTrait> rts) {
    this.foodCardIdx = foodCardIdx;
    this.gps = Objects.requireNonNull(gps);
    this.gbs = Objects.requireNonNull(gbs);
    this.bts = Objects.requireNonNull(bts);
    this.rts = Objects.requireNonNull(rts);
  }

  public void addBoardTrade(BoardTrade toAdd) { this.bts.add(toAdd); }
  public void addReplaceTrait(ReplaceTrait toAdd) { this.rts.add(toAdd); }
  public void addGrowPop(GrowPop toAdd) { this.gps.add(toAdd); }
  public void addGrowBody(GrowBody toAdd) { this.gbs.add(toAdd); }

  /**
   * Effect: carries out the game-modifying actions represented by this {@link Action4}. This
   *         includes adding the bag card to the {@link WaterHole}, and modifying the
   *         {@link EvoPlayer} as needed
   *
   * @param player the {@link EvoPlayer} to modify
   * @param wh the {@link WaterHole} to add bag to
   * @param deck the {@link Deck} to discard to if needed
   */
  public void doAll(EvoPlayer player, WaterHole wh, Deck deck) {
    CardList newHand = player.getHand().makeCopy();

    foodToWH(player, newHand, wh);
    for (BoardTrade bt : bts) bt.doAction(player, newHand);
    for (ReplaceTrait rt : rts) rt.doAction(player, newHand);
    for (GrowPop gp : gps) gp.doAction(player, newHand);
    for (GrowBody gb : gbs) gb.doAction(player, newHand);

    player.updateHand(newHand);
  }

  private void foodToWH(EvoPlayer player, CardList newHand, WaterHole wh) {
    TraitCard toPlay = player.idxToCard(foodCardIdx);
    wh.addFood(toPlay.getFoodPoints());
    newHand.remove(toPlay);
  }

  /**
   *
   * @param speciesIdx
   * @param trait
   * @param hand
   * @return
   */
  public boolean hasReplaceTrait(int speciesIdx, TraitName trait, CardList hand) {
    for (ReplaceTrait rt : rts)
      if (rt.getSpeciesIdx() == speciesIdx && hand.get(rt.getCardIdx()).getName() == trait)
        return true;
    return false;
  }

  /**
   * Verifies that this {@link Action4} is valid, given the {@link EvoPlayer} that it
   * corresponds to. Ensures that all indices referring to cards in the player's hand are unique
   * (i.e. no card is used twice) and that all designated {@link CardAction}s are valid.
   *
   * @param player the {@link EvoPlayer} that this {@link Action4} corresponds to
   * @return true if this is a valid {@link Action4}
   */
  public boolean isValid(EvoPlayer player) {
    List<CardAction> allActions = new ArrayList<>();
    allActions.addAll(bts);
    allActions.addAll(rts);
    allActions.addAll(gps);
    allActions.addAll(gbs);

    Set<Integer> handIdxs = new HashSet<>();
    if (!handIdxs.add(foodCardIdx) || foodCardIdx < 0 || foodCardIdx >= player.getHand().size())
      return false;

    for (CardAction ca : allActions)
      if (!handIdxs.add(ca.getCardIdx()) || !ca.isValid(player)) return false;

    int numBoards = player.getBoards().size() + bts.size();

    for (ReplaceTrait rt : rts) {
      if (rt.getSpeciesIdx() < 0 || rt.getSpeciesIdx() >= numBoards)
        return false;

      for (ReplaceTrait other : rts) {
        if (!rt.equals(other) && rt.getSpeciesIdx() == other.getSpeciesIdx() &&
                player.idxToCard(rt.getCardIdx()).getName() ==
                        player.idxToCard(other.getCardIdx()).getName())
          return false;
      }
    }

    for (GrowPop gp : gps)
      if (gp.getSpeciesIdx() < 0 || gp.getSpeciesIdx() >= numBoards)
        return false;

    for (GrowBody gb : gbs)
      if (gb.getSpeciesIdx() < 0 || gb.getSpeciesIdx() >= numBoards)
        return false;

    return true;
  }


  /**
   * An Action4 is [Natural, [GP, ...], [GB, ...], [BT, ...], [RT, ...]].
   */

  private static final int FC_IDX = 0;
  private static final int GP_IDX = 1;
  private static final int GB_IDX = 2;
  private static final int BT_IDX = 3;
  private static final int RT_IDX = 4;


  @Override
  public JsonElement toJson() {
    return JFactory.getGson().toJsonTree(this, this.getClass());
  }

  @Override
  public void toJson(JsonWriter writer) throws IOException {
    JFactory.getGson().toJson(this, this.getClass(), writer);
    writer.flush();
  }

  public static JsonSerializer serializer() { return new Action4Serializer(); }
  private static class Action4Serializer implements JsonSerializer<Action4> {
    @Override
    public JsonElement serialize(Action4 action4, Type type,
                                 JsonSerializationContext jsonSerializationContext) {
      JsonArray result = new JsonArray();
      result.add(new JsonPrimitive(action4.foodCardIdx));
      result.add(JUtils.listToJArray(action4.gps));
      result.add(JUtils.listToJArray(action4.gbs));
      result.add(JUtils.listToJArray(action4.bts));
      result.add(JUtils.listToJArray(action4.rts));
      return result;
    }
  }

  public static JsonDeserializer deserializer() { return new Action4Deserializer(); }
  private static class Action4Deserializer implements JsonDeserializer<Action4> {
    @Override
    public Action4 deserialize(JsonElement jsonElement, Type type,
                               JsonDeserializationContext jsonDeserializationContext)
            throws JsonParseException {
      JsonArray jsonAction4 = jsonElement.getAsJsonArray();
      Action4 result = new Action4(jsonAction4.get(FC_IDX).getAsInt());
      for (JsonElement je : jsonAction4.get(GP_IDX).getAsJsonArray())
        result.addGrowPop(JFactory.fromJson(je, GrowPop.class));
      for (JsonElement je : jsonAction4.get(GB_IDX).getAsJsonArray())
        result.addGrowBody(JFactory.fromJson(je, GrowBody.class));
      for (JsonElement je : jsonAction4.get(BT_IDX).getAsJsonArray())
        result.addBoardTrade(JFactory.fromJson(je, BoardTrade.class));
      for (JsonElement je : jsonAction4.get(RT_IDX).getAsJsonArray())
        result.addReplaceTrait(JFactory.fromJson(je, ReplaceTrait.class));
      return result;
    }
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    Action4 action4 = (Action4) o;

    if (foodCardIdx != action4.foodCardIdx) return false;
    if (!bts.equals(action4.bts)) return false;
    if (!rts.equals(action4.rts)) return false;
    if (!gps.equals(action4.gps)) return false;
    return gbs.equals(action4.gbs);

  }

  @Override
  public int hashCode() {
    int result = foodCardIdx;
    result = 31 * result + bts.hashCode();
    result = 31 * result + rts.hashCode();
    result = 31 * result + gps.hashCode();
    result = 31 * result + gbs.hashCode();
    return result;
  }

  @Override
  public String toString() {
    return "Action4{" +
            "foodCardIdx=" + foodCardIdx +
            ", bts=" + bts +
            ", rts=" + rts +
            ", gps=" + gps +
            ", gbs=" + gbs +
            '}';
  }
}