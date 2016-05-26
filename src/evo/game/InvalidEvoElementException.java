package evo.game;

/**
 * Created by jackfriedson on 4/27/16.
 */
public class InvalidEvoElementException extends IllegalStateException {

  public InvalidEvoElementException() {}
  public InvalidEvoElementException(String message) { super(message); }
  public InvalidEvoElementException(Throwable cause) {
    super(cause);
  }
  public InvalidEvoElementException(String message, Throwable cause) {
    super(message, cause);
  }
}
