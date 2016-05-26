package evo.remote;

import java.io.IOException;

/**
 * Created by jackfriedson on 4/22/16.
 */
public class SignUpException extends IOException {

  public SignUpException() {}

  public SignUpException(String message) {
    super(message);
  }

  public SignUpException(Throwable cause) {
    super(cause);
  }

  public SignUpException(String message, Throwable cause) {
    super(message, cause);
  }
}
