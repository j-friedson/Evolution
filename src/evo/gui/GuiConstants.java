package evo.gui;

import java.awt.*;

/**
 * Created by jackfriedson on 3/16/16.
 */
public class GuiConstants {
  public static final int DEFAULT_PLAYER_WIDTH = 500;
  public static final int DEFAULT_PLAYER_HEIGHT = 275;

  public static final int DEFAULT_DEALER_WIDTH = 275;
  public static final int DEFAULT_DEALER_HEIGHT = 150;

  public static final Color BG_COLOR = Color.white;
  public static final Color TEXT_COLOR = Color.black;
  public static final Color RECT_COLOR = Color.black;

  public static final int BOARDS_OFFSET_Y = lineOffsetY(3);
  public static final int BOARD_WIDTH = 100;
  public static final int BOARD_HEIGHT = lineOffsetY(8);

  public static final int HAND_OFFSET = BOARDS_OFFSET_Y + BOARD_HEIGHT + lineOffsetY(2);
  public static final int CARD_HEIGHT = 20;

  public static final int X_OFFSET = 5;
  public static final int LINE_SPACING = 10;


  public static int lineOffsetY(int lineNum) {
    return lineNum * (X_OFFSET + LINE_SPACING);
  }

  public static int boardOffsetX(int boardNum) {
    return X_OFFSET + boardNum * (X_OFFSET + BOARD_WIDTH);
  }
}
