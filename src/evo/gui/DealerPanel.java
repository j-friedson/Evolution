package evo.gui;


import evo.game.Dealer;

import java.awt.*;

/**
 * Created by jackfriedson on 3/16/16.
 */
public class DealerPanel extends EvolutionPanel {
  private Dealer ed;

  public DealerPanel(Dealer ed) {
    this.ed = ed;
  }

  @Override
  public void paintComponent(Graphics g) {
    super.paintComponent(g);

    int line = 1;
    drawTitle(g, line++);
    line++;
    drawDeck(g, line++);
    drawWaterHole(g, line++);
    drawPlayerOrder(g, line++);
    drawFeedingOrder(g, line++);
  }

  @Override
  public Dimension getPreferredSize() {
    return new Dimension(GuiConstants.DEFAULT_DEALER_WIDTH, GuiConstants.DEFAULT_DEALER_HEIGHT);
  }

  private void drawTitle(Graphics g, int line) {
    g.drawString("Dealer", GuiConstants.X_OFFSET, GuiConstants.lineOffsetY(line));
  }

  private void drawDeck(Graphics g, int line) {
    drawField(g, "Deck: ", this.ed.getDeck().size(), GuiConstants.X_OFFSET,
            GuiConstants.lineOffsetY(line));
  }

  private void drawWaterHole(Graphics g, int line) {
    drawField(g, "Watering hole: ", this.ed.getWHTokens(), GuiConstants.X_OFFSET,
            GuiConstants.lineOffsetY(line));
  }

  private void drawPlayerOrder(Graphics g, int line) {
    String playersAsString = "Players: " + ed.getPlayers().toString();
    g.drawString(playersAsString, GuiConstants.X_OFFSET, GuiConstants.lineOffsetY(line));
  }

  private void drawFeedingOrder(Graphics g, int line) {
    String feedOrderAsString = "Current feedNext order: " + ed.getFeedOrder().toString();
    g.drawString(feedOrderAsString, GuiConstants.X_OFFSET, GuiConstants.lineOffsetY(line));
  }
}
