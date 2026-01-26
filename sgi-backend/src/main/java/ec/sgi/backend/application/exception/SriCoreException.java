package ec.sgi.backend.application.exception;

public class SriCoreException extends RuntimeException {
  public SriCoreException(String message, Throwable cause) {
    super(message, cause);
  }

  public SriCoreException(String message) {
    super(message);
  }
}
