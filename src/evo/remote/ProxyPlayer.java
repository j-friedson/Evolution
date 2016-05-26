package evo.remote;

import com.google.gson.JsonArray;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import evo.Constants;
import evo.game.*;
import evo.game.choose.Action4;
import evo.game.feed.FeedNothing;
import evo.game.feed.FeedResponse;
import evo.game.feed.FeedVeg;
import evo.game.list.SpeciesList;
import evo.json.JFactory;
import evo.json.JUtils;

import java.io.*;
import java.net.Socket;
import java.util.List;
import java.util.concurrent.*;

/**
 * Created by jackfriedson on 4/14/16.
 */
public class ProxyPlayer implements ExternalPlayer {
  private final Socket socket;
  private final BufferedReader br;
  private final JsonReader jr;
  private final JsonWriter jw;
  private String name;

  public ProxyPlayer(Socket socket) throws IOException {
    this.socket = socket;
    DataInputStream in = new DataInputStream(socket.getInputStream());
    DataOutputStream out = new DataOutputStream(socket.getOutputStream());
    this.br = new BufferedReader(new InputStreamReader(in));
    this.jr = new JsonReader(br);
    this.jw = new JsonWriter(new BufferedWriter(new OutputStreamWriter(out)));
    this.jr.setLenient(true);
    this.jw.setLenient(true);
  }

  /**
   * todo
   * @return
   */
  public boolean sendOK() {
    try {
      jw.value(Constants.OK_MSG);
      jw.flush();
      return true;
    } catch (IOException e) { return false; }
  }

  /**
   * Reads the sign-up message sent by the client and returns it as a string, or times out after
   * the predetermined amount of time.
   *
   * @return the {@link String} sent by the player
   */
  public boolean readSignup() {
    ExecutorService executor = null;
    try {
      executor = Executors.newSingleThreadExecutor();
      Future<String> future = executor.submit(jr::nextString);
      name = future.get(Constants.MAX_RESPONSE_TIME, Constants.TIME_UNIT);
      return true;
    }
    catch (InterruptedException | TimeoutException | ExecutionException e) { return false; }
    finally { if (executor != null) executor.shutdownNow(); }
  }

  @Override
  public void start(int wh, BasePlayer myState) {
    try { JUtils.writeStartMsg(jw, wh, myState); }
    catch (IOException e) { throw new MessageSendFailure("Start: " + e); }
  }

  @Override
  public Action4 choose(List<SpeciesList> playersBefore, List<SpeciesList> playersAfter) {
    try {
      JUtils.writeChooseMsg(jw, playersBefore, playersAfter);
      return JFactory.fromJson(jr, Action4.class);
    }
    catch (IOException e) { throw new MessageSendFailure("Choose" + e); }
  }

  @Override
  public FeedResponse feedNext(BasePlayer myState, List<SpeciesList> otherBoards, int tokens) {
    try {
      JUtils.writeFeedNextMsg(jw, myState, otherBoards, tokens);
//      return JFactory.fromJson(jr, FeedResponse.class);
      //todo: fix FeedNone and FeedVeg bug (i.e. don't use hack)
      return readFeedResponse();
    }
    catch (IOException e) { throw new MessageSendFailure("Feed: " + e); }
  }

  @Override
  public void gameOver() { disconnect(); }

  @Override
  public String getName() { return name; }

  /**
   * Effect: disconnects from the client, closing all relevant resources
   */
  private void disconnect() {
    try {
      if (jr != null) jr.close();
      if (br != null) br.close();
      if (jw != null) jw.close();
      if (socket != null) socket.close();
    }
    catch (IOException e) {
      System.out.println("Could not close connection to client");
      System.exit(-1);
    }
  }


  /**
   * Reads a stream of JSON from the client that represents a {@link FeedResponse}, and converts it
   * to the correct data type.
   *
   * This separate method is necessary (instead of using our simple JsonReader based methods)
   * because one possible JSON representation of a FeedResponse is a 'top-level' JSON number.
   * Trying to read a number as a top-level value using Gson's JsonReader class will break the
   * reader, even when setLenient is true.
   *
   * @return the correct {@link FeedResponse} parsed from the JSON
   * @throws IOException
   */
  private FeedResponse readFeedResponse() throws IOException {
    br.mark(1);
    char c = (char)br.read();
    br.reset();

    switch (c) {
      case 'f':
        brSkip(5);
        return new FeedNothing();
      case '[':
        JsonArray result = new JsonArray();
        jr.beginArray();
        result.add(JUtils.getNat(jr));
        result.add(JUtils.getNat(jr));
        if (jr.hasNext()) result.add(JUtils.getNat(jr));
        jr.endArray();
        return JFactory.fromJson(result, FeedResponse.class);
      default:
        int n = Character.getNumericValue(c);
        brSkip(1);
        while (br.ready()) {
          n *= 10;
          n += Character.getNumericValue((char)br.read());
        }
        return new FeedVeg(n);
    }
  }

  /**
   * Effect: directs the BufferedReader to skip the next n characters in the stream
   *
   * @param n the number of characters to skip
   * @throws IOException
   */
  private void brSkip(int n) throws IOException {
    int i = 0;
    while (i < n)
      if (br.skip(1) == 1) i++;
  }
}