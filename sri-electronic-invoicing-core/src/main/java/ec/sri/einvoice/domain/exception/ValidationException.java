package ec.sri.einvoice.domain.exception;

import java.util.List;

public class ValidationException extends DomainException {
  private final List<String> errors;

  public ValidationException(List<String> errors) {
    super("VALIDATION_ERROR", String.join("; ", errors));
    this.errors = List.copyOf(errors);
  }

  public List<String> errors() {
    return errors;
  }
}
