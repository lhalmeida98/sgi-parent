package ec.sgi.backend.domain.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

public final class Factura {
  private final Long id;
  private final Long empresaId;
  private final Long clienteId;
  private final Long preordenId;
  private final InfoTributariaData infoTributaria;
  private final LocalDate fechaEmision;
  private final String dirEstablecimiento;
  private final String moneda;
  private final List<FacturaItem> items;
  private final FacturaTotales totales;
  private final List<FacturaPago> pagos;
  private final FacturaEstado estado;
  private final String claveAcceso;
  private final String coreComprobanteId;
  private final SriEstado sriEstado;
  private final String numeroAutorizacion;
  private final LocalDateTime fechaAutorizacion;
  private final String xmlFirmado;
  private final String xmlAutorizado;
  private final int intentosConsulta;
  private final LocalDateTime ultimoIntentoConsulta;

  public Factura(
      Long id,
      Long empresaId,
      Long clienteId,
      Long preordenId,
      InfoTributariaData infoTributaria,
      LocalDate fechaEmision,
      String dirEstablecimiento,
      String moneda,
      List<FacturaItem> items,
      FacturaTotales totales,
      List<FacturaPago> pagos,
      FacturaEstado estado,
      String claveAcceso,
      String coreComprobanteId,
      SriEstado sriEstado,
      String numeroAutorizacion,
      LocalDateTime fechaAutorizacion,
      String xmlFirmado,
      String xmlAutorizado,
      int intentosConsulta,
      LocalDateTime ultimoIntentoConsulta
  ) {
    this.id = id;
    this.empresaId = empresaId;
    this.clienteId = Objects.requireNonNull(clienteId, "clienteId");
    this.preordenId = preordenId;
    this.infoTributaria = Objects.requireNonNull(infoTributaria, "infoTributaria");
    this.fechaEmision = Objects.requireNonNull(fechaEmision, "fechaEmision");
    this.dirEstablecimiento = Objects.requireNonNull(dirEstablecimiento, "dirEstablecimiento");
    this.moneda = Objects.requireNonNull(moneda, "moneda");
    this.items = List.copyOf(items);
    this.totales = Objects.requireNonNull(totales, "totales");
    this.pagos = List.copyOf(pagos);
    this.estado = Objects.requireNonNull(estado, "estado");
    this.claveAcceso = claveAcceso;
    this.coreComprobanteId = coreComprobanteId;
    this.sriEstado = sriEstado;
    this.numeroAutorizacion = numeroAutorizacion;
    this.fechaAutorizacion = fechaAutorizacion;
    this.xmlFirmado = xmlFirmado;
    this.xmlAutorizado = xmlAutorizado;
    this.intentosConsulta = intentosConsulta;
    this.ultimoIntentoConsulta = ultimoIntentoConsulta;
  }

  public Long id() {
    return id;
  }

  public Long empresaId() {
    return empresaId;
  }

  public Long clienteId() {
    return clienteId;
  }

  public Long preordenId() {
    return preordenId;
  }

  public InfoTributariaData infoTributaria() {
    return infoTributaria;
  }

  public LocalDate fechaEmision() {
    return fechaEmision;
  }

  public String dirEstablecimiento() {
    return dirEstablecimiento;
  }

  public String moneda() {
    return moneda;
  }

  public List<FacturaItem> items() {
    return items;
  }

  public FacturaTotales totales() {
    return totales;
  }

  public List<FacturaPago> pagos() {
    return pagos;
  }

  public FacturaEstado estado() {
    return estado;
  }

  public String claveAcceso() {
    return claveAcceso;
  }

  public String coreComprobanteId() {
    return coreComprobanteId;
  }

  public SriEstado sriEstado() {
    return sriEstado;
  }

  public String numeroAutorizacion() {
    return numeroAutorizacion;
  }

  public LocalDateTime fechaAutorizacion() {
    return fechaAutorizacion;
  }

  public String xmlFirmado() {
    return xmlFirmado;
  }

  public String xmlAutorizado() {
    return xmlAutorizado;
  }

  public int intentosConsulta() {
    return intentosConsulta;
  }

  public LocalDateTime ultimoIntentoConsulta() {
    return ultimoIntentoConsulta;
  }

  public Factura withEstado(FacturaEstado nuevoEstado) {
    return new Factura(
        id,
        empresaId,
        clienteId,
        preordenId,
        infoTributaria,
        fechaEmision,
        dirEstablecimiento,
        moneda,
        items,
        totales,
        pagos,
        nuevoEstado,
        claveAcceso,
        coreComprobanteId,
        sriEstado,
        numeroAutorizacion,
        fechaAutorizacion,
        xmlFirmado,
        xmlAutorizado,
        intentosConsulta,
        ultimoIntentoConsulta
    );
  }

