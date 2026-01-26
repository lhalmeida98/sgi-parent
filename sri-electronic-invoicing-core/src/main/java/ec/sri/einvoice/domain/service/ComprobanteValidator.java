package ec.sri.einvoice.domain.service;

import ec.sri.einvoice.domain.exception.ValidationException;
import ec.sri.einvoice.domain.model.Comprobante;
import java.util.ArrayList;
import java.util.List;

public class ComprobanteValidator {
  private final List<ValidationRule<Comprobante>> rules;

  public ComprobanteValidator(List<ValidationRule<Comprobante>> rules) {
    this.rules = List.copyOf(rules);
  }

  public void validateOrThrow(Comprobante comprobante) {
    List<String> errores = new ArrayList<>();
    for (ValidationRule<Comprobante> rule : rules) {
      ValidationResult result = rule.validate(comprobante);
      if (!result.valid()) {
        errores.add(result.message());
      }
    }
    if (!errores.isEmpty()) {
      throw new ValidationException(errores);
    }
  }
}
