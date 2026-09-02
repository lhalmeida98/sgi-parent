package ec.sgi.backend.infrastructure.persistence.adapter;

import ec.sgi.backend.domain.model.Factura;
import ec.sgi.backend.domain.model.FacturaEstado;
import ec.sgi.backend.domain.model.FacturaImpuesto;
import ec.sgi.backend.domain.model.FacturaItem;
import ec.sgi.backend.domain.model.FacturaPago;
import ec.sgi.backend.domain.model.FacturaTotales;
import ec.sgi.backend.domain.model.InfoTributariaData;
import ec.sgi.backend.domain.model.RegimenTributario;
import ec.sgi.backend.domain.model.SriEstado;
import ec.sgi.backend.infrastructure.persistence.entity.FacturaEntity;
import ec.sgi.backend.infrastructure.persistence.entity.FacturaImpuestoEntity;
import ec.sgi.backend.infrastructure.persistence.entity.FacturaItemEntity;
import ec.sgi.backend.infrastructure.persistence.entity.FacturaPagoEntity;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class FacturaEntityMapper {
  public FacturaEntity toEntity(Factura factura) {
    FacturaEntity entity = new FacturaEntity();
    copyScalarFields(entity, factura);
    entity.setItems(toItemEntities(entity, factura.items()));
    entity.setPagos(toPagoEntities(entity, factura.pagos()));
    return entity;
  }

  public void updateEntityPreservingDetalle(FacturaEntity entity, Factura factura) {
    copyScalarFields(entity, factura);
  }

  private void copyScalarFields(FacturaEntity entity, Factura factura) {
    entity.setId(factura.id());
    entity.setEmpresaId(factura.empresaId());
    entity.setClienteId(factura.clienteId());
    entity.setPreordenId(factura.preordenId());
    entity.setFechaEmision(factura.fechaEmision());
    entity.setDirEstablecimiento(factura.dirEstablecimiento());
    entity.setMoneda(factura.moneda());
    entity.setObservaciones(factura.observaciones());
    entity.setTotalSinImpuestos(factura.totales().totalSinImpuestos());
    entity.setTotalDescuento(factura.totales().totalDescuento());
    entity.setTotalImpuestos(factura.totales().totalImpuestos());
    entity.setImporteTotal(factura.totales().importeTotal());
    entity.setEstado(factura.estado().name());
    entity.setClaveAcceso(factura.claveAcceso());
    entity.setCoreComprobanteId(factura.coreComprobanteId());
    entity.setNumeroAutorizacion(factura.numeroAutorizacion());
    entity.setFechaAutorizacion(factura.fechaAutorizacion());
    entity.setXmlFirmado(factura.xmlFirmado());
    entity.setXmlAutorizado(factura.xmlAutorizado());
    entity.setIntentosConsulta(factura.intentosConsulta());
    entity.setUltimoIntentoConsulta(factura.ultimoIntentoConsulta());

    SriEstado sriEstado = factura.sriEstado();
    if (sriEstado != null) {
      entity.setSriEstadoConsulta(sriEstado.estadoConsulta());
      entity.setSriEstadoAutorizacion(sriEstado.estadoAutorizacion());
      entity.setSriMensaje(sriEstado.mensaje());
    } else {
      entity.setSriEstadoConsulta(null);
      entity.setSriEstadoAutorizacion(null);
      entity.setSriMensaje(null);
    }

    InfoTributariaData info = factura.infoTributaria();
    entity.setInfoAmbiente(info.ambiente());
    entity.setInfoTipoEmision(info.tipoEmision());
    entity.setInfoRazonSocial(info.razonSocial());
    entity.setInfoNombreComercial(info.nombreComercial());
    entity.setInfoRuc(info.ruc());
    entity.setInfoDirMatriz(info.dirMatriz());
    entity.setInfoEstab(info.estab());
    entity.setInfoPtoEmi(info.ptoEmi());
    entity.setInfoSecuencial(info.secuencial());
    entity.setInfoObligadoContabilidad(info.obligadoContabilidad());
    entity.setInfoRegimenTributario(info.regimenTributario().name());
    entity.setInfoContribuyenteEspecial(info.contribuyenteEspecial());
    entity.setInfoNumeroContribuyenteEspecial(info.numeroContribuyenteEspecial());
    entity.setInfoAgenteRetencion(info.agenteRetencion());
  }

  private List<FacturaItemEntity> toItemEntities(FacturaEntity entity, List<FacturaItem> items) {
    List<FacturaItemEntity> itemEntities = new ArrayList<>();
    for (FacturaItem item : items) {
      FacturaItemEntity itemEntity = new FacturaItemEntity();
      itemEntity.setFactura(entity);
      itemEntity.setBodegaId(item.bodegaId());
      itemEntity.setProductoId(item.productoId());
      itemEntity.setCodigoPrincipal(item.codigoPrincipal());
      itemEntity.setDescripcion(item.descripcion());
      itemEntity.setCantidad(item.cantidad());
      itemEntity.setPrecioUnitario(item.precioUnitario());
      itemEntity.setDescuento(item.descuento());
      itemEntity.setPrecioTotalSinImpuesto(item.precioTotalSinImpuesto());

      List<FacturaImpuestoEntity> impuestos = new ArrayList<>();
      for (FacturaImpuesto impuesto : item.impuestos()) {
        FacturaImpuestoEntity impuestoEntity = new FacturaImpuestoEntity();
        impuestoEntity.setItem(itemEntity);
        impuestoEntity.setCodigo(impuesto.codigo());
        impuestoEntity.setCodigoPorcentaje(impuesto.codigoPorcentaje());
        impuestoEntity.setTarifa(impuesto.tarifa());
        impuestoEntity.setBaseImponible(impuesto.baseImponible());
        impuestoEntity.setValor(impuesto.valor());
        impuestos.add(impuestoEntity);
      }
      itemEntity.setImpuestos(impuestos);
      itemEntities.add(itemEntity);
    }
    return itemEntities;
  }

  private List<FacturaPagoEntity> toPagoEntities(FacturaEntity entity, List<FacturaPago> pagos) {
    List<FacturaPagoEntity> pagoEntities = new ArrayList<>();
    for (FacturaPago pago : pagos) {
      FacturaPagoEntity pagoEntity = new FacturaPagoEntity();
      pagoEntity.setFactura(entity);
      pagoEntity.setFormaPago(pago.formaPago());
      pagoEntity.setMonto(pago.monto());
      pagoEntities.add(pagoEntity);
    }
    return pagoEntities;
  }

  public Factura toDomain(FacturaEntity entity) {
    InfoTributariaData info = new InfoTributariaData(
        entity.getInfoAmbiente(),
        entity.getInfoTipoEmision(),
        entity.getInfoRazonSocial(),
        entity.getInfoNombreComercial(),
        entity.getInfoRuc(),
        entity.getInfoDirMatriz(),
        entity.getInfoEstab(),
        entity.getInfoPtoEmi(),
        entity.getInfoSecuencial(),
        Boolean.TRUE.equals(entity.getInfoObligadoContabilidad()),
        RegimenTributario.from(entity.getInfoRegimenTributario(), false),
        Boolean.TRUE.equals(entity.getInfoContribuyenteEspecial()),
        entity.getInfoNumeroContribuyenteEspecial(),
        Boolean.TRUE.equals(entity.getInfoAgenteRetencion())
    );

    FacturaTotales totales = new FacturaTotales(
        entity.getTotalSinImpuestos(),
        entity.getTotalDescuento(),
        entity.getTotalImpuestos(),
        entity.getImporteTotal()
    );

    SriEstado sriEstado = null;
    if (entity.getSriEstadoConsulta() != null || entity.getSriEstadoAutorizacion() != null || entity.getSriMensaje() != null) {
      sriEstado = new SriEstado(
          entity.getSriEstadoConsulta(),
          entity.getSriEstadoAutorizacion(),
          entity.getSriMensaje()
      );
    }

    List<FacturaItem> items = mapItems(entity.getItems(), entity.getTotalSinImpuestos());
    List<FacturaPago> pagos = mapPagos(entity.getPagos(), entity.getImporteTotal());

    FacturaEstado estado = mapEstado(entity.getEstado());
    int intentosConsulta = entity.getIntentosConsulta() == null ? 0 : entity.getIntentosConsulta();

    return new Factura(
        entity.getId(),
        entity.getEmpresaId(),
        entity.getClienteId(),
        entity.getPreordenId(),
        info,
        entity.getFechaEmision(),
        entity.getDirEstablecimiento(),
        entity.getMoneda(),
        entity.getObservaciones(),
        items,
        totales,
        pagos,
        estado,
        entity.getClaveAcceso(),
        entity.getCoreComprobanteId(),
        sriEstado,
        entity.getNumeroAutorizacion(),
        entity.getFechaAutorizacion(),
        entity.getXmlFirmado(),
        entity.getXmlAutorizado(),
        intentosConsulta,
        entity.getUltimoIntentoConsulta()
    );
  }

  private List<FacturaItem> mapItems(List<FacturaItemEntity> itemEntities, BigDecimal totalSinImpuestos) {
    List<FacturaItem> items = new ArrayList<>();
    for (FacturaItemEntity itemEntity : distinctItems(itemEntities, totalSinImpuestos)) {
      List<FacturaImpuesto> impuestos = new ArrayList<>();
      for (FacturaImpuestoEntity impuestoEntity : distinctImpuestos(itemEntity.getImpuestos())) {
        impuestos.add(new FacturaImpuesto(
            impuestoEntity.getCodigo(),
            impuestoEntity.getCodigoPorcentaje(),
            impuestoEntity.getTarifa(),
            impuestoEntity.getBaseImponible(),
            impuestoEntity.getValor()
        ));
      }
      items.add(new FacturaItem(
          itemEntity.getBodegaId(),
          itemEntity.getProductoId(),
          itemEntity.getCodigoPrincipal(),
          itemEntity.getDescripcion(),
          itemEntity.getCantidad(),
          itemEntity.getPrecioUnitario(),
          itemEntity.getDescuento(),
          itemEntity.getPrecioTotalSinImpuesto(),
          impuestos
      ));
    }
    return items;
  }

  private List<FacturaPago> mapPagos(List<FacturaPagoEntity> pagoEntities, BigDecimal importeTotal) {
    List<FacturaPago> pagos = new ArrayList<>();
    for (FacturaPagoEntity pagoEntity : distinctPagos(pagoEntities, importeTotal)) {
      pagos.add(new FacturaPago(pagoEntity.getFormaPago(), pagoEntity.getMonto()));
    }
    return pagos;
  }

  private List<FacturaItemEntity> distinctItems(List<FacturaItemEntity> itemEntities, BigDecimal totalSinImpuestos) {
    if (itemEntities == null) {
      return List.of();
    }
    List<FacturaItemEntity> byEntity = distinctByEntityKey(itemEntities, "item");
    List<FacturaItemEntity> byNaturalKey = distinctByNaturalItemKey(byEntity);
    if (byNaturalKey.size() < byEntity.size()
        && sameAmount(sumItems(byNaturalKey), totalSinImpuestos)
        && !sameAmount(sumItems(byEntity), totalSinImpuestos)) {
      return byNaturalKey;
    }
    return byEntity;
  }

  private List<FacturaItemEntity> distinctByNaturalItemKey(List<FacturaItemEntity> itemEntities) {
    Map<String, FacturaItemEntity> unique = new LinkedHashMap<>();
    for (FacturaItemEntity itemEntity : itemEntities) {
      if (itemEntity != null) {
        unique.putIfAbsent(itemNaturalKey(itemEntity), itemEntity);
      }
    }
    return new ArrayList<>(unique.values());
  }

  private List<FacturaImpuestoEntity> distinctImpuestos(List<FacturaImpuestoEntity> impuestoEntities) {
    if (impuestoEntities == null) {
      return List.of();
    }
    Map<String, FacturaImpuestoEntity> unique = new LinkedHashMap<>();
    for (FacturaImpuestoEntity impuestoEntity : impuestoEntities) {
      if (impuestoEntity != null) {
        unique.putIfAbsent(impuestoNaturalKey(impuestoEntity), impuestoEntity);
      }
    }
    return new ArrayList<>(unique.values());
  }

  private List<FacturaPagoEntity> distinctPagos(List<FacturaPagoEntity> pagoEntities, BigDecimal importeTotal) {
    if (pagoEntities == null) {
      return List.of();
    }
    List<FacturaPagoEntity> byEntity = distinctByEntityKey(pagoEntities, "pago");
    List<FacturaPagoEntity> byNaturalKey = distinctByNaturalPagoKey(byEntity);
    if (byNaturalKey.size() < byEntity.size()
        && sameAmount(sumPagos(byNaturalKey), importeTotal)
        && !sameAmount(sumPagos(byEntity), importeTotal)) {
      return byNaturalKey;
    }
    return byEntity;
  }

  private List<FacturaPagoEntity> distinctByNaturalPagoKey(List<FacturaPagoEntity> pagoEntities) {
    Map<String, FacturaPagoEntity> unique = new LinkedHashMap<>();
    for (FacturaPagoEntity pagoEntity : pagoEntities) {
      if (pagoEntity != null) {
        unique.putIfAbsent(pagoNaturalKey(pagoEntity), pagoEntity);
      }
    }
    return new ArrayList<>(unique.values());
  }

  private <T> List<T> distinctByEntityKey(List<T> entities, String type) {
    Map<String, T> unique = new LinkedHashMap<>();
    for (T entity : entities) {
      if (entity == null) {
        continue;
      }
      Long id = switch (entity) {
        case FacturaItemEntity item -> item.getId();
        case FacturaPagoEntity pago -> pago.getId();
        default -> null;
      };
      unique.putIfAbsent(entityKey(type, id, entity), entity);
    }
    return new ArrayList<>(unique.values());
  }

  private String entityKey(String type, Long id, Object entity) {
    if (id != null) {
      return type + ":" + id;
    }
    return type + ":identity:" + System.identityHashCode(entity);
  }

  private String itemNaturalKey(FacturaItemEntity item) {
    return safe(item.getBodegaId()) + "|"
        + safe(item.getProductoId()) + "|"
        + safe(item.getCodigoPrincipal()) + "|"
        + safe(item.getDescripcion()) + "|"
        + amountKey(item.getCantidad()) + "|"
        + amountKey(item.getPrecioUnitario()) + "|"
        + amountKey(item.getDescuento()) + "|"
        + amountKey(item.getPrecioTotalSinImpuesto());
  }

  private String impuestoNaturalKey(FacturaImpuestoEntity impuesto) {
    return safe(impuesto.getCodigo()) + "|"
        + safe(impuesto.getCodigoPorcentaje()) + "|"
        + amountKey(impuesto.getTarifa()) + "|"
        + amountKey(impuesto.getBaseImponible()) + "|"
        + amountKey(impuesto.getValor());
  }

  private String pagoNaturalKey(FacturaPagoEntity pago) {
    return safe(pago.getFormaPago()) + "|" + amountKey(pago.getMonto());
  }

  private BigDecimal sumItems(List<FacturaItemEntity> itemEntities) {
    BigDecimal total = BigDecimal.ZERO;
    for (FacturaItemEntity itemEntity : itemEntities) {
      if (itemEntity != null && itemEntity.getPrecioTotalSinImpuesto() != null) {
        total = total.add(itemEntity.getPrecioTotalSinImpuesto());
      }
    }
    return total;
  }

  private BigDecimal sumPagos(List<FacturaPagoEntity> pagoEntities) {
    BigDecimal total = BigDecimal.ZERO;
    for (FacturaPagoEntity pagoEntity : pagoEntities) {
      if (pagoEntity != null && pagoEntity.getMonto() != null) {
        total = total.add(pagoEntity.getMonto());
      }
    }
    return total;
  }

  private boolean sameAmount(BigDecimal left, BigDecimal right) {
    if (left == null || right == null) {
      return false;
    }
    return left.compareTo(right) == 0;
  }

  private String amountKey(BigDecimal value) {
    return value == null ? "" : value.stripTrailingZeros().toPlainString();
  }

  private String safe(Object value) {
    return value == null ? "" : value.toString();
  }

  private FacturaEstado mapEstado(String estado) {
    if (estado == null) {
      return FacturaEstado.CREADA;
    }
    return switch (estado) {
      case "ENVIADA_CORE" -> FacturaEstado.ENVIADA_SRI;
      case "RECHAZADA" -> FacturaEstado.NO_AUTORIZADA;
      default -> FacturaEstado.valueOf(estado);
    };
  }
}
