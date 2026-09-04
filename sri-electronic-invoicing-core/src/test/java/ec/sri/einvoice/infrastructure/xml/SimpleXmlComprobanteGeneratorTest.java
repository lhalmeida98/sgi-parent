package ec.sri.einvoice.infrastructure.xml;

import static org.assertj.core.api.Assertions.assertThat;

import ec.sri.einvoice.domain.model.CampoAdicional;
import ec.sri.einvoice.domain.ComprobanteTestData;
import ec.sri.einvoice.domain.model.ClaveAcceso;
import ec.sri.einvoice.domain.model.Comprobante;
import ec.sri.einvoice.domain.model.InfoFactura;
import ec.sri.einvoice.domain.model.InfoTributaria;
import ec.sri.einvoice.domain.model.Pago;
import ec.sri.einvoice.infrastructure.config.AppProperties;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;

class SimpleXmlComprobanteGeneratorTest {
  @Test
  void generaFacturaRimpeEmprendedorConObligadoContabilidadNo() {
    Comprobante comprobante = facturaConDatosTributarios(
        "CONTRIBUYENTE RÉGIMEN RIMPE",
        "NO"
    );
    String xml = new SimpleXmlComprobanteGenerator(new AppProperties()).generar(comprobante);

    assertThat(xml).contains("<contribuyenteRimpe>CONTRIBUYENTE RÉGIMEN RIMPE</contribuyenteRimpe>");
    assertThat(xml).contains("<obligadoContabilidad>NO</obligadoContabilidad>");
    assertThat(xml).contains("<direccionComprador>Guayas - Guayaquil</direccionComprador>");
    assertThat(xml).contains("<infoAdicional>");
    assertThat(xml).contains("<campoAdicional nombre=\"Correo\">cliente@example.com</campoAdicional>");
    new XsdSriXmlValidator().validar(comprobante.tipo(), xml);
  }

  @Test
  void generaFacturaGeneralSinLeyendaRimpeConObligadoContabilidadSi() {
    Comprobante comprobante = facturaConDatosTributarios(null, "SI");
    String xml = new SimpleXmlComprobanteGenerator(new AppProperties()).generar(comprobante);

    assertThat(xml).doesNotContain("contribuyenteRimpe");
    assertThat(xml).contains("<obligadoContabilidad>SI</obligadoContabilidad>");
    new XsdSriXmlValidator().validar(comprobante.tipo(), xml);
  }

  @Test
  void generaFacturaConPagoCreditoYPlazo() {
    Comprobante comprobante = facturaConPagoCredito();
    String xml = new SimpleXmlComprobanteGenerator(new AppProperties()).generar(comprobante);

    assertThat(xml).contains("<pagos><pago><formaPago>20</formaPago><total>112.00</total>"
        + "<plazo>30</plazo><unidadTiempo>dias</unidadTiempo></pago></pagos>");
    new XsdSriXmlValidator().validar(comprobante.tipo(), xml);
  }

  private Comprobante facturaConDatosTributarios(String contribuyenteRimpe, String obligadoContabilidad) {
    Comprobante base = ComprobanteTestData.facturaValida();
    InfoTributaria baseInfo = base.infoTributaria();
    InfoTributaria infoTributaria = new InfoTributaria(
        baseInfo.ambiente(),
        baseInfo.tipoEmision(),
        baseInfo.razonSocial(),
        baseInfo.nombreComercial(),
        baseInfo.ruc(),
        baseInfo.dirMatriz(),
        baseInfo.estab(),
        baseInfo.ptoEmi(),
        baseInfo.secuencial(),
        ClaveAcceso.of("2708202601172580912100120010010000000016851017615"),
        contribuyenteRimpe,
        null,
        baseInfo.firmaElectronica(),
        baseInfo.claveFirma()
    );

    InfoFactura baseFactura = (InfoFactura) base.infoDocumento();
    InfoFactura infoFactura = new InfoFactura(
        baseFactura.fechaEmision(),
        baseFactura.dirEstablecimiento(),
        null,
        obligadoContabilidad,
        baseFactura.tipoIdentificacionComprador(),
        baseFactura.razonSocialComprador(),
        baseFactura.identificacionComprador(),
        "Guayas - Guayaquil",
        baseFactura.totalSinImpuestos(),
        baseFactura.totalDescuento(),
        baseFactura.propina(),
        baseFactura.importeTotal(),
        baseFactura.moneda(),
        baseFactura.totalConImpuestos()
    );

    return Comprobante.reconstruir(
        base.id(),
        base.tipo(),
        infoTributaria,
        infoFactura,
        base.detalles(),
        List.of(
            new CampoAdicional("Correo", "cliente@example.com"),
            new CampoAdicional("Direccion cliente", "Guayas - Guayaquil")
        ),
        base.estado(),
        infoTributaria.claveAcceso(),
        base.xml(),
        base.xmlFirmado(),
        base.numeroAutorizacion(),
        base.ultimoError(),
        base.intentosEnvio(),
        base.siguienteReintento(),
        base.creadoEn(),
        base.actualizadoEn()
    );
  }

  private Comprobante facturaConPagoCredito() {
    Comprobante base = facturaConDatosTributarios(null, "SI");
    InfoFactura baseFactura = (InfoFactura) base.infoDocumento();
    InfoFactura infoFactura = new InfoFactura(
        baseFactura.fechaEmision(),
        baseFactura.dirEstablecimiento(),
        baseFactura.contribuyenteEspecial(),
        baseFactura.obligadoContabilidad(),
        baseFactura.tipoIdentificacionComprador(),
        baseFactura.razonSocialComprador(),
        baseFactura.identificacionComprador(),
        baseFactura.direccionComprador(),
        baseFactura.totalSinImpuestos(),
        baseFactura.totalDescuento(),
        baseFactura.propina(),
        baseFactura.importeTotal(),
        baseFactura.moneda(),
        baseFactura.totalConImpuestos(),
        List.of(new Pago("20", new BigDecimal("112.00"), 30, "dias"))
    );
    return Comprobante.reconstruir(
        base.id(),
        base.tipo(),
        base.infoTributaria(),
        infoFactura,
        base.detalles(),
        base.infoAdicional(),
        base.estado(),
        base.claveAcceso(),
        base.xml(),
        base.xmlFirmado(),
        base.numeroAutorizacion(),
        base.ultimoError(),
        base.intentosEnvio(),
        base.siguienteReintento(),
        base.creadoEn(),
        base.actualizadoEn()
    );
  }
}
