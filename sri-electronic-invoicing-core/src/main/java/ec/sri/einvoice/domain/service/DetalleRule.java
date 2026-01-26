package ec.sri.einvoice.domain.service;

import ec.sri.einvoice.domain.model.Comprobante;

public class DetalleRule implements ValidationRule<Comprobante> {
  @Override
  public ValidationResult validate(Comprobante comprobante) {
    if (comprobante.detalles().isEmpty()) {
      return ValidationResult.error("El comprobante debe tener al menos un detalle");
    }
    return ValidationResult.ok();
  }
}
