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
import java.util.ArrayList;
import java.util.List;

public class FacturaEntityMapper {
  public FacturaEntity toEntity(Factura factura) {
    FacturaEntity entity = new FacturaEntity();
    entity.setId(factura.id());
    entity.setEmpresaId(factura.empresaId());
    entity.setClienteId(factura.clienteId());
    entity.setPreordenId(factura.preordenId());
    entity.setFechaEmision(factura.fechaEmision());
    entity.setDirEstablecimiento(factura.dirEstablecimiento());
    entity.setMoneda(factura.moneda());
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

    List<FacturaItemEntity> itemEntities = new ArrayList<>();
    for (FacturaItem item : factura.items()) {
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
    entity.setItems(itemEntities);

    List<FacturaPagoEntity> pagoEntities = new ArrayList<>();
    for (FacturaPago pago : factura.pagos()) {
      FacturaPagoEntity pagoEntity = new FacturaPagoEntity();
      pagoEntity.setFactura(entity);
      pagoEntity.setFormaPago(pago.formaPago());
      pagoEntity.setMonto(pago.monto());
      pagoEntities.add(pagoEntity);
    }
    entity.setPagos(pagoEntities);

    return entity;
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

    List<FacturaItem> items = new ArrayList<>();
    for (FacturaItemEntity itemEntity : entity.getItems()) {
      List<FacturaImpuesto> impuestos = new ArrayList<>();
      for (FacturaImpuestoEntity impuestoEntity : itemEntity.getImpuestos()) {
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

    List<FacturaPago> pagos = new ArrayList<>();
    for (FacturaPagoEntity pagoEntity : entity.getPagos()) {
      pagos.add(new FacturaPago(pagoEntity.getFormaPago(), pagoEntity.getMonto()));
    }

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
