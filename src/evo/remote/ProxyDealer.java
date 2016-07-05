package evo.remote;

import com.google.gson.JsonParseException;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import evo.Constants;
import evo.game.*;
import evo.game.choose.Action4;
import evo.game.feed.FeedResponse;
import evo.game.list.CardList;
import evo.game.list.SpeciesList;
import evo.json.JFactory;
import evo.json.JUtils;

import java.io.*;
import java.net.Socket;
import java.util.List;

/**
 * Created by jackfriedson on 4/14/16.
 */
public class ProxyDealer {
  private final ExternalPlayer player;
  private final Socket socket;
  private final BufferedReader br;
  private final BufferedWriter bw;
  private JsonWriter jw;
  private JsonReader jr;

  public ProxyDealer(ExternalPlayer player, String host, int port) {
    this.player = player;

    try {
      socket = new Socket(host, port);
      DataOutputStream out = new DataOutputStream(socket.getOutputStream());
      DataInputStream in = new DataInputStream(socket.getInputStream());
      bw = new BufferedWriter(new OutputStreamWriter(out));
      br = new BufferedReader(new InputStreamReader(in));
      jw = new JsonWriter(bw);
      jr = new JsonReader(br);
      jr.setLenient(true);
      jw.setLenient(true);
      jw.setIndent(" ");
    }
    catch (IOException e) {
      System.out.println("Could not connect to " + host + " at port " + port);
      disconnect();
      System.exit(-1);
      throw new RuntimeException();
    }
  }

  /**
   * Effect: Plays the Evolution game, first sending a readSignup message and verifying the "ok"
   * message from the server, then waiting for a start-of-turn message, a choose message, and then
   * either a feed-next message or a start-of-turn message, repeating this cycle until the
   * connection to the server is no longer active
   */
  public void play() {
    try {
      signup();
      start();
      choose();
      while (streamIsLive())
        if (startOrFeedNext())
          choose();
    }
    catch (IOException e) { System.out.println(e.getMessage()); }
    finally { disconnect(); }
  }

  /**
   * Effect: Sends the readSignup message (i.e. this player's name) to the server, receives the "ok"
   * message. Disconnects if anything other than the string "ok" is received.
   *
   * @throws IOException
   */
  private void signup() throws IOException {
    jw.value(Constants.DEFAULT_NAME);
    jw.flush();
    String okMsg = jr.nextString();
    if (!okMsg.equals(Constants.OK_MSG)) { disconnect(); }
    System.out.println("Signed up successfully");
  }

  /**
   * Effect: Reads the start-of-turn message from the server, and updates the external player's
   * state according to the given state
   *
   * @throws IOException
   */
  private void start() throws IOException {
    BasePlayer.BasePlayerBuilder builder = BasePlayer.builder();
    int wh;

    jr.beginArray();
    wh = JUtils.getNat(jr);
    builder.bag(JUtils.getNat(jr));
    builder.boards(JFactory.fromJson(jr, SpeciesList.class));
    builder.hand(JFactory.fromJson(jr, CardList.class));
    jr.endArray();

    player.start(wh, builder.build());
  }

  /**
   * Effect: Reads the choose message from the server, queries the external player for card choices,
   * and sends them back to the server
   */
  private void choose() throws IOException {
    jr.beginArray();
    Action4 choice = player.choose(JUtils.readLOB(jr), JUtils.readLOB(jr));
    jr.endArray();
    choice.toJson(jw);
  }

  /**
   * Effect: Reads a start or feedNext message from the server and applies the appropriate actions,
   * i.e. updating the external player's state and/or querying the external player for a feed
   * choose
   *
   * @return true if this is a start message, false if it is a feedNext message
   */
  private boolean startOrFeedNext() throws IOException {
    BasePlayer.BasePlayerBuilder builder = BasePlayer.builder();
    jr.beginArray();
    int bagOrWH = JUtils.getNat(jr);

    switch (jr.peek()) {
      case NUMBER:
        builder.bag(JUtils.getNat(jr));
        builder.boards(JFactory.fromJson(jr, SpeciesList.class));
        builder.hand(JFactory.fromJson(jr, CardList.class));
        jr.endArray();

        player.start(bagOrWH, builder.build());
        return true;

      case BEGIN_ARRAY:
        builder.bag(bagOrWH);
        builder.boards(JFactory.fromJson(jr, SpeciesList.class));
        builder.hand(JFactory.fromJson(jr, CardList.class));
        int tokens = JUtils.getNat(jr);
        List<SpeciesList> lob = JUtils.readList(jr, SpeciesList.class);
        jr.endArray();

        FeedResponse response = player.feedNext(builder.build(), lob, tokens);
        response.toJson(jw);
        //todo: remove string "hack" (to send feedVeg and feedNone messages)
        return false;

      default:
        throw new JsonParseException("Could not interpret message");
    }
  }

  /**
   * Determines whether or not the connection to the server is active
   *
   * @return true if the stream is active, false if it has been closed (presumably from the
   *         server side)
   */
  private boolean streamIsLive() {
    try { return jr.peek() != JsonToken.END_DOCUMENT; }
    catch (IOException e) { return false; }
  }

  /**
   * Effect: disconnects this proxy from the server, closing all relevant streams and sockets
   */
  private void disconnect() {
    try {
      System.out.println("Disconnecting from server...");
      if (jr != null) jr.close();
      if (jw != null) jw.close();
      if (socket != null) socket.close();
    }
    catch (IOException | NullPointerException e) {
      System.out.println("Could not close connection to server");
      System.exit(-1);
    }
  }
}
