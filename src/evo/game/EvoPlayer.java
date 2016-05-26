package evo.game;

import com.google.gson.*;
import evo.Constants;
import evo.game.choose.Action4;
import evo.game.feed.FeedResponse;
import evo.game.feed.IdxPair;
import evo.game.list.CardList;
import evo.game.list.PlayerList;
import evo.game.list.SpeciesList;
import evo.gui.Displayable;
import evo.gui.View;
import evo.json.JFactory;
import evo.json.JUtils;

import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.*;

/**
 * A class representation of a Player for the Evolution game.
 */
public class EvoPlayer extends BasePlayer implements Displayable {
  private final int id;
  private final ExternalPlayer external;

  private EvoPlayer(int id, ExternalPlayer external, SpeciesList boards, CardList hand, int bag) {
    super(boards, hand, bag);
    this.id = id;
    this.external = Objects.requireNonNull(external);
    if (!isValid()) throw new InvalidEvoElementException("Invalid EvoPlayer: " + toString());
  }

  /**
   * todo
   */
  public void gameOver() {
    external.gameOver();
  }

  /**
   * Directs this {@link EvoPlayer} to update its state for the beginning of a new turn,
   * including adding a new {@link SpeciesBoard} if it has none, and adding the given cards to
   * its hand.
   *
   * @param toAdd the list of cards to be added to this player's hand
   * @return true if this player updated its state successfully, false if the deck ran out
   */
  public void start(int wh, List<TraitCard> toAdd) {
    if (getBoards().isEmpty()) this.addNewSpecies();
    getHand().addAll(toAdd);
    external.start(wh, this);
  }

  /**
   * Queries this {@link EvoPlayer} to ask what actions it would like to take with the cards
   * in its hand, and which it would like to place at the {@link WaterHole}
   *
   * @param playersBefore the {@link SpeciesList} of each {@link EvoPlayer} that comes before
   *                      this one
   * @param playersAfter the {@link SpeciesList} of each {@link EvoPlayer} that comes after
   *                      this one
   * @return an {@link Action4} representing the actions this player would like to take
   */
  public Action4 choose(List<SpeciesList> playersBefore, List<SpeciesList> playersAfter)
          throws IllegalResponseException {
    Action4 response = queryExternal(() -> external.choose(playersBefore, playersAfter));
    if (!response.isValid(this))
      throw new IllegalResponseException("Illegal response: " + response.toJson().toString());
    else return response;
  }

  /**
   * Queries this {@link EvoPlayer} to ask which {@link SpeciesBoard} should be fed, if any
   *
   * @param otherPlayers the list of all {@link EvoPlayer}s in this game, rotated so that this
   *                       player is at the endTurn
   * @param tokens the number of tokens at the {@link WaterHole}
   * @return the {@link FeedResponse} representing this player's choose
   */
  public FeedResponse feedNext(PlayerList otherPlayers, int tokens) throws IllegalResponseException {
    FeedResponse response = queryExternal(() ->
            external.feedNext(this, otherPlayers.getAllBoards(), tokens));
    if (!response.isValid(this, otherPlayers))
      throw new IllegalResponseException("Illegal response: " + response.toJson().toString());
    else return response;
  }

  /**
   * Determines whether this {@link EvoPlayer} has been successfully auto-fed, or whether a query is
   * needed because there is more than one choose
   *
   * Effect: automatically feeds the appropriate {@link SpeciesBoard} if there is only once choose
   *
   * @param allPlayers the list of all {@link EvoPlayer}s in this game, rotated so that this
   *                       player is at the end
   * @param wh the {@link WaterHole} to feed from
   * @param deck the {@link Deck} to draw cards from if necessary
   * @return true if the feed step has been completed automatically
   */
  public boolean autoFeed(PlayerList allPlayers, WaterHole wh, Deck deck) {
    // Invariant: autofeed is only ever called when canFeed returns true, thus it is never the
    //            case that autofeed will be called when there are 0 species to be fed
    int hungryIdx = findUniqueIdx(i -> idxToBoard(i).canEatMore() || idxToBoard(i).canStoreMore());
    if (hungryIdx == -1) return false;
    if (hungryIdx < -1) throw new IllegalStateException("Invariant broken");
    SpeciesBoard hungry = idxToBoard(hungryIdx);

    if (hungry.canStoreMore()) return false;
    else if (hungry.isVeg()) {
      feedSpecies(hungryIdx, wh);
      return true;
    }
    else {
      PlayerList otherPlayers = allPlayers.removeLast();
      if (!hungry.canAttackAny(otherPlayers.getAllBoards()))
        throw new IllegalStateException("Invariant broken");

      List<IdxPair> attackables = Utils.getAllAttackables(hungry, otherPlayers.getAllBoards());
      if (attackables.size() != 1) return false;
      else {
        EvoPlayer victimOwner = otherPlayers.get(attackables.get(0).getPlayerIdx());
        if (feedCarnivore(hungryIdx, victimOwner, attackables.get(0).getSpeciesIdx(), wh, deck))
          allPlayers.feedScavengers(this, wh);
        return true;
      }
    }
  }

  /**
   * Queries this {@link EvoPlayer}'s corresponding {@link ExternalPlayer} to take some action,
   * and returns the response, or times out after a specified time interval (defined in Constants).
   *
   * @param call the {@link Callable} query to the {@link ExternalPlayer}
   * @param <T> the type of the response from the {@link ExternalPlayer}, must extend
   *            {@link PlayerResponse}
   * @return the {@link ExternalPlayer}'s response
   */
  private <T extends PlayerResponse> T queryExternal(Callable<T> call)
          throws IllegalResponseException {
    ExecutorService executor = Executors.newSingleThreadExecutor();
    Future<T> future = executor.submit(call);

    try { return future.get(Constants.MAX_RESPONSE_TIME, Constants.TIME_UNIT); }
    catch (TimeoutException e) { throw new IllegalResponseException("Player timeout"); }
    catch (InterruptedException | ExecutionException e) {
      e.printStackTrace();
      throw new IllegalResponseException("Other player exception");
    }
    finally { executor.shutdownNow(); }
  }

