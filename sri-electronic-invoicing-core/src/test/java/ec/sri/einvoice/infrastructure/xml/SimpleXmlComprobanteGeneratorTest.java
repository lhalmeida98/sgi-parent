package ec.sri.einvoice.infrastructure.xml;

import static org.assertj.core.api.Assertions.assertThat;

import ec.sri.einvoice.domain.ComprobanteTestData;
import ec.sri.einvoice.domain.model.ClaveAcceso;
import ec.sri.einvoice.domain.model.Comprobante;
import ec.sri.einvoice.domain.model.InfoFactura;
import ec.sri.einvoice.domain.model.InfoTributaria;
import ec.sri.einvoice.infrastructure.config.AppProperties;
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
}
