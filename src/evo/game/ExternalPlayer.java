package evo.game;

import evo.game.choose.Action4;
import evo.game.feed.FeedResponse;
import evo.game.list.SpeciesList;

import java.util.List;

/**
 * Created by jackfriedson on 3/11/16.
 */
public interface ExternalPlayer {

  /**
   * Sends this {@link ExternalPlayer} information about its state at the start of the round.
   *
   * ASSUMPTION: external players do not modify/write to/mutate the internal state of the dealer
   *
   * @param myState a representation of this player's internal state
   */
  void start(int wh, BasePlayer myState);

  /**
   * Chooses actions to take with the cards in this {@link ExternalPlayer}'s hand, given info
   * about the {@link SpeciesList}s of the other players in the game
   *
   * @param playersBefore the list of {@link SpeciesList}s of the players that come before this one
   * @param playersAfter the list of {@link SpeciesList}s of the players that come after this one
   * @return an {@link Action4} representing the choices this {@link ExternalPlayer} wishes to make
   */
  Action4 choose(List<SpeciesList> playersBefore, List<SpeciesList> playersAfter);

  /**
   * Chooses a {@link SpeciesBoard} to feed, given pertinent game information such as the
   * boards boards belonging to all players in the game, and the number of tokens currently
   * at the {@link WaterHole}
   *
   * @param myState a representation of this player's internal state
   * @param otherBoards the {@link SpeciesList} of every player in the game
   * @param tokens the number of tokens at the {@link WaterHole}
   * @return the {@link FeedResponse} representing which {@link SpeciesBoard} to feed
   */
  FeedResponse feedNext(BasePlayer myState, List<SpeciesList> otherBoards, int tokens);

  void gameOver();

  String getName();
}
