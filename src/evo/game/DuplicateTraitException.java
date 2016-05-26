package evo.game;

/**
 * Created by jackfriedson on 3/31/16.
 */
public class DuplicateTraitException extends IllegalStateException {

  public DuplicateTraitException() {}
  public DuplicateTraitException(String message) {
    super(message);
  }
  public DuplicateTraitException(Throwable cause) {
    super(cause);
  }
  public DuplicateTraitException(String message, Throwable cause) {
    super(message, cause);
  }
}