package ec.sri.einvoice.domain.service;

import ec.sri.einvoice.domain.model.Comprobante;
import ec.sri.einvoice.domain.model.InfoDocumento;
import ec.sri.einvoice.domain.model.InfoFactura;
import ec.sri.einvoice.domain.model.TotalImpuesto;
import java.math.BigDecimal;

public class TotalFacturaRule implements ValidationRule<Comprobante> {
  @Override
  public ValidationResult validate(Comprobante comprobante) {
    InfoDocumento infoDocumento = comprobante.infoDocumento();
    if (!(infoDocumento instanceof InfoFactura infoFactura)) {
      return ValidationResult.ok();
    }
    BigDecimal totalImpuestos = infoFactura.totalConImpuestos().stream()
        .map(TotalImpuesto::valor)
        .reduce(BigDecimal.ZERO, BigDecimal::add);

    BigDecimal esperado = infoFactura.totalSinImpuestos()
        //.subtract(infoFactura.totalDescuento())
        .add(totalImpuestos)
        .add(infoFactura.propina());

    if (infoFactura.importeTotal().compareTo(esperado) != 0) {
      return ValidationResult.error("El importe total no coincide con el resumen de impuestos");
    }
    return ValidationResult.ok();
  }
}
