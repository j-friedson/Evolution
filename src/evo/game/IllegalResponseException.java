package evo.game;

/**
 * Created by jackfriedson on 4/6/16.
 */
public class IllegalResponseException extends IllegalStateException {

  public IllegalResponseException() {}

  public IllegalResponseException(String message) {
    super(message);
  }

  public IllegalResponseException(Throwable cause) {
    super(cause);
  }

  public IllegalResponseException(String message, Throwable cause) {
    super(message, cause);
  }
}
