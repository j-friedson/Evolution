package evo.game.list;

import com.google.gson.*;
import evo.Constants;
import evo.game.*;
import evo.game.choose.Action4;
import evo.game.compare.HighestScoreFirst;
import evo.game.IllegalResponseException;
import evo.json.JFactory;

import java.lang.reflect.Type;
import java.util.*;
import java.util.function.Predicate;

/**
 * Created by jackfriedson on 3/16/16.
 */
public class PlayerList extends EvoList<EvoPlayer> {

  public PlayerList() { super(false); }

  public PlayerList(List<EvoPlayer> players) { super(players, false); }

  public void clear() { contents.clear(); }

  /**
   * Directs each {@link EvoPlayer} in this list to take endTurn-of-turn actions
   *
   * @param deck The deck to draw from/discard to if needed
   */
  public void endTurn(Deck deck) {
    this.forEach(p -> p.endTurn(deck));
  }

  public void gameOver() { this.forEach(EvoPlayer::gameOver); }

  /**
   * Queries each {@link EvoPlayer} in this list for an {@link Action4} representing the
   * actions it would like to take with the cards in its hand. In the event that a player gives
   * an invalid response or times out, that player is kicked from the game (i.e. removed from this
   * list)
   *
   * @return the list of {@link Action4}s representing (in order) the actions each player in this
   *         list wishes to take
   */
  public List<Action4> getChoices() {
    List<Action4> result = new ArrayList<>();
    List<EvoPlayer> toKick = new ArrayList<>();

    //TODO: replace exception handling with validity checking
    for (EvoPlayer p : this) {
      try { result.add(p.choose(getBoardsBefore(p), getBoardsAfter(p))); }
      catch (IllegalResponseException e) {
        toKick.add(p);
        System.out.println("Kicked player " + p.getId() + " from the game");
        System.out.println(e.getMessage());
      }
    }

    toKick.forEach(this::remove);
    return result;
  }

  /**
   * Extracts the {@link SpeciesList} of each player in this {@link PlayerList}, effectively
   * mapping this list of {@link EvoPlayer}s to a list of {@link SpeciesList}s
   *
   * @return the list of {@link SpeciesList}s belonging to each member (in order) of this list
   */
  public List<SpeciesList> getAllBoards() {
    return Utils.map(contents, EvoPlayer::getBoards);
  }

  /**
   * Gets the list of {@link SpeciesList}s belonging to {@link EvoPlayer}s before the given
   * one
   *
   * @param p the {@link EvoPlayer} to check before
   * @return the list of {@link SpeciesList}s before the given {@link EvoPlayer}
   */
  private List<SpeciesList> getBoardsBefore(EvoPlayer p) {
    return getAllBoards().subList(0, indexOf(p));
  }

  /**
   * Gets the list of {@link SpeciesList}s belonging to {@link EvoPlayer}s after the given
   * one
   *
   * @param p the {@link EvoPlayer} to check after
   * @return the list of {@link SpeciesList}s after the given {@link EvoPlayer}
   */
  private List<SpeciesList> getBoardsAfter(EvoPlayer p) {
    int idx = indexOf(p);
    if (idx == size() - 1) return new ArrayList<>();
    else return getAllBoards().subList(idx + 1, size());
  }

  /**
   *
   * @return
   */
  public int totalCardsNeeded() {
    int numCards = 0;
    for (EvoPlayer ep : this) numCards += ep.cardsNeeded();
    return numCards;
  }

  /**
   * Directs every {@link EvoPlayer} in this {@link PlayerList} to modify its state as needed
   * for the beginning of the round. This includes adding a new board if there are currently none,
   * and drawing the required number of cards from the deck.
   *
   * @param deck the {@link Deck} to draw cards from
   * @return true on success, false if there are not enough cards left
   */
  public void startAll(int wh, Deck deck) {
    for (EvoPlayer ep : this) ep.start(wh, deck.draw(ep.cardsNeeded()));
  }

  /**
   * Queries each {@link EvoPlayer} in this {@link PlayerList} for its score, then compiles
   * them in sorted order (highest score first), and returns the list as a {@link String}
   *
   * @return the {@link String} representation of {@link PlayerScore}s in descending order
   */
  public String getGameResult() {
    List<PlayerScore> rankings = getRankings();
    StringBuilder result = new StringBuilder("\nGame Results\n");
    for (int i = 0; i < rankings.size(); i++) {
      result.append(Integer.toString(i + 1))
              .append(" ")
              .append(rankings.get(i).toString())
              .append("\n");
    }
    return result.toString();
  }

  /**
   *
   * @return
   */
  public List<PlayerScore> getRankings() {
    List<PlayerScore> rankings = new ArrayList<>();
    forEach(p -> rankings.add(p.getScoreObject()));
    rankings.sort(new HighestScoreFirst());
    return rankings;
  }

  /**
   * Removes the last player in this list and returns the new list
   *
   * @return this {@link PlayerList}, minus the last element
   */
  public PlayerList removeLast() {
    if (isEmpty()) throw new IndexOutOfBoundsException("List is empty");
    PlayerList result = this.makeCopy();
    result.remove(this.size() - 1);
    return result;
  }

