package ec.sri.einvoice.domain.service;

import ec.sri.einvoice.domain.model.Comprobante;
import ec.sri.einvoice.domain.model.Detalle;
import ec.sri.einvoice.domain.model.InfoFactura;
import ec.sri.einvoice.domain.model.InfoTributaria;
import java.util.ArrayList;
import java.util.List;

public class AlphanumericWhitespaceRule implements ValidationRule<Comprobante> {
  @Override
  public ValidationResult validate(Comprobante comprobante) {
    List<String> errores = new ArrayList<>();
    InfoTributaria infoTributaria = comprobante.infoTributaria();
    validarCampo("razonSocial", infoTributaria.razonSocial(), errores);
    validarCampo("nombreComercial", infoTributaria.nombreComercial(), errores);
    validarCampo("dirMatriz", infoTributaria.dirMatriz(), errores);

    if (comprobante.infoDocumento() instanceof InfoFactura infoFactura) {
      validarCampo("dirEstablecimiento", infoFactura.dirEstablecimiento(), errores);
      validarCampo("razonSocialComprador", infoFactura.razonSocialComprador(), errores);
      validarCampo("identificacionComprador", infoFactura.identificacionComprador(), errores);
      validarCampo("moneda", infoFactura.moneda(), errores);
    }

    for (Detalle detalle : comprobante.detalles()) {
      validarCampo("codigoPrincipal", detalle.codigoPrincipal(), errores);
      validarCampo("descripcion", detalle.descripcion(), errores);
    }

    if (!errores.isEmpty()) {
      return ValidationResult.error(String.join(" | ", errores));
    }
    return ValidationResult.ok();
  }

  private void validarCampo(String nombre, String valor, List<String> errores) {
    if (valor == null) {
      return;
    }
    if (!valor.equals(valor.trim()) || valor.contains("  ")) {
      errores.add("Campo " + nombre + " contiene espacios invalidos");
    }
  }
}
