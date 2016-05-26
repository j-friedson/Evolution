package evo;

import evo.game.Dealer;
import evo.game.ExternalPlayer;
import evo.game.SillyPlayer;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by jackfriedson on 4/4/16.
 */
public class EvolutionMain {

  public static void main(String[] args) {
    if (args.length != 1) throw new IllegalArgumentException("Must specify a number of players");

    int numPlayers = Integer.valueOf(args[0]);
    if (numPlayers < Constants.MIN_NUM_PLAYERS || numPlayers > Constants.MAX_NUM_PLAYERS)
      throw new IllegalArgumentException("Number of players must be between "
              + Constants.MIN_NUM_PLAYERS + " and " + Constants.MAX_NUM_PLAYERS
              + " (inclusive)");

    Dealer dealer = Dealer.builder().players(makeSillyPlayers(numPlayers)).build();
    dealer.runGame();
  }

  private static List<ExternalPlayer> makeSillyPlayers(int numPlayers) {
    List<ExternalPlayer> result = new ArrayList<>(numPlayers);
    for (int i = 0; i < numPlayers; i++) result.add(new SillyPlayer());
    return result;
  }
}