package ec.sgi.backend.application.service;

import ec.sgi.backend.application.dto.SriCampoAdicionalDto;
import ec.sgi.backend.application.dto.SriDetalleDto;
import ec.sgi.backend.application.dto.SriEmitirFacturaRequest;
import ec.sgi.backend.application.dto.SriImpuestoDto;
import ec.sgi.backend.application.dto.SriInfoFacturaDto;
import ec.sgi.backend.application.dto.SriInfoTributariaDto;
import ec.sgi.backend.application.dto.SriTotalImpuestoDto;
import ec.sgi.backend.application.port.in.CrearFacturaCommand;
import ec.sgi.backend.domain.model.Cliente;
import ec.sgi.backend.domain.model.Factura;
import ec.sgi.backend.domain.model.FacturaImpuesto;
import ec.sgi.backend.domain.model.FacturaItem;
import ec.sgi.backend.domain.service.FacturaCalculoResult;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public final class SriEmitirFacturaRequestBuilder {
  private SriEmitirFacturaRequestBuilder() {
  }

  public static SriEmitirFacturaRequest build(
      CrearFacturaCommand command,
      Cliente cliente,
      FacturaCalculoResult calculo,
      Factura factura,
      BigDecimal propina,
      SriInfoTributariaDto infoTributaria
  ) {
    List<SriTotalImpuestoDto> totalImpuestos = calculo.impuestosTotales().stream()
        .map(total -> new SriTotalImpuestoDto(
            total.codigo(),
            total.codigoPorcentaje(),
            total.baseImponible(),
            total.valor()
        ))
        .toList();

    SriInfoFacturaDto infoFactura = new SriInfoFacturaDto(
        command.fechaEmision(),
        resolveDirEstablecimiento(factura.dirEstablecimiento(), infoTributaria.dirMatriz()),
        infoTributaria.contribuyenteEspecial(),
        infoTributaria.obligadoContabilidad(),
        cliente.tipoIdentificacion(),
        cliente.razonSocial(),
        cliente.identificacion(),
        truncateNullable(cliente.direccion(), 300),
        calculo.totales().totalSinImpuestos(),
        calculo.totales().totalDescuento(),
        propina,
        calculo.totales().importeTotal(),
        factura.moneda(),
        totalImpuestos
    );

    List<SriDetalleDto> detalles = calculo.items().stream()
        .map(SriEmitirFacturaRequestBuilder::toDetalle)
        .toList();

    return new SriEmitirFacturaRequest(
        infoTributaria,
        infoFactura,
        detalles,
        buildInfoAdicional(cliente, factura.dirEstablecimiento(), infoTributaria.dirMatriz(), command.observaciones()),
        command.codigoNumerico()
    );
  }

  private static List<SriCampoAdicionalDto> buildInfoAdicional(
      Cliente cliente,
      String dirEstablecimiento,
      String dirMatriz,
      String observaciones
  ) {
    List<SriCampoAdicionalDto> campos = new ArrayList<>();
    addCampo(campos, "Correo", cliente.email());
    addCampo(campos, "Direccion cliente", cliente.direccion());
    addCampo(campos, "Direccion establecimiento", resolveDirEstablecimiento(dirEstablecimiento, dirMatriz));
    addCampo(campos, "Observacion", observaciones);
    return campos;
  }

  private static void addCampo(List<SriCampoAdicionalDto> campos, String nombre, String valor) {
    String normalized = normalizeNullable(valor);
    if (normalized == null) {
      return;
    }
    campos.add(new SriCampoAdicionalDto(nombre, truncate(normalized, 300)));
  }

  private static String resolveDirEstablecimiento(String dirEstablecimiento, String dirMatriz) {
    String normalized = normalizeNullable(dirEstablecimiento);
    return normalized == null ? normalizeNullable(dirMatriz) : normalized;
  }

  private static String normalizeNullable(String value) {
    if (value == null) {
      return null;
    }
    String trimmed = value.trim();
    return trimmed.isEmpty() ? null : trimmed;
  }

  private static String truncate(String value, int maxLength) {
    if (value.length() <= maxLength) {
      return value;
    }
    return value.substring(0, maxLength);
  }

  private static String truncateNullable(String value, int maxLength) {
    String normalized = normalizeNullable(value);
    return normalized == null ? null : truncate(normalized, maxLength);
  }

  private static SriDetalleDto toDetalle(FacturaItem item) {
    List<SriImpuestoDto> impuestos = item.impuestos().stream()
        .map(SriEmitirFacturaRequestBuilder::toImpuesto)
        .toList();
    return new SriDetalleDto(
        item.codigoPrincipal(),
        item.descripcion(),
        item.cantidad(),
        item.precioUnitario(),
        item.descuento(),
        item.precioTotalSinImpuesto(),
        impuestos
    );
  }

  private static SriImpuestoDto toImpuesto(FacturaImpuesto impuesto) {
    return new SriImpuestoDto(
        impuesto.codigo(),
        impuesto.codigoPorcentaje(),
        impuesto.tarifa(),
        impuesto.baseImponible(),
        impuesto.valor()
    );
  }
}
