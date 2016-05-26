package evo.gui;


import evo.game.Dealer;
import evo.game.EvoPlayer;

/**
 * Created by jackfriedson on 4/5/16.
 */
public interface View {

  void displayDealer(Dealer toDisplay);
  void displayPlayer(EvoPlayer toDisplay);

  void update();
}
