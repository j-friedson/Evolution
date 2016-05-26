package evo.game;

import com.google.gson.*;
import evo.game.choose.Action4;
import evo.game.feed.FeedResponse;
import evo.game.list.CardList;
import evo.game.list.PlayerList;
import evo.gui.Displayable;
import evo.gui.View;
import evo.json.JFactory;
import evo.json.JUtils;
import evo.strategy.GameData;

import java.lang.reflect.Type;
import java.util.*;

/**
 * A class representation of a Dealer for the Evolution game.
 */
public class Dealer extends EvoElement implements Displayable {
  private final PlayerList players;
  private final Deck deck;
  private PlayerList feedOrder;
  private final WaterHole wh;

  private Dealer(PlayerList players, Deck deck, PlayerList feedOrder, WaterHole wh)
          throws InvalidEvoElementException {
    this.players = players;
    this.deck = deck;
    this.feedOrder = feedOrder;
    this.wh = wh;
    if (!isValid()) throw new InvalidEvoElementException("Invalid Dealer: " + toString());
  }

  /**
   * todo
   */
  public void runGame() {
    System.out.println("Starting game...");

    while (!players.isEmpty() && deck.size() > players.totalCardsNeeded()) {
      players.startAll(wh.getFood(), deck);
      List<Action4> cardChoices = players.getChoices();
      step4(cardChoices);
      players.endTurn(deck);
    }

    gameOver();
  }


  /**
   * Effect: updates the current game according to the given list of {@link Action4}s, then
   *         carries out the feed round. The feed round includes:
   *          - feed all fat-tissue boards from storage at the beginning of the round
   *          - calling the feed1 method until there are no bag tokens left at the watering hole
   *            or all players cannot/don't want to continue feed
   *          - dealing cards to the players whose boards became extinct during this round
   *            of feed
   *
   * @param actions the list of {@link Action4}s that all {@link EvoPlayer}s wish to
   *                perform before the feed round starts
   */
  public void step4(List<Action4> actions) {
    for (int i = 0; i < players.size(); i++)
      actions.get(i).doAll(players.get(i), wh, deck);

    feedOrder = players.makeCopy();
    feedOrder.populateFertiles();
    feedOrder.feedLongNecks(wh);
    feedOrder.eatFromStorage();

    while (!wh.isEmpty() && !feedOrder.isEmpty())
      feed1();

    feedOrder.clear();
  }

  /**
   * Effect: Executes one step in the feed cycle. This includes:
   *          - deciding whether the dealer can transfer a token of bag (or several) from
   *            the watering hole to the current player automatically, or whether it is necessary
   *            to query said player
   *          - transfering bag, by interpreting the answer from a player if necessary
   *          - performing any other feed-related actions such as removing extinct boards
   *            boards and/or activating relevant traits (e.g. cooperation, scavenging)
   *          - removing the player from the sequence or rotating the sequence of players, as needed
   */
  public void feed1() {
    EvoPlayer playerToFeed = feedOrder.first();
    PlayerList rotatedPlayers = players.thisPlayerLast(playerToFeed);
    PlayerList otherPlayers = rotatedPlayers.removeLast();

    if (!playerToFeed.canFeed(otherPlayers)) feedOrder.remove(playerToFeed);
    else if (playerToFeed.autoFeed(rotatedPlayers, wh, deck)) feedOrder.rotate();
    else {
      try {
        FeedResponse playerResponse = playerToFeed.feedNext(otherPlayers, wh.getFood());
        if (!playerResponse.feedAction(playerToFeed, rotatedPlayers, wh, deck))
          feedOrder.remove(playerToFeed);
        else feedOrder.rotate();
      }
      catch (IllegalResponseException e) {
        feedOrder.remove(playerToFeed);
        players.remove(playerToFeed);
        System.out.println("Kicked player " + playerToFeed.getId() +
                " from the game: " + e.getMessage());
      }
    }
  }

  /**
   * todo
   */
  private void gameOver() {
    if (players.isEmpty()) {
      System.out.println("All players kicked from game");
      System.exit(0);
    }
    else {
      saveGameData();
      System.out.print(players.getGameResult());
      players.gameOver();
      System.exit(0);
    }
  }

  /**
   * todo
   */
  private void saveGameData() {
    GameData data = GameData.readDataFile();
    data.addData(players);
    data.saveToFile();
  }

