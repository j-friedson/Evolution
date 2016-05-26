package evo;

import java.util.concurrent.TimeUnit;

/**
 * A file containing all game constants
 */
@SuppressWarnings("all")
public final class Constants {

  public static final String CHARSET = "UTF-8";

  public static final String DEFAULT_NAME = "jack";
  public static final String OK_MSG = "ok";
  public static final int MIN_ID = 1;

  public static final int DEFAULT_PORT = 45678;
  public static final String DEFAULT_HOST = "localhost";
  public static final int MAX_PORT = 65535;

  public static final int CARDS_PER_TURN = 3;
  public static final int CARDS_PER_BOARD = 2;

  public static final int MAX_RESPONSE_TIME = 10;
  public static final TimeUnit TIME_UNIT = TimeUnit.SECONDS;
  
  public static final long SIGNUP_DELAY = 3;
  public static final TimeUnit SIGNUP_UNIT = TimeUnit.SECONDS;

  public static final int DEFAULT_FOOD_VAL = 0;
  public static final int DEFAULT_WH_VAL = 0;

  public static final int MIN_POP_SIZE = 1;
  public static final int MAX_POP_SIZE = 7;
  public static final int MIN_BODY_SIZE = 0;
  public static final int MAX_BODY_SIZE = 7;
  public static final int MIN_FOOD_SIZE = 0;
  public static final int MIN_FAT_FOOD = 0;
  public static final int MAX_FAT_FOOD = 7;
  public static final int MAX_TRAITS_PER_BOARD = 3;

  public static final int MIN_FOOD_BAG = 0;

  public static final int MIN_NUM_PLAYERS = 3;
  public static final int MAX_NUM_PLAYERS = 8;

  public static final int MIN_CARN_FOODVAL = -8;
  public static final int MAX_CARN_FOODVAL = 8;
  public static final int MIN_VEG_FOODVAL = -3;
  public static final int MAX_VEG_FOODVAL = 3;
}
