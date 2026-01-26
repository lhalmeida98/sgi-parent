package ec.sri.einvoice.domain.service;

import ec.sri.einvoice.domain.model.Comprobante;

public class RucLengthRule implements ValidationRule<Comprobante> {
  @Override
  public ValidationResult validate(Comprobante comprobante) {
    String ruc = comprobante.infoTributaria().ruc();
    if (ruc == null || ruc.length() != 13) {
      return ValidationResult.error("RUC de emisor invalido");
    }
    for (int i = 0; i < ruc.length(); i++) {
      if (!Character.isDigit(ruc.charAt(i))) {
        return ValidationResult.error("RUC de emisor invalido");
      }
    }
    return ValidationResult.ok();
  }
}
