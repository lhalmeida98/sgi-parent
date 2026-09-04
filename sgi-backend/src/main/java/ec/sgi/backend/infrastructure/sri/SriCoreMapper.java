package ec.sgi.backend.infrastructure.sri;

import ec.sgi.backend.application.dto.SriCampoAdicionalDto;
import ec.sgi.backend.application.dto.SriConsultaEstadoRequest;
import ec.sgi.backend.application.dto.SriDetalleDto;
import ec.sgi.backend.application.dto.SriEmitirFacturaRequest;
import ec.sgi.backend.application.dto.SriImpuestoDto;
import ec.sgi.backend.application.dto.SriInfoFacturaDto;
import ec.sgi.backend.application.dto.SriInfoTributariaDto;
import ec.sgi.backend.application.dto.SriTotalImpuestoDto;
import ec.sgi.backend.application.dto.SriPagoDto;
import ec.sgi.backend.application.exception.BusinessRuleException;
import ec.sri.einvoice.application.port.in.ConsultarComprobanteCommand;
import ec.sri.einvoice.application.port.in.EmitirComprobanteCommand;
import ec.sri.einvoice.domain.model.Ambiente;
import ec.sri.einvoice.domain.model.CampoAdicional;
import ec.sri.einvoice.domain.model.ClaveAcceso;
import ec.sri.einvoice.domain.model.Detalle;
import ec.sri.einvoice.domain.model.Impuesto;
import ec.sri.einvoice.domain.model.Pago;
import ec.sri.einvoice.domain.model.InfoFactura;
import ec.sri.einvoice.domain.model.InfoTributaria;
import ec.sri.einvoice.domain.model.TipoComprobante;
import ec.sri.einvoice.domain.model.TipoEmision;
import ec.sri.einvoice.domain.model.TipoIdentificacion;
import ec.sri.einvoice.domain.model.TotalImpuesto;
import java.util.List;

public final class SriCoreMapper {
  private SriCoreMapper() {
  }

  public static EmitirComprobanteCommand toEmitirCommand(SriEmitirFacturaRequest request) {
    SriInfoTributariaDto infoTributaria = request.infoTributaria();
    InfoTributaria infoTrib = new InfoTributaria(
        parseAmbiente(infoTributaria.ambiente()),
        parseTipoEmision(infoTributaria.tipoEmision()),
        infoTributaria.razonSocial(),
        infoTributaria.nombreComercial(),
        infoTributaria.ruc(),
        infoTributaria.dirMatriz(),
        infoTributaria.estab(),
        infoTributaria.ptoEmi(),
        infoTributaria.secuencial(),
        null,
        infoTributaria.contribuyenteRimpe(),
        infoTributaria.agenteRetencion(),
        infoTributaria.firmaElectronica(),
        infoTributaria.claveFirma()
    );

    SriInfoFacturaDto infoFacturaDto = request.infoFactura();
    InfoFactura infoFactura = new InfoFactura(
        infoFacturaDto.fechaEmision(),
        infoFacturaDto.dirEstablecimiento(),
        infoFacturaDto.contribuyenteEspecial(),
        infoFacturaDto.obligadoContabilidad(),
        parseTipoIdentificacion(infoFacturaDto.tipoIdentificacionComprador()),
        infoFacturaDto.razonSocialComprador(),
        infoFacturaDto.identificacionComprador(),
        infoFacturaDto.direccionComprador(),
        infoFacturaDto.totalSinImpuestos(),
        infoFacturaDto.totalDescuento(),
        infoFacturaDto.propina(),
        infoFacturaDto.importeTotal(),
        infoFacturaDto.moneda(),
        toTotalImpuestos(infoFacturaDto.totalConImpuestos()),
        toPagos(request.pagos())
    );

    return new EmitirComprobanteCommand(
        TipoComprobante.FACTURA,
        infoTrib,
        infoFactura,
        toDetalles(request.detalles()),
        toInfoAdicional(request.infoAdicional()),
        request.codigoNumerico()
    );
  }

