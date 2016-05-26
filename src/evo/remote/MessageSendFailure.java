package evo.remote;

/**
 * Created by jackfriedson on 4/15/16.
 */
public class MessageSendFailure extends UnsupportedOperationException {

  public MessageSendFailure() {}

  public MessageSendFailure(String message) {
    super(message);
  }

  public MessageSendFailure(Throwable cause) {
    super(cause);
  }

  public MessageSendFailure(String message, Throwable cause) {
    super(message, cause);
  }
}
