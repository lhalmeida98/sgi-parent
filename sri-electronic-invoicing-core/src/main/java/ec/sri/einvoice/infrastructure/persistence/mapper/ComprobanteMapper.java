package ec.sri.einvoice.infrastructure.persistence.mapper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import ec.sri.einvoice.domain.model.Ambiente;
import ec.sri.einvoice.domain.model.ClaveAcceso;
import ec.sri.einvoice.domain.model.Comprobante;
import ec.sri.einvoice.domain.model.ComprobanteId;
import ec.sri.einvoice.domain.model.Detalle;
import ec.sri.einvoice.domain.model.EstadoComprobante;
import ec.sri.einvoice.domain.model.InfoDocumento;
import ec.sri.einvoice.domain.model.InfoFactura;
import ec.sri.einvoice.domain.model.InfoTributaria;
import ec.sri.einvoice.domain.model.Impuesto;
import ec.sri.einvoice.domain.model.TipoComprobante;
import ec.sri.einvoice.domain.model.TipoEmision;
import ec.sri.einvoice.domain.model.TipoIdentificacion;
import ec.sri.einvoice.domain.model.TotalImpuesto;
import ec.sri.einvoice.infrastructure.persistence.entity.ComprobanteEntity;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class ComprobanteMapper {
  private final ObjectMapper objectMapper;

  public ComprobanteMapper() {
    this.objectMapper = new ObjectMapper();
    this.objectMapper.registerModule(new JavaTimeModule());
  }

  public ComprobanteEntity toEntity(Comprobante comprobante) {
    ComprobanteEntity entity = new ComprobanteEntity();
    entity.setId(comprobante.id().value());
    entity.setTipo(comprobante.tipo().name());
    entity.setEstado(comprobante.estado().name());
    entity.setClaveAcceso(comprobante.claveAcceso() != null ? comprobante.claveAcceso().value() : null);
    entity.setXml(comprobante.xml());
    entity.setXmlFirmado(comprobante.xmlFirmado());
    entity.setNumeroAutorizacion(comprobante.numeroAutorizacion());
    entity.setEmisorRuc(comprobante.infoTributaria().ruc());

    InfoDocumento infoDocumento = comprobante.infoDocumento();
    if (infoDocumento instanceof InfoFactura infoFactura) {
      entity.setReceptorIdentificacion(infoFactura.identificacionComprador());
      entity.setTotal(infoFactura.importeTotal());
    }

    entity.setIntentosEnvio(comprobante.intentosEnvio());
    entity.setSiguienteReintento(comprobante.siguienteReintento());
    entity.setCreadoEn(comprobante.creadoEn());
    entity.setActualizadoEn(comprobante.actualizadoEn());
    entity.setPayloadJson(toPayloadJson(comprobante));
    return entity;
  }

  public Comprobante toDomain(ComprobanteEntity entity) {
    ComprobantePayload payload = fromPayloadJson(entity.getPayloadJson());
    InfoTributaria infoTributaria = toInfoTributaria(payload.infoTributaria());
    InfoDocumento infoDocumento = toInfoFactura(payload.infoFactura());
    List<Detalle> detalles = payload.detalles().stream().map(this::toDetalle).toList();
    ClaveAcceso claveAcceso = entity.getClaveAcceso() != null ? ClaveAcceso.of(entity.getClaveAcceso()) : null;

    return Comprobante.reconstruir(
        new ComprobanteId(entity.getId()),
        TipoComprobante.valueOf(entity.getTipo()),
        infoTributaria,
        infoDocumento,
        detalles,
        EstadoComprobante.valueOf(entity.getEstado()),
        claveAcceso,
        entity.getXml(),
        entity.getXmlFirmado(),
        entity.getNumeroAutorizacion(),
        null,
        entity.getIntentosEnvio(),
        entity.getSiguienteReintento(),
        entity.getCreadoEn(),
        entity.getActualizadoEn()
    );
  }

  private String toPayloadJson(Comprobante comprobante) {
    ComprobantePayload payload = new ComprobantePayload(
        comprobante.tipo().name(),
        toPayload(comprobante.infoTributaria()),
        toPayload((InfoFactura) comprobante.infoDocumento()),
        comprobante.detalles().stream().map(this::toPayload).toList()
    );
    try {
      return objectMapper.writeValueAsString(payload);
    } catch (JsonProcessingException ex) {
      throw new IllegalStateException("No se pudo serializar comprobante", ex);
    }
  }

  private ComprobantePayload fromPayloadJson(String json) {
    try {
      return objectMapper.readValue(json, ComprobantePayload.class);
    } catch (Exception ex) {
      throw new IllegalStateException("No se pudo leer payload", ex);
    }
  }

  private ComprobantePayload.InfoTributariaPayload toPayload(InfoTributaria infoTributaria) {
    return new ComprobantePayload.InfoTributariaPayload(
        infoTributaria.ambiente().name(),
        infoTributaria.tipoEmision().name(),
        infoTributaria.razonSocial(),
        infoTributaria.nombreComercial(),
        infoTributaria.ruc(),
        infoTributaria.dirMatriz(),
        infoTributaria.estab(),
        infoTributaria.ptoEmi(),
        infoTributaria.secuencial(),
        infoTributaria.claveAcceso() != null ? infoTributaria.claveAcceso().value() : null,
        infoTributaria.firmaElectronica(),
        infoTributaria.claveFirma()
    );
  }

  private ComprobantePayload.InfoFacturaPayload toPayload(InfoFactura infoFactura) {
    return new ComprobantePayload.InfoFacturaPayload(
        infoFactura.fechaEmision(),
        infoFactura.dirEstablecimiento(),
        infoFactura.tipoIdentificacionComprador().name(),
        infoFactura.razonSocialComprador(),
        infoFactura.identificacionComprador(),
        infoFactura.totalSinImpuestos(),
        infoFactura.totalDescuento(),
        infoFactura.propina(),
        infoFactura.importeTotal(),
        infoFactura.moneda(),
        infoFactura.totalConImpuestos().stream().map(this::toPayload).toList()
    );
  }

  private ComprobantePayload.DetallePayload toPayload(Detalle detalle) {
    return new ComprobantePayload.DetallePayload(
        detalle.codigoPrincipal(),
        detalle.descripcion(),
        detalle.cantidad(),
        detalle.precioUnitario(),
        detalle.descuento(),
        detalle.precioTotalSinImpuesto(),
        detalle.impuestos().stream().map(this::toPayload).toList()
    );
  }

  private ComprobantePayload.ImpuestoPayload toPayload(Impuesto impuesto) {
    return new ComprobantePayload.ImpuestoPayload(
        impuesto.codigo(),
        impuesto.codigoPorcentaje(),
        impuesto.tarifa(),
        impuesto.baseImponible(),
        impuesto.valor()
    );
  }

  private ComprobantePayload.TotalImpuestoPayload toPayload(TotalImpuesto totalImpuesto) {
    return new ComprobantePayload.TotalImpuestoPayload(
        totalImpuesto.codigo(),
        totalImpuesto.codigoPorcentaje(),
        totalImpuesto.baseImponible(),
        totalImpuesto.valor()
    );
  }

  private InfoTributaria toInfoTributaria(ComprobantePayload.InfoTributariaPayload payload) {
    return new InfoTributaria(
        Ambiente.valueOf(payload.ambiente()),
        TipoEmision.valueOf(payload.tipoEmision()),
        payload.razonSocial(),
        payload.nombreComercial(),
        payload.ruc(),
        payload.dirMatriz(),
        payload.estab(),
        payload.ptoEmi(),
        payload.secuencial(),
        payload.claveAcceso() != null ? ClaveAcceso.of(payload.claveAcceso()) : null,
        payload.firmaElectronica(),
        payload.claveFirma()
    );
  }

  private InfoFactura toInfoFactura(ComprobantePayload.InfoFacturaPayload payload) {
    return new InfoFactura(
        payload.fechaEmision(),
        payload.dirEstablecimiento(),
        TipoIdentificacion.valueOf(payload.tipoIdentificacionComprador()),
        payload.razonSocialComprador(),
        payload.identificacionComprador(),
        payload.totalSinImpuestos(),
        payload.totalDescuento(),
        payload.propina(),
        payload.importeTotal(),
        payload.moneda(),
        payload.totalConImpuestos().stream().map(this::toTotalImpuesto).toList()
    );
  }

  private Detalle toDetalle(ComprobantePayload.DetallePayload payload) {
    return new Detalle(
        payload.codigoPrincipal(),
        payload.descripcion(),
        payload.cantidad(),
        payload.precioUnitario(),
        payload.descuento(),
        payload.precioTotalSinImpuesto(),
        payload.impuestos().stream().map(this::toImpuesto).toList()
    );
  }

  private Impuesto toImpuesto(ComprobantePayload.ImpuestoPayload payload) {
    return new Impuesto(
        payload.codigo(),
        payload.codigoPorcentaje(),
        payload.tarifa(),
        payload.baseImponible(),
        payload.valor()
    );
  }

  private TotalImpuesto toTotalImpuesto(ComprobantePayload.TotalImpuestoPayload payload) {
    return new TotalImpuesto(
        payload.codigo(),
        payload.codigoPorcentaje(),
        payload.baseImponible(),
        payload.valor()
    );
  }
}