  public static ConsultarComprobanteCommand toConsultarCommand(SriConsultaEstadoRequest request) {
    return new ConsultarComprobanteCommand(
        parseAmbiente(request.ambiente()),
        request.claveAcceso()
    );
  }

  public static ClaveAcceso toClaveAcceso(String claveAcceso) {
    return ClaveAcceso.of(claveAcceso);
  }

  private static List<Detalle> toDetalles(List<SriDetalleDto> detalles) {
    return detalles.stream()
        .map(detalle -> new Detalle(
            detalle.codigoPrincipal(),
            detalle.descripcion(),
            detalle.cantidad(),
            detalle.precioUnitario(),
            detalle.descuento(),
            detalle.precioTotalSinImpuesto(),
            toImpuestos(detalle.impuestos())
        ))
        .toList();
  }

  private static List<CampoAdicional> toInfoAdicional(List<SriCampoAdicionalDto> campos) {
    if (campos == null || campos.isEmpty()) {
      return List.of();
    }
    return campos.stream()
        .filter(campo -> campo != null && !isBlank(campo.nombre()) && !isBlank(campo.valor()))
        .limit(15)
        .map(campo -> new CampoAdicional(campo.nombre().trim(), campo.valor().trim()))
        .toList();
  }

  private static List<Pago> toPagos(List<SriPagoDto> pagos) {
    if (pagos == null || pagos.isEmpty()) {
      return List.of();
    }
    return pagos.stream()
        .filter(pago -> pago != null && !isBlank(pago.formaPago()) && pago.total() != null)
        .map(pago -> new Pago(
            pago.formaPago(),
            pago.total(),
            pago.plazo(),
            pago.unidadTiempo()
        ))
        .toList();
  }

  private static List<Impuesto> toImpuestos(List<SriImpuestoDto> impuestos) {
    return impuestos.stream()
        .map(impuesto -> new Impuesto(
            impuesto.codigo(),
            impuesto.codigoPorcentaje(),
            impuesto.tarifa(),
            impuesto.baseImponible(),
            impuesto.valor()
        ))
        .toList();
  }

  private static List<TotalImpuesto> toTotalImpuestos(List<SriTotalImpuestoDto> totales) {
    return totales.stream()
        .map(total -> new TotalImpuesto(
            total.codigo(),
            total.codigoPorcentaje(),
            total.baseImponible(),
            total.valor()
        ))
        .toList();
  }

  private static Ambiente parseAmbiente(String value) {
    String normalized = normalize(value);
    return switch (normalized) {
      case "1", "PRUEBAS" -> Ambiente.PRUEBAS;
      case "2", "PRODUCCION" -> Ambiente.PRODUCCION;
      default -> throw new BusinessRuleException("Ambiente no valido: " + value);
    };
  }

  private static TipoEmision parseTipoEmision(String value) {
    String normalized = normalize(value);
    return switch (normalized) {
      case "1", "NORMAL" -> TipoEmision.NORMAL;
      case "2", "CONTINGENCIA" -> TipoEmision.CONTINGENCIA;
      default -> throw new BusinessRuleException("Tipo de emision no valido: " + value);
    };
  }

  private static TipoIdentificacion parseTipoIdentificacion(String value) {
    String normalized = normalize(value);
    return switch (normalized) {
      case "04", "RUC" -> TipoIdentificacion.RUC;
      case "05", "CEDULA" -> TipoIdentificacion.CEDULA;
      case "06", "PASAPORTE" -> TipoIdentificacion.PASAPORTE;
      case "07", "CONSUMIDOR_FINAL" -> TipoIdentificacion.CONSUMIDOR_FINAL;
      case "08", "IDENTIFICACION_EXTERIOR" -> TipoIdentificacion.IDENTIFICACION_EXTERIOR;
      default -> throw new BusinessRuleException("Tipo de identificacion no valido: " + value);
    };
  }

  private static String normalize(String value) {
    if (value == null) {
      return "";
    }
    return value.trim().toUpperCase();
  }

  private static boolean isBlank(String value) {
    return value == null || value.trim().isEmpty();
  }
}
