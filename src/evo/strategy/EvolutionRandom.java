package evo.strategy;

import evo.game.Dealer;
import evo.game.ExternalPlayer;
import evo.Constants;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by jackfriedson on 4/26/16.
 */
public class EvolutionRandom {

  public static void main(String[] args) {
    if (args.length != 1) throw new IllegalArgumentException("Must specify a number of players");

    int numPlayers = Integer.valueOf(args[0]);
    if (numPlayers < Constants.MIN_NUM_PLAYERS || numPlayers > Constants.MAX_NUM_PLAYERS)
      throw new IllegalArgumentException("Number of players must be between "
              + Constants.MIN_NUM_PLAYERS + " and " + Constants.MAX_NUM_PLAYERS
              + " (inclusive)");

    Dealer dealer = Dealer.builder().players(makePlayers(numPlayers)).build();
    dealer.getDeck().shuffle();
    dealer.runGame();
  }

  private static List<ExternalPlayer> makePlayers(int numPlayers) {
    List<ExternalPlayer> result = new ArrayList<>(numPlayers);
    for (int i = 0; i < numPlayers; i++) result.add(new RandomPlayer());

    result.remove(1);
    result.add(1, new SmartPlayer());

    return result;
  }
}
