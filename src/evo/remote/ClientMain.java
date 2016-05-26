package evo.remote;

import evo.Constants;
import evo.game.SillyPlayer;

/**
 * Created by jackfriedson on 4/15/16.
 */
public class ClientMain {

  public static void main(String args[]) {
    String host = Constants.DEFAULT_HOST;
    int port = Constants.DEFAULT_PORT;

    if (args.length > 0) host = args[0];
    if (args.length > 1) port = Integer.valueOf(args[1]);
    if (port < 0 || port > Constants.MAX_PORT) throw new IllegalArgumentException("Invalid port number");

    ProxyDealer proxy = new ProxyDealer(new SillyPlayer(), host, port);
    proxy.play();
  }
}