  /**
   * Directs the {@link ExternalPlayer} that corresponds to this {@link EvoPlayer} to update
   * its state
   */
  public void updateExternal(int wh) {
    external.start(wh, this);
  }

  /**
   * Calculates this {@link EvoPlayer}'s current score and returns a new {@link PlayerScore}
   * representing it
   *
   * @return a {@link PlayerScore} representing the score of this {@link EvoPlayer}
   */
  public PlayerScore getScoreObject() {
    return new PlayerScore(id, external.getName(), getScore());
  }

  @Override
  public boolean isValid() {
    return super.isValid() && id > 0 && external != null;
  }

  @Override
  public void display(View view) {
    view.displayPlayer(this);
  }

  public int getId() {
    return this.id;
  }

  /**
   * A Player+ is one of
   * - a regular Player
   * - a Player with a "cards" field:
   *   [["id",Natural+],
   *    ["species",LOS],
   *    ["bag",Natural]
   *    ["cards",LOC]]
   */

  private static final String ID_STR = "id";
  private static final int ID_IDX = 0;
  private static final String BRDS_STR = "species";
  private static final int BRDS_IDX = 1;
  private static final String BAG_STR = "bag";
  private static final int BAG_IDX = 2;
  private static final String HAND_STR = "cards";
  private static final int HAND_IDX = 3;


  public static JsonSerializer serializer() { return new EvoPlayerSerializer(); }
  private static class EvoPlayerSerializer implements JsonSerializer<EvoPlayer> {
    @Override
    public JsonElement serialize(EvoPlayer evoPlayer, Type type,
                                 JsonSerializationContext jsonSerializationContext) {
      JsonArray playerJson = new JsonArray();
      playerJson.add(JUtils.makeIntField(ID_STR, evoPlayer.getId()));
      playerJson.add(JUtils.makeListField(BRDS_STR, evoPlayer.getBoards()));
      playerJson.add(JUtils.makeIntField(BAG_STR, evoPlayer.getBag()));

      if (!evoPlayer.getHand().isEmpty())
        playerJson.add(JUtils.makeListField(HAND_STR, evoPlayer.getHand()));

      return playerJson;
    }
  }

  public static JsonDeserializer deserializer() { return new EvoPlayerDeserializer(); }
  private static class EvoPlayerDeserializer implements JsonDeserializer<EvoPlayer> {
    @Override
    public EvoPlayer deserialize(JsonElement jsonElement, Type type,
                                 JsonDeserializationContext jsonDeserializationContext)
            throws JsonParseException {

      JsonArray jsonPlayer = jsonElement.getAsJsonArray();
      EvoPlayerBuilder builder = builder();
      builder.id(JUtils.getIntFromField(jsonPlayer, ID_STR, ID_IDX));
      builder.boards(JFactory.fromJson(JUtils.getArrayFromField(jsonPlayer, BRDS_STR, BRDS_IDX),
              SpeciesList.class));
      builder.bag(JUtils.getIntFromField(jsonPlayer, BAG_STR, BAG_IDX));

      if (jsonPlayer.size() == 4)
        builder.hand(JFactory.fromJson(JUtils.getArrayFromField(jsonPlayer, HAND_STR, HAND_IDX),
                CardList.class));

      try { return builder.build(); }
      catch (InvalidEvoElementException e) { throw new JsonParseException(e); }
    }
  }

  public static EvoPlayerBuilder builder() { return new EvoPlayerBuilder(); }
  public static class EvoPlayerBuilder extends BasePlayerBuilder {
    private int id = Constants.MIN_ID;
    private ExternalPlayer external = new SillyPlayer();

    public EvoPlayer build() throws InvalidEvoElementException {
      return new EvoPlayer(id, external, boards, hand, bag);
    }

    public EvoPlayerBuilder id(int id) {
      this.id = id;
      return this;
    }

    public EvoPlayerBuilder external(ExternalPlayer external) {
      this.external = Objects.requireNonNull(external);
      return this;
    }

    public EvoPlayerBuilder boards(SpeciesBoard... species) {
      this.boards.addAll(Arrays.asList(species));
      return this;
    }

    public EvoPlayerBuilder boards(SpeciesList species) {
      this.boards = Objects.requireNonNull(species);
      return this;
    }

    public EvoPlayerBuilder boards(List<SpeciesBoard> species) {
      this.boards = new SpeciesList(Objects.requireNonNull(species));
      return this;
    }

    public EvoPlayerBuilder hand(CardList hand) {
      this.hand = Objects.requireNonNull(hand);
      return this;
    }

    public EvoPlayerBuilder hand(List<TraitCard> hand) {
      this.hand = new CardList(Objects.requireNonNull(hand));
      return this;
    }

    public EvoPlayerBuilder bag(int food) {
      this.bag = food;
      return this;
    }
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    if (!super.equals(o)) return false;

    EvoPlayer evoPlayer = (EvoPlayer) o;

    if (getId() != evoPlayer.getId()) return false;
    return external.getName().equals(evoPlayer.external.getName());
  }

  @Override
  public int hashCode() {
    int result = super.hashCode();
    result = 31 * result + getId();
    result = 31 * result + external.hashCode();
    return result;
  }

  @Override
  public String toString() {
    return "EvoPlayer{" +
            "id=" + id +
            ", external=" + external +
            ", base=" + super.toString() +
            '}';
  }
}
