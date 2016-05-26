package evo.remote;

import evo.Constants;
import evo.game.Dealer;
import evo.game.ExternalPlayer;

import java.io.IOException;
import java.net.Socket;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;

/**
 * Created by jackfriedson on 4/15/16.
 */
public class SignUpHandler extends Thread {
  private final Socket client;
  private final List<ExternalPlayer> players;
  private ScheduledFuture game;

  public SignUpHandler(Socket client, List<ExternalPlayer> players, ScheduledFuture game) {
    this.client = client;
    this.players = players;
    this.game = game;

    setUncaughtExceptionHandler((t, e) -> {
      System.out.println("Uncaught exception: " + e.getMessage());
      e.printStackTrace();
      disconnect();
    });
  }

  @Override
  public void run() {
    try {
      ProxyPlayer proxy = new ProxyPlayer(client);
      if (proxy.readSignup()) {
        synchronized (players) {
          if (players.size() < Constants.MAX_NUM_PLAYERS && proxy.sendOK()) {
            players.add(proxy);
            System.out.println("Player " + players.size() + " joined the game");
            if (players.size() == Constants.MIN_NUM_PLAYERS) scheduleGameStart();
          }
        }
      }
    }
    catch (IOException e) { disconnect(); }
  }

  /**
   * Effect: schedules the game to start in the predetermined amount of time
   */
  private void scheduleGameStart() {
    ScheduledExecutorService schedExecutor = Executors.newSingleThreadScheduledExecutor();
    game = schedExecutor.schedule(() -> Dealer.builder().players(players).build().runGame(),
            Constants.SIGNUP_DELAY, Constants.SIGNUP_UNIT);

    System.out.println("Game will start in " + Constants.SIGNUP_DELAY + " " +
            Constants.SIGNUP_UNIT.toString().toLowerCase() + "...");

    schedExecutor.shutdown();
  }

  /**
   * Effect: disconnects from the client and closes all relevant resources
   */
  private void disconnect() {
    try { if (client != null) client.close(); }
    catch (IOException e) {
      System.out.println("Could not close client socket");
      System.exit(-1);
    }
  }
}
