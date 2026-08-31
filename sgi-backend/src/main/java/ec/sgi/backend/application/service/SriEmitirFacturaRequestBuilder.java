package ec.sgi.backend.application.service;

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
        factura.dirEstablecimiento(),
        infoTributaria.contribuyenteEspecial(),
        infoTributaria.obligadoContabilidad(),
        cliente.tipoIdentificacion(),
        cliente.razonSocial(),
        cliente.identificacion(),
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

    return new SriEmitirFacturaRequest(infoTributaria, infoFactura, detalles, command.codigoNumerico());
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
