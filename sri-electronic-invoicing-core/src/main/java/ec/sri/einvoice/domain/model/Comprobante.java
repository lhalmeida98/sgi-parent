package ec.sri.einvoice.domain.model;

import ec.sri.einvoice.domain.event.ComprobanteEvent;
import ec.sri.einvoice.domain.event.ComprobanteEventType;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public final class Comprobante {
  private final ComprobanteId id;
  private final TipoComprobante tipo;
  private InfoTributaria infoTributaria;
  private InfoDocumento infoDocumento;
  private final List<Detalle> detalles;
  private EstadoComprobante estado;
  private ClaveAcceso claveAcceso;
  private String xml;
  private String xmlFirmado;
  private String numeroAutorizacion;
  private String ultimoError;
  private int intentosEnvio;
  private Instant siguienteReintento;
  private final Instant creadoEn;
  private Instant actualizadoEn;
  private final List<ComprobanteEvent> eventos = new ArrayList<>();

  private Comprobante(
      ComprobanteId id,
      TipoComprobante tipo,
      InfoTributaria infoTributaria,
      InfoDocumento infoDocumento,
      List<Detalle> detalles,
      EstadoComprobante estado,
      ClaveAcceso claveAcceso,
      String xml,
      String xmlFirmado,
      String numeroAutorizacion,
      String ultimoError,
      int intentosEnvio,
      Instant siguienteReintento,
      Instant creadoEn,
      Instant actualizadoEn
  ) {
    this.id = Objects.requireNonNull(id, "id");
    this.tipo = Objects.requireNonNull(tipo, "tipo");
    this.infoTributaria = Objects.requireNonNull(infoTributaria, "infoTributaria");
    this.infoDocumento = Objects.requireNonNull(infoDocumento, "infoDocumento");
    this.detalles = List.copyOf(detalles);
    this.estado = Objects.requireNonNull(estado, "estado");
    this.claveAcceso = claveAcceso;
    this.xml = xml;
    this.xmlFirmado = xmlFirmado;
    this.numeroAutorizacion = numeroAutorizacion;
    this.ultimoError = ultimoError;
    this.intentosEnvio = intentosEnvio;
    this.siguienteReintento = siguienteReintento;
    this.creadoEn = Objects.requireNonNull(creadoEn, "creadoEn");
    this.actualizadoEn = Objects.requireNonNull(actualizadoEn, "actualizadoEn");
  }

  public static Comprobante crear(
      ComprobanteId id,
      TipoComprobante tipo,
      InfoTributaria infoTributaria,
      InfoDocumento infoDocumento,
      List<Detalle> detalles,
      Instant ahora
  ) {
    Comprobante comprobante = new Comprobante(
        id,
        tipo,
        infoTributaria,
        infoDocumento,
        detalles,
        EstadoComprobante.CREADO,
        null,
        null,
        null,
        null,
        null,
        0,
        null,
        ahora,
        ahora
    );
    comprobante.registrarEvento(ComprobanteEventType.CREADO, "Comprobante creado", ahora);
    return comprobante;
  }

  public static Comprobante reconstruir(
      ComprobanteId id,
      TipoComprobante tipo,
      InfoTributaria infoTributaria,
      InfoDocumento infoDocumento,
      List<Detalle> detalles,
      EstadoComprobante estado,
      ClaveAcceso claveAcceso,
      String xml,
      String xmlFirmado,
      String numeroAutorizacion,
      String ultimoError,
      int intentosEnvio,
      Instant siguienteReintento,
      Instant creadoEn,
      Instant actualizadoEn
  ) {
    return new Comprobante(
        id,
        tipo,
        infoTributaria,
        infoDocumento,
        detalles,
        estado,
        claveAcceso,
        xml,
        xmlFirmado,
        numeroAutorizacion,
        ultimoError,
        intentosEnvio,
        siguienteReintento,
        creadoEn,
        actualizadoEn
    );
  }

  public ComprobanteId id() {
    return id;
  }

  public TipoComprobante tipo() {
    return tipo;
  }

  public InfoTributaria infoTributaria() {
    return infoTributaria;
  }

  public InfoDocumento infoDocumento() {
    return infoDocumento;
  }

  public List<Detalle> detalles() {
    return detalles;
  }

  public EstadoComprobante estado() {
    return estado;
  }

  public ClaveAcceso claveAcceso() {
    return claveAcceso;
  }

  public String xml() {
    return xml;
  }

  public String xmlFirmado() {
    return xmlFirmado;
  }

  public String numeroAutorizacion() {
    return numeroAutorizacion;
  }

  public String ultimoError() {
    return ultimoError;
  }

  public int intentosEnvio() {
    return intentosEnvio;
  }

  public Instant siguienteReintento() {
    return siguienteReintento;
  }

  public Instant creadoEn() {
    return creadoEn;
  }

  public Instant actualizadoEn() {
    return actualizadoEn;
  }

  public void asignarClaveAcceso(ClaveAcceso claveAcceso) {
    this.claveAcceso = Objects.requireNonNull(claveAcceso, "claveAcceso");
    this.infoTributaria = infoTributaria.withClaveAcceso(claveAcceso);
  }

  public void registrarXml(String xml) {
    this.xml = Objects.requireNonNull(xml, "xml");
  }

  public void registrarXmlFirmado(String xmlFirmado) {
    this.xmlFirmado = Objects.requireNonNull(xmlFirmado, "xmlFirmado");
  }

  public void registrarAutorizacion(String numeroAutorizacion) {
    this.numeroAutorizacion = Objects.requireNonNull(numeroAutorizacion, "numeroAutorizacion");
    this.ultimoError = null;
  }

  public void registrarRechazo(String motivo) {
    this.ultimoError = Objects.requireNonNull(motivo, "motivo");
  }

  public void programarSiguienteReintento(Instant siguienteReintento) {
    this.siguienteReintento = siguienteReintento;
  }

  public void incrementarIntento(Instant ahora, Instant siguienteReintento) {
    this.intentosEnvio += 1;
    this.siguienteReintento = siguienteReintento;
    this.actualizadoEn = ahora;
  }

      public void aplicarEstado(EstadoComprobante nuevoEstado, Instant ahora, String detalle) {
    this.estado = nuevoEstado;
    this.actualizadoEn = ahora;
    registrarEvento(toEventType(nuevoEstado), detalle, ahora);
  }

  private void registrarEvento(ComprobanteEventType tipoEvento, String detalle, Instant ahora) {
    eventos.add(new ComprobanteEvent(UUID.randomUUID(), id, tipoEvento, detalle, ahora));
  }

  public List<ComprobanteEvent> pullEvents() {
    List<ComprobanteEvent> snapshot = new ArrayList<>(eventos);
    eventos.clear();
    return snapshot;
  }

  private ComprobanteEventType toEventType(EstadoComprobante estado) {
    return switch (estado) {
      case CREADO -> ComprobanteEventType.CREADO;
      case VALIDADO -> ComprobanteEventType.VALIDADO;
      case FIRMADO -> ComprobanteEventType.FIRMADO;
      case EN_COLA -> ComprobanteEventType.EN_COLA;
      case ENVIADO -> ComprobanteEventType.ENVIADO;
      case AUTORIZADO -> ComprobanteEventType.AUTORIZADO;
      case RECHAZADO -> ComprobanteEventType.RECHAZADO;
      case ERROR -> ComprobanteEventType.ERROR;
    };
  }
}
