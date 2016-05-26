package evo.remote;

import evo.Constants;
import evo.game.ExternalPlayer;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ScheduledFuture;

/**
 * Created by jackfriedson on 4/14/16.
 */
public class ServerMain {
  private static ScheduledFuture game = null;

  public static void main(String args[]) {
    int port = Constants.DEFAULT_PORT;
    List<ExternalPlayer> players = new ArrayList<>();

    if (args.length > 0) port = Integer.valueOf(args[0]);
    if (port < 0 || port > Constants.MAX_PORT) throw new IllegalArgumentException("Invalid port number");

    try (ServerSocket server = new ServerSocket(port)) {
      while (players.size() < Constants.MAX_NUM_PLAYERS) {
        Socket client = server.accept();
        Thread handler = new SignUpHandler(client, players, game);
        handler.start();
      }
    }
    catch (IOException e) {
      System.out.println("Error setting up server");
      System.exit(-1);
    }
  }
}