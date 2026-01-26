package ec.sri.einvoice.domain;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import ec.sri.einvoice.domain.exception.ValidationException;
import ec.sri.einvoice.domain.model.Comprobante;
import ec.sri.einvoice.domain.model.InfoFactura;
import ec.sri.einvoice.domain.service.AlphanumericWhitespaceRule;
import ec.sri.einvoice.domain.service.ComprobanteValidator;
import ec.sri.einvoice.domain.service.DetalleRule;
import ec.sri.einvoice.domain.service.RucLengthRule;
import ec.sri.einvoice.domain.service.TotalFacturaRule;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;

class ComprobanteValidatorTest {
  @Test
  void validaComprobanteCorrecto() {
    Comprobante comprobante = ComprobanteTestData.facturaValida();
    ComprobanteValidator validator = new ComprobanteValidator(List.of(
        new RucLengthRule(),
        new AlphanumericWhitespaceRule(),
        new DetalleRule(),
        new TotalFacturaRule()
    ));

    validator.validateOrThrow(comprobante);
  }

  @Test
  void fallaCuandoTotalesNoCoinciden() {
    Comprobante comprobante = ComprobanteTestData.facturaValida();
    InfoFactura infoFactura = (InfoFactura) comprobante.infoDocumento();
    InfoFactura alterado = new InfoFactura(
        infoFactura.fechaEmision(),
        infoFactura.dirEstablecimiento(),
        infoFactura.tipoIdentificacionComprador(),
        infoFactura.razonSocialComprador(),
        infoFactura.identificacionComprador(),
        infoFactura.totalSinImpuestos(),
        infoFactura.totalDescuento(),
        infoFactura.propina(),
        new BigDecimal("999.99"),
        infoFactura.moneda(),
        infoFactura.totalConImpuestos()
    );

    Comprobante comprobanteAlterado = Comprobante.reconstruir(
        comprobante.id(),
        comprobante.tipo(),
        comprobante.infoTributaria(),
        alterado,
        comprobante.detalles(),
        comprobante.estado(),
        comprobante.claveAcceso(),
        comprobante.xml(),
        comprobante.xmlFirmado(),
        comprobante.numeroAutorizacion(),
        comprobante.ultimoError(),
        comprobante.intentosEnvio(),
        comprobante.siguienteReintento(),
        comprobante.creadoEn(),
        comprobante.actualizadoEn()
    );

    ComprobanteValidator validator = new ComprobanteValidator(List.of(
        new RucLengthRule(),
        new AlphanumericWhitespaceRule(),
        new DetalleRule(),
        new TotalFacturaRule()
    ));

    assertThatThrownBy(() -> validator.validateOrThrow(comprobanteAlterado))
        .isInstanceOf(ValidationException.class);
  }

  @Test
  void fallaCuandoCamposTienenDobleEspacio() {
    Comprobante comprobante = ComprobanteTestData.facturaValida();
    InfoFactura infoFactura = (InfoFactura) comprobante.infoDocumento();
    InfoFactura alterado = new InfoFactura(
        infoFactura.fechaEmision(),
        "Quito  Norte",
        infoFactura.tipoIdentificacionComprador(),
        "Cliente  Demo",
        infoFactura.identificacionComprador(),
        infoFactura.totalSinImpuestos(),
        infoFactura.totalDescuento(),
        infoFactura.propina(),
        infoFactura.importeTotal(),
        infoFactura.moneda(),
        infoFactura.totalConImpuestos()
    );

    Comprobante comprobanteAlterado = Comprobante.reconstruir(
        comprobante.id(),
        comprobante.tipo(),
        comprobante.infoTributaria(),
        alterado,
        comprobante.detalles(),
        comprobante.estado(),
        comprobante.claveAcceso(),
        comprobante.xml(),
        comprobante.xmlFirmado(),
        comprobante.numeroAutorizacion(),
        comprobante.ultimoError(),
        comprobante.intentosEnvio(),
        comprobante.siguienteReintento(),
        comprobante.creadoEn(),
        comprobante.actualizadoEn()
    );

    ComprobanteValidator validator = new ComprobanteValidator(List.of(
        new RucLengthRule(),
        new AlphanumericWhitespaceRule(),
        new DetalleRule(),
        new TotalFacturaRule()
    ));

    assertThatThrownBy(() -> validator.validateOrThrow(comprobanteAlterado))
        .isInstanceOf(ValidationException.class);
  }
}
