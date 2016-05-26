package evo.gui;


import evo.game.*;
import evo.game.list.SpeciesList;

import java.awt.*;

/**
 * Created by jackfriedson on 3/16/16.
 */
public class PlayerPanel extends EvolutionPanel {
  private final EvoPlayer ep;

  public PlayerPanel(EvoPlayer ep) {
    this.ep = ep;
  }

  @Override
  public void paintComponent(Graphics g) {
    super.paintComponent(g);
    drawID(g);
    drawFoodBag(g);
    drawAllBoards(g);
    drawHand(g);
  }

  @Override
  public Dimension getPreferredSize() {
    return new Dimension(GuiConstants.DEFAULT_PLAYER_WIDTH, GuiConstants.DEFAULT_PLAYER_HEIGHT);
  }

  private void drawID(Graphics g) {
    drawField(g, "Player ID: ", this.ep.getId(), GuiConstants.X_OFFSET,
            GuiConstants.lineOffsetY(1));
  }

  private void drawFoodBag(Graphics g) {
    drawField(g, "Food Bag: ", this.ep.getBag(), GuiConstants.X_OFFSET,
            GuiConstants.lineOffsetY(2));
  }

  private void drawAllBoards(Graphics g) {
    SpeciesList boards = this.ep.getBoards();

    for (int i = 0; i < boards.size(); i++) {
      drawSpeciesBoard(g, boards.get(i), GuiConstants.boardOffsetX(i), GuiConstants.BOARDS_OFFSET_Y);
    }
  }

  private void drawSpeciesBoard(Graphics g, SpeciesBoard s, int x, int y) {
    g.setColor(GuiConstants.RECT_COLOR);
    g.drawRect(x, y, GuiConstants.BOARD_WIDTH, GuiConstants.BOARD_HEIGHT);
    g.setColor(GuiConstants.TEXT_COLOR);

    int line = 1;
    drawField(g, "Pop: ", s.getPopulationSize(), x + GuiConstants.X_OFFSET,
            y + GuiConstants.lineOffsetY(line++));
    drawField(g, "Body: ", s.getBodySize(), x + GuiConstants.X_OFFSET,
            y + GuiConstants.lineOffsetY(line++));
    drawField(g, "Food: ", s.getFoodSupply(), x + GuiConstants.X_OFFSET,
            y + GuiConstants.lineOffsetY(line++));
    line++;

    for (TraitCard t : s.getTraitCards()) {
      String trait = t.getName().toString();

      if (t.getName() == TraitName.FAT_TISSUE)
        trait = trait + " (" + Integer.toString(s.getFatFood()) + ")";

      g.drawString(trait, x + GuiConstants.X_OFFSET, y + GuiConstants.lineOffsetY(line++));
    }
  }

  private void drawHand(Graphics g) {
    int x = GuiConstants.X_OFFSET;
    int y = GuiConstants.HAND_OFFSET;

    g.drawString("Hand: ", x, y - GuiConstants.X_OFFSET);

    for (TraitCard t : this.ep.getHand()) {
      String cardAsString = Integer.toString(t.getFoodPoints()) + " | " + t.getName().toString();
      int cardWidth = g.getFontMetrics().stringWidth(cardAsString) + GuiConstants.LINE_SPACING;

      g.setColor(GuiConstants.RECT_COLOR);
      g.drawRect(x, y, cardWidth, GuiConstants.CARD_HEIGHT);

      g.setColor(GuiConstants.TEXT_COLOR);
      g.drawString(cardAsString, x + GuiConstants.X_OFFSET, y + GuiConstants.lineOffsetY(1));

      x += cardWidth + GuiConstants.X_OFFSET;
    }
  }
}
