package evo.gui;

import javax.swing.*;
import java.awt.*;

/**
 * Created by jackfriedson on 3/16/16.
 */
public abstract class EvolutionPanel extends JPanel {

  protected void drawField(Graphics g, String name, int value, int x, int y) {
    g.setColor(GuiConstants.TEXT_COLOR);
    g.drawString(name + Integer.toString(value), x, y);
  }
}
