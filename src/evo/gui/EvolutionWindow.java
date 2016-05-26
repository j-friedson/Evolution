package evo.gui;

import javax.swing.*;
import java.awt.*;

/**
 * Created by jackfriedson on 3/16/16.
 */
public class EvolutionWindow extends JFrame {

  public EvolutionWindow(EvolutionPanel panel) {
    this.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    JScrollPane scrollPane = new JScrollPane(panel);
    scrollPane.setPreferredSize(panel.getPreferredSize());
    this.getContentPane().add(scrollPane, BorderLayout.CENTER);
    this.pack();
  }

  public void draw() { this.setVisible(true); }
}