  public Factura withClaveAcceso(String nuevaClave) {
    return new Factura(
        id,
        empresaId,
        clienteId,
        preordenId,
        infoTributaria,
        fechaEmision,
        dirEstablecimiento,
        moneda,
        items,
        totales,
        pagos,
        estado,
        nuevaClave,
        coreComprobanteId,
        sriEstado,
        numeroAutorizacion,
        fechaAutorizacion,
        xmlFirmado,
        xmlAutorizado,
        intentosConsulta,
        ultimoIntentoConsulta
    );
  }

  public Factura withCoreComprobanteId(String nuevoId) {
    return new Factura(
        id,
        empresaId,
        clienteId,
        preordenId,
        infoTributaria,
        fechaEmision,
        dirEstablecimiento,
        moneda,
        items,
        totales,
        pagos,
        estado,
        claveAcceso,
        nuevoId,
        sriEstado,
        numeroAutorizacion,
        fechaAutorizacion,
        xmlFirmado,
        xmlAutorizado,
        intentosConsulta,
        ultimoIntentoConsulta
    );
  }

  public Factura withSriEstado(SriEstado nuevoEstado) {
    return new Factura(
        id,
        empresaId,
        clienteId,
        preordenId,
        infoTributaria,
        fechaEmision,
        dirEstablecimiento,
        moneda,
        items,
        totales,
        pagos,
        estado,
        claveAcceso,
        coreComprobanteId,
        nuevoEstado,
        numeroAutorizacion,
        fechaAutorizacion,
        xmlFirmado,
        xmlAutorizado,
        intentosConsulta,
        ultimoIntentoConsulta
    );
  }

  public Factura withNumeroAutorizacion(String nuevoNumero) {
    return new Factura(
        id,
        empresaId,
        clienteId,
        preordenId,
        infoTributaria,
        fechaEmision,
        dirEstablecimiento,
        moneda,
        items,
        totales,
        pagos,
        estado,
        claveAcceso,
        coreComprobanteId,
        sriEstado,
        nuevoNumero,
        fechaAutorizacion,
        xmlFirmado,
        xmlAutorizado,
        intentosConsulta,
        ultimoIntentoConsulta
    );
  }

  public Factura withFechaAutorizacion(LocalDateTime nuevaFecha) {
    return new Factura(
        id,
        empresaId,
        clienteId,
        preordenId,
        infoTributaria,
        fechaEmision,
        dirEstablecimiento,
        moneda,
        items,
        totales,
        pagos,
        estado,
        claveAcceso,
        coreComprobanteId,
        sriEstado,
        numeroAutorizacion,
        nuevaFecha,
        xmlFirmado,
        xmlAutorizado,
        intentosConsulta,
        ultimoIntentoConsulta
    );
  }

  public Factura withXmlFirmado(String nuevoXml) {
    return new Factura(
        id,
        empresaId,
        clienteId,
        preordenId,
        infoTributaria,
        fechaEmision,
        dirEstablecimiento,
        moneda,
        items,
        totales,
        pagos,
        estado,
        claveAcceso,
        coreComprobanteId,
        sriEstado,
        numeroAutorizacion,
        fechaAutorizacion,
        nuevoXml,
        xmlAutorizado,
        intentosConsulta,
        ultimoIntentoConsulta
    );
  }

  public Factura withXmlAutorizado(String nuevoXml) {
    return new Factura(
        id,
        empresaId,
        clienteId,
        preordenId,
        infoTributaria,
        fechaEmision,
        dirEstablecimiento,
        moneda,
        items,
        totales,
        pagos,
        estado,
        claveAcceso,
        coreComprobanteId,
        sriEstado,
        numeroAutorizacion,
        fechaAutorizacion,
        xmlFirmado,
        nuevoXml,
        intentosConsulta,
        ultimoIntentoConsulta
    );
  }

  public Factura withIntentoConsulta(int nuevosIntentos, LocalDateTime nuevoUltimoIntento) {
    return new Factura(
        id,
        empresaId,
        clienteId,
        preordenId,
        infoTributaria,
        fechaEmision,
        dirEstablecimiento,
        moneda,
        items,
        totales,
        pagos,
        estado,
        claveAcceso,
        coreComprobanteId,
        sriEstado,
        numeroAutorizacion,
        fechaAutorizacion,
        xmlFirmado,
        xmlAutorizado,
        nuevosIntentos,
        nuevoUltimoIntento
    );
  }
}
