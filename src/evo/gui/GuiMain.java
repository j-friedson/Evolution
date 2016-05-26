package evo.gui;

import com.google.gson.stream.JsonReader;
import evo.game.Dealer;
import evo.game.EvoPlayer;
import evo.json.JFactory;

import java.io.InputStreamReader;

/**
 * Created by jackfriedson on 3/16/16.
 */
public class GuiMain {

  public static void main(String[] args) {
    JsonReader reader = new JsonReader(new InputStreamReader(System.in));
    Dealer dealer = JFactory.fromJson(reader, Dealer.class);
    EvoPlayer player1 = dealer.getPlayers().get(0);
    View view = new GuiView();
    dealer.display(view);
    player1.display(view);
  }
}
