package evo.game.feed;

import com.google.gson.*;
import evo.game.*;
import evo.game.list.PlayerList;

import java.lang.reflect.Type;
import java.util.Optional;

/**
 * Created by jackfriedson on 3/22/16.
 */
public final class FeedCarn extends FeedResponse {
  private final int attackerIdx;
  private final int victimOwnerIdx;
  private final int victimIdx;

  public FeedCarn(int attackerIdx, int victimOwnerIdx, int victimIdx) {
    this.attackerIdx = attackerIdx;
    this.victimOwnerIdx = victimOwnerIdx;
    this.victimIdx = victimIdx;
  }

  @Override
  public boolean feedAction(EvoPlayer playerToFeed, PlayerList allPlayers, WaterHole wh, Deck deck) {
    if (playerToFeed.feedCarnivore(attackerIdx, allPlayers.get(victimOwnerIdx), victimIdx, wh, deck))
      allPlayers.feedScavengers(playerToFeed, wh);
    return true;
  }

  @Override
  public boolean isValid(EvoPlayer player, PlayerList allPlayers) {
    SpeciesBoard attacker;
    try { attacker = player.idxToBoard(attackerIdx); }
    catch (IllegalArgumentException e) { return false; }
    EvoPlayer victimOwner = allPlayers.get(victimOwnerIdx);
    SpeciesBoard victim;
    try { victim = victimOwner.idxToBoard(victimIdx); }
    catch (IllegalArgumentException e) { return false; }
    Optional<SpeciesBoard> leftNeighbor = victimOwner.leftNeighborOf(victimIdx);
    Optional<SpeciesBoard> rightNeighbor = victimOwner.rightNeighborOf(victimIdx);

    return attacker.isCarn() && attacker.canEatMore()
            && attacker.canAttack(victim, leftNeighbor, rightNeighbor);
  }


  /**
   * A CarnivoreChoice is [Natural, Natural, Natural].
   */

  public static JsonSerializer serializer() { return new FeedCarnSerializer(); }
  private static class FeedCarnSerializer implements JsonSerializer<FeedCarn> {
    @Override
    public JsonElement serialize(FeedCarn feedCarn, Type type,
                                 JsonSerializationContext jsonSerializationContext) {
      JsonArray carnivoreSpecies = new JsonArray();
      carnivoreSpecies.add(new JsonPrimitive(feedCarn.attackerIdx));
      carnivoreSpecies.add(new JsonPrimitive(feedCarn.victimOwnerIdx));
      carnivoreSpecies.add(new JsonPrimitive(feedCarn.victimIdx));
      return carnivoreSpecies;
    }
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    FeedCarn feedCarn = (FeedCarn) o;

    if (attackerIdx != feedCarn.attackerIdx) return false;
    if (victimOwnerIdx != feedCarn.victimOwnerIdx) return false;
    return victimIdx == feedCarn.victimIdx;

  }

  @Override
  public int hashCode() {
    int result = attackerIdx;
    result = 31 * result + victimOwnerIdx;
    result = 31 * result + victimIdx;
    return result;
  }

  @Override
  public String toString() {
    return "FeedCarn{" +
            "attackerIdx=" + attackerIdx +
            ", victimOwnerIdx=" + victimOwnerIdx +
            ", victimIdx=" + victimIdx +
            '}';
  }
}
