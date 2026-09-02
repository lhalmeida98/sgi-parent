package ec.sgi.backend.infrastructure.persistence.adapter;

import static org.assertj.core.api.Assertions.assertThat;

import ec.sgi.backend.domain.model.Factura;
import ec.sgi.backend.infrastructure.persistence.entity.FacturaEntity;
import ec.sgi.backend.infrastructure.persistence.entity.FacturaImpuestoEntity;
import ec.sgi.backend.infrastructure.persistence.entity.FacturaItemEntity;
import ec.sgi.backend.infrastructure.persistence.entity.FacturaPagoEntity;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

class FacturaEntityMapperTest {
  private final FacturaEntityMapper mapper = new FacturaEntityMapper();

  @Test
  void toDomainRemueveDuplicadosDeColeccionesJpa() {
    FacturaEntity entity = facturaBase();
    FacturaItemEntity item = item(entity);
    FacturaImpuestoEntity impuesto = impuesto(item);
    item.setImpuestos(new ArrayList<>(List.of(impuesto, impuesto, impuesto)));
    entity.setItems(new ArrayList<>(List.of(item, item, item)));

    FacturaPagoEntity pago = pago(entity);
    entity.setPagos(new ArrayList<>(List.of(pago, pago, pago)));

    Factura factura = mapper.toDomain(entity);

    assertThat(factura.items()).hasSize(1);
    assertThat(factura.items().getFirst().impuestos()).hasSize(1);
    assertThat(factura.pagos()).hasSize(1);
    assertThat(factura.pagos().getFirst().monto()).isEqualByComparingTo("1.38");
  }

  @Test
  void toDomainRemueveDuplicadosPersistidosCuandoNoCuadranConTotales() {
    FacturaEntity entity = facturaBase();
    entity.setTotalSinImpuestos(new BigDecimal("2.40"));
    entity.setTotalImpuestos(new BigDecimal("0.36"));
    entity.setImporteTotal(new BigDecimal("2.76"));

    FacturaItemEntity item1 = item(entity, 10L, new BigDecimal("2.00"), new BigDecimal("2.40"));
    FacturaItemEntity item2 = item(entity, 11L, new BigDecimal("2.00"), new BigDecimal("2.40"));
    FacturaItemEntity item3 = item(entity, 12L, new BigDecimal("2.00"), new BigDecimal("2.40"));
    entity.setItems(new ArrayList<>(List.of(item1, item2, item3)));

    FacturaPagoEntity pago1 = pago(entity, 200L, new BigDecimal("2.76"));
    FacturaPagoEntity pago2 = pago(entity, 201L, new BigDecimal("2.76"));
    FacturaPagoEntity pago3 = pago(entity, 202L, new BigDecimal("2.76"));
    entity.setPagos(new ArrayList<>(List.of(pago1, pago2, pago3)));

    Factura factura = mapper.toDomain(entity);

    assertThat(factura.items()).hasSize(1);
    assertThat(factura.items().getFirst().cantidad()).isEqualByComparingTo("2.00");
    assertThat(factura.items().getFirst().precioTotalSinImpuesto()).isEqualByComparingTo("2.40");
    assertThat(factura.pagos()).hasSize(1);
    assertThat(factura.pagos().getFirst().monto()).isEqualByComparingTo("2.76");
  }

  private FacturaEntity facturaBase() {
    FacturaEntity entity = new FacturaEntity();
    entity.setId(1L);
    entity.setEmpresaId(1L);
    entity.setClienteId(2L);
    entity.setFechaEmision(LocalDate.of(2026, 9, 1));
    entity.setDirEstablecimiento("JIJIPAPA AV. EL INCA E14-38 Y N47B DE LOS NOGALES");
    entity.setMoneda("USD");
    entity.setTotalSinImpuestos(new BigDecimal("1.20"));
    entity.setTotalDescuento(BigDecimal.ZERO);
    entity.setTotalImpuestos(new BigDecimal("0.18"));
    entity.setImporteTotal(new BigDecimal("1.38"));
    entity.setEstado("AUTORIZADA");
    entity.setInfoAmbiente("2");
    entity.setInfoTipoEmision("1");
    entity.setInfoRazonSocial("LUIS HENRY ALMEIDA FLORES");
    entity.setInfoNombreComercial("MyM mundo repuestos");
    entity.setInfoRuc("1725809121001");
    entity.setInfoDirMatriz("JIJIPAPA AV. EL INCA E14-38 Y N47B DE LOS NOGALES");
    entity.setInfoEstab("001");
    entity.setInfoPtoEmi("001");
    entity.setInfoSecuencial("000000002");
    entity.setInfoObligadoContabilidad(false);
    entity.setInfoRegimenTributario("GENERAL");
    entity.setInfoContribuyenteEspecial(false);
    entity.setInfoAgenteRetencion(false);
    entity.setIntentosConsulta(0);
    return entity;
  }

  private FacturaItemEntity item(FacturaEntity factura) {
    return item(factura, 10L, new BigDecimal("1.00"), new BigDecimal("1.20"));
  }

  private FacturaItemEntity item(
      FacturaEntity factura,
      Long id,
      BigDecimal cantidad,
      BigDecimal precioTotalSinImpuesto
  ) {
    FacturaItemEntity item = new FacturaItemEntity();
    item.setId(id);
    item.setFactura(factura);
    item.setBodegaId(1L);
    item.setProductoId(20L);
    item.setCodigoPrincipal("1025-PR");
    item.setDescripcion("Tapa de radiador plastico");
    item.setCantidad(cantidad);
    item.setPrecioUnitario(new BigDecimal("1.20"));
    item.setDescuento(BigDecimal.ZERO);
    item.setPrecioTotalSinImpuesto(precioTotalSinImpuesto);
    return item;
  }

  private FacturaImpuestoEntity impuesto(FacturaItemEntity item) {
    FacturaImpuestoEntity impuesto = new FacturaImpuestoEntity();
    impuesto.setId(100L);
    impuesto.setItem(item);
    impuesto.setCodigo("2");
    impuesto.setCodigoPorcentaje("4");
    impuesto.setTarifa(new BigDecimal("15.00"));
    impuesto.setBaseImponible(new BigDecimal("1.20"));
    impuesto.setValor(new BigDecimal("0.18"));
    return impuesto;
  }

  private FacturaPagoEntity pago(FacturaEntity factura) {
    return pago(factura, 200L, new BigDecimal("1.38"));
  }

  private FacturaPagoEntity pago(FacturaEntity factura, Long id, BigDecimal monto) {
    FacturaPagoEntity pago = new FacturaPagoEntity();
    pago.setId(id);
    pago.setFactura(factura);
    pago.setFormaPago("01");
    pago.setMonto(monto);
    return pago;
  }
}