  /**
   * Effect: feeds all {@link SpeciesBoard}s with the scavenger trait, starting at the given player
   *
   * @param player the {@link InternalPlayer} whose carnivore just ate
   * @param wh the {@link WaterHole} to feedNext from
   */
  public void feedScavengers(EvoPlayer player, WaterHole wh) {
    for (EvoPlayer p : this.thisPlayerFirst(player))
      p.actOnAllWith(s -> s.hasTrait(TraitName.SCAVENGER), i -> p.feedSpecies(i, wh));
  }

  /**
   * Effect: directs all {@link InternalPlayer}s in this {@link PlayerList} to have all of their fat-tissue
   *         {@link SpeciesBoard}s eat their stored bag
   */
  public void eatFromStorage() {
    for (EvoPlayer p : this)
      p.actOnAllWith(SpeciesBoard::isFat, p::eatStoredFood);
  }

  /**
   * Directs all {@link EvoPlayer}s in this {@link PlayerList} to feed all of their
   * long-neck boards, if possible
   *
   * @param wh the {@link WaterHole} to feed from
   */
  public void feedLongNecks(WaterHole wh) {
    for (EvoPlayer p : this)
      p.actOnAllWith(s -> s.hasTrait(TraitName.LONG_NECK), i -> p.feedSpecies(i, wh));
  }

  /**
   * Directs all {@link EvoPlayer}s in this {@link PlayerList} to increase the population of
   * their fertile boards by one
   */
  public void populateFertiles() {
    for (EvoPlayer p : this)
      p.actOnAllWith(s -> s.hasTrait(TraitName.FERTILE), i -> p.idxToBoard(i).increasePopulation());
  }

  /**
   * Gets this {@link PlayerList}, rotated so that the given {@link EvoPlayer} is the first
   * element in the list
   *
   * @param player the {@link EvoPlayer} to move to the front
   * @return the rotated {@link PlayerList}
   */
  public PlayerList thisPlayerFirst(EvoPlayer player) {
    int playerIdx = indexOf(player);
    PlayerList result = this.makeCopy();
    Collections.rotate(result.contents, -playerIdx);
    return result;
  }

  /**
   * Gets this {@link PlayerList}, rotated so that the given {@link EvoPlayer} is the last
   * element in the list
   *
   * @param player the {@link EvoPlayer} to move to the endTurn
   * @return the rotated {@link PlayerList}
   */
  public PlayerList thisPlayerLast(EvoPlayer player) {
    int playerIdx = indexOf(player);
    PlayerList result = this.makeCopy();
    Collections.rotate(result.contents, -playerIdx - 1);
    return result;
  }


  @Override
  public PlayerList makeCopy() {
    PlayerList result = new PlayerList();
    this.forEach(result::add);
    return result;
  }

  @Override
  public PlayerList filterBy(Predicate<EvoPlayer> pred) {
    PlayerList result = new PlayerList();
    this.forEach(p -> { if (pred.test(p)) result.add(p); });
    return result;
  }

  /**
   * Effect: rotates this {@link PlayerList}, moving the element at index 0 to index 1
   */
  public void rotate() { Collections.rotate(this.contents, -1); }

  /**
   * Gets the first element of this {@link PlayerList}
   *
   * @return the first element of this {@link PlayerList}
   */
  public EvoPlayer first() {
    if (this.isEmpty()) throw new IndexOutOfBoundsException("List is empty");
    return this.contents.get(0);
  }

  public static PlayerList fromExternalPlayers(List<ExternalPlayer> players) {
    PlayerList result = new PlayerList();
    for (int i = 0; i < players.size(); i++)
      result.add(EvoPlayer.builder().id(i + 1).external(players.get(i)).build());
    return result;
  }

  @Override
  public boolean isValid() {
    if (this.size() > Constants.MAX_NUM_PLAYERS)
      return false;

    Set<Integer> idSet = new HashSet<>();

    for (EvoPlayer player : this)
      if (!player.isValid() || !idSet.add(player.getId()))
        return false;

    return true;
  }

  public static JsonDeserializer deserializer() { return new PlayerListDeserializer(); }
  private static class PlayerListDeserializer implements JsonDeserializer<PlayerList> {
    @Override
    public PlayerList deserialize(JsonElement jsonElement, Type type,
                                  JsonDeserializationContext jsonDeserializationContext)
            throws JsonParseException {
      JsonArray jsonList = jsonElement.getAsJsonArray();
      PlayerList result = new PlayerList();
      for (JsonElement je : jsonList)
        result.add(JFactory.fromJson(je.getAsJsonArray(), EvoPlayer.class));
      if (!result.isValid()) throw new JsonParseException("Invalid PlayerList: " + result.toString());
      else return result;
    }
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    PlayerList that = (PlayerList) o;

    return contents.equals(that.contents);
  }

  @Override
  public int hashCode() {
    return contents.hashCode();
  }

  @Override
  public String toString() {
    List<Integer> result = new ArrayList<>();
    for (EvoPlayer p : this)
      result.add(p.getId());
    return result.toString();
  }
}