  /**
   * Validates that this is a valid dealer configuration, according to the following constraints:
   *          - The list of players contains at least three and at most eight items
   *          - The player specifications may not contain extinct boards
   *          - The list of cards in a configuration together with the cards in possession of the
   *            players must make up a subset of the complete set of Evolution cards
   *
   * @return true if this is a valid dealer configuration
   */
  public boolean isValid() {
    Set<TraitCard> currentSet = new HashSet<>();
    currentSet.addAll(deck.getContents());
    for (EvoPlayer p : players)
      currentSet.addAll(p.getHand().getContents());

    Set<TraitCard> completeSet = new HashSet<>();
    Deck temp = new Deck();
    completeSet.addAll(temp.getContents());

    return completeSet.containsAll(currentSet);
  }

  /******************
   * Getter methods *
   ******************/
  public Deck getDeck() { return deck; }
  public List<TraitCard> getDeckContents() {
    return deck.getContents();
  }
  public PlayerList getPlayers() {
    return players.makeCopy();
  }
  public PlayerList getFeedOrder() {
    return feedOrder;
  }
  public WaterHole getWateringHole() { return wh; }
  public int getWHTokens() { return wh.getFood(); }

  @Override
  public void display(View view) { view.displayDealer(this); }


  /**
   * A Configuration is [LOP+, Natural, LOC].
   */

  private static final int LOP_IDX = 0;
  private static final int WH_IDX = 1;
  private static final int LOC_IDX = 2;

  public static JsonSerializer serializer() { return new EvoDealerSerializer(); }
  private static class EvoDealerSerializer implements JsonSerializer<Dealer> {
    @Override
    public JsonElement serialize(Dealer dealer, Type type,
                                 JsonSerializationContext jsonSerializationContext) {
      JsonArray config = new JsonArray();
      config.add(JUtils.listToJArray(dealer.getPlayers()));
      config.add(new JsonPrimitive(dealer.wh.getFood()));
      config.add(JUtils.listToJArray(dealer.deck.getContents()));
      return config;
    }
  }

  public static JsonDeserializer deserializer() { return new EvoDealerDeserializer(); }
  private static class EvoDealerDeserializer implements JsonDeserializer<Dealer> {
    @Override
    public Dealer deserialize(JsonElement jsonElement, Type type,
                              JsonDeserializationContext jsonDeserializationContext)
            throws JsonParseException {
      JsonArray configuration = jsonElement.getAsJsonArray();
      DealerBuilder builder = new DealerBuilder();
      builder.players(JFactory.fromJson(configuration.get(LOP_IDX).getAsJsonArray(),
              PlayerList.class));
      builder.wh(new WaterHole(configuration.get(WH_IDX).getAsInt()));
      builder.deck(new Deck(JFactory.fromJson(configuration.get(LOC_IDX).getAsJsonArray(),
              CardList.class)));

      try { return builder.build(); }
      catch (InvalidEvoElementException e) { throw new JsonParseException(e); }
    }
  }

  public static DealerBuilder builder() { return new DealerBuilder(); }
  public static class DealerBuilder {
    private PlayerList players = new PlayerList();
    private Deck deck = new Deck();
    private Optional<PlayerList> feedOrder = Optional.empty();
    private WaterHole wh = new WaterHole();

    public Dealer build() throws InvalidEvoElementException {
      if (feedOrder.isPresent()) return new Dealer(players, deck, feedOrder.get(), wh);
      else return new Dealer(players, deck, players.makeCopy(), wh);
    }

    public DealerBuilder players(PlayerList players) {
      this.players = Objects.requireNonNull(players);
      return this;
    }

    public DealerBuilder players(List<ExternalPlayer> players) {
      this.players = PlayerList.fromExternalPlayers(Objects.requireNonNull(players));
      return this;
    }

    public DealerBuilder deck(Deck deck) {
      this.deck = Objects.requireNonNull(deck);
      return this;
    }

    public DealerBuilder order(PlayerList order) {
      this.feedOrder = Optional.of(Objects.requireNonNull(order));
      return this;
    }

    public DealerBuilder wh(WaterHole wh) {
      this.wh = Objects.requireNonNull(wh);
      return this;
    }
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    Dealer dealer = (Dealer) o;

    if (!getPlayers().equals(dealer.getPlayers())) return false;
    if (!getDeck().equals(dealer.getDeck())) return false;
    if (!getFeedOrder().equals(dealer.getFeedOrder())) return false;
    return wh.equals(dealer.wh);

  }

  @Override
  public int hashCode() {
    int result = getPlayers().hashCode();
    result = 31 * result + getDeck().hashCode();
    result = 31 * result + getFeedOrder().hashCode();
    result = 31 * result + wh.hashCode();
    return result;
  }

  @Override
  public String toString() {
    return "Dealer{" +
            "players=" + players +
            ", deck=" + deck +
            ", feedOrder=" + feedOrder +
            ", wh=" + wh +
            '}';
  }
}