package evo;

import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import evo.game.Dealer;
import evo.game.EvoPlayer;
import evo.game.IllegalResponseException;
import evo.game.SpeciesBoard;
import evo.game.choose.Action4;
import evo.game.feed.FeedResponse;
import evo.game.list.PlayerList;
import evo.game.list.SpeciesList;
import evo.json.JFactory;
import evo.json.JUtils;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Optional;

/**
 * Created by jackfriedson on 5/10/16.
 */
public class TestHarnessMain {

  public static void main(String[] args) {
    if (args.length != 1) throw new IllegalArgumentException("Must specify the type of harness");

    String harnessType = args[0];
    JsonReader reader = new JsonReader(new InputStreamReader(System.in));

    try {
      switch (harnessType) {
        case "attack":
          JsonPrimitive output = new JsonPrimitive(interpretSituation(reader));
          System.out.println(output);
          break;
        case "feed":
          System.out.println(interpretFeeding(reader).toJson());
          break;
        case "step":
          System.out.println(interpretFeed1(reader).toJson());
          break;
        case "step4":
          System.out.println(interpretStep4(reader).toJson());
          break;
        case "choose":
          Action4 playerResponse = interpretChoice(reader);
          System.out.println(playerResponse.toJson());
          break;
        default:
          throw new IllegalArgumentException("Must specify a valid test harness type");
      }
    }
    catch (IllegalResponseException e) {
      System.exit(0);
    }
  }

  /**
   * A Choice is [Player+,[LOS,...,LOS],[LOS,...,LOS]].
   *
   * A Choice [p, before, after] represents a player p whose choose is then called with the arguments
   * before and after. Recall that the first argument to choose represents the players that take
   * precede p in the current turn order, and the second one are those that follow.
   */
  private static Action4 interpretChoice(JsonReader reader) throws IllegalResponseException {
    try {
      reader.beginArray();
      EvoPlayer playerToChoose = JFactory.fromJson(reader, EvoPlayer.class);
      playerToChoose.updateExternal(Constants.DEFAULT_WH_VAL);
      List<SpeciesList> before = JUtils.readList(reader, SpeciesList.class);
      List<SpeciesList> after = JUtils.readList(reader, SpeciesList.class);
      reader.endArray();
      return playerToChoose.choose(before, after);
    } catch (IOException e) { throw new JsonParseException(e); }
  }

  /**
   * A Step4 is [Action4, ..., Action4].
   *
   * Each Action4 specifies what the corresponding player object wishes to do with its cards.
   */
  private static Dealer interpretStep4(JsonReader reader) {
    try {
      reader.beginArray();
      Dealer dealer = JFactory.fromJson(reader, Dealer.class);
      List<Action4> action4List = JUtils.readList(reader, Action4.class);
      dealer.step4(action4List);
      reader.endArray();
      return dealer;
    } catch (IOException e) { throw new JsonParseException(e); }
  }

  /**
   * A Configuration is [LOP+, Natural, LOC].
   *
   * The list of players describes all players participating in this game. It also specifies the
   * order in which they take turns feed their species.
   *
   * The natural number is the number of food tokens at the watering hole.
   *
   * The list of cards is the remaining deck of cards; the cards are handed out in this order.
   */
  private static Dealer interpretFeed1(JsonReader reader) {
    Dealer dealer = JFactory.fromJson(reader, Dealer.class);
    dealer.feed1();
    return dealer;
  }

  /**
   * A Feeding is [Player, Natural+, LOP]. The natural number in the middle specifies how many
   * tokens of food are left at the watering hole.
   */
  private static FeedResponse interpretFeeding(JsonReader reader) throws IllegalResponseException {
    try {
      reader.beginArray();
      EvoPlayer playerToFeed = JFactory.fromJson(reader, EvoPlayer.class);
      int whTokens = reader.nextInt();
      PlayerList otherPlayers = JFactory.fromJson(reader, PlayerList.class);
      reader.endArray();
      return playerToFeed.feedNext(otherPlayers, whTokens);
    } catch (IOException e) { throw new JsonParseException(e); }
  }

  /**
   * A Situation is [Species, Species, OptSpecies, OptSpecies]
   */
  private static boolean interpretSituation(JsonReader reader) {
    try {
      reader.beginArray();
      SpeciesBoard defender = JFactory.fromJson(reader, SpeciesBoard.class);
      SpeciesBoard attacker = JFactory.fromJson(reader, SpeciesBoard.class);
      Optional<SpeciesBoard> left = optSpeciesFromJson(reader);
      Optional<SpeciesBoard> right = optSpeciesFromJson(reader);
      reader.endArray();
      return attacker.canAttack(defender, left, right);
    } catch (IOException e) { throw new JsonParseException(e); }
  }

  /**
   * Interprets a JSON "OptSpecies" from a {@link JsonReader} and creates an
   * {@link Optional<SpeciesBoard>} that represents it.
   *
   * An OptSpecies is one of:
   * - false
   * - Species+
   *
   * @param reader the {@link JsonReader} to create the new object from
   * @return an {@link Optional<SpeciesBoard>}
   * @throws IOException
   */
  private static Optional<SpeciesBoard> optSpeciesFromJson(JsonReader reader) throws IOException {
    if (reader.peek() == JsonToken.BEGIN_ARRAY)
      return Optional.of(JFactory.fromJson(reader, SpeciesBoard.class));
    else if (!reader.nextBoolean())
      return Optional.empty();
    else throw new JsonParseException("Illegal OptSpecies");
  }
}
