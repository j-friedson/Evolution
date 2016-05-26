package evo.gui;

import evo.game.Dealer;
import evo.game.EvoPlayer;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by jackfriedson on 4/5/16.
 */
public class GuiView implements View {
  private EvolutionWindow mainWindow;
  private List<EvolutionWindow> playerWindows = new ArrayList<>();

  @Override
  public void displayDealer(Dealer dealer) {
    mainWindow = new EvolutionWindow(new DealerPanel(dealer));
    mainWindow.draw();
  }

  @Override
  public void displayPlayer(EvoPlayer player) {
    EvolutionWindow pWindow = new EvolutionWindow(new PlayerPanel(player));
    playerWindows.add(pWindow);
    pWindow.draw();
  }

  @Override
  public void update() {
    mainWindow.repaint();
    playerWindows.forEach(EvolutionWindow::repaint);
  }
}
