package evo.game.feed;

/**
 * Created by jackfriedson on 3/11/16.
 */
public class FeedingException extends UnsupportedOperationException {

  public FeedingException() {}

  public FeedingException(String message) {
    super(message);
  }

  public FeedingException(Throwable cause) {
    super(cause);
  }

  public FeedingException(String message, Throwable cause) {
    super(message, cause);
  }
}
