package ec.sri.einvoice.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "comprobante")
public class ComprobanteEntity {
  @Id
  private UUID id;

  private String tipo;
  private String estado;

  @Column(name = "clave_acceso")
  private String claveAcceso;

  @Lob
  private String xml;

  @Lob
  @Column(name = "xml_firmado")
  private String xmlFirmado;

  @Column(name = "numero_autorizacion")
  private String numeroAutorizacion;

  @Column(name = "emisor_ruc")
  private String emisorRuc;

  @Column(name = "receptor_identificacion")
  private String receptorIdentificacion;

  private BigDecimal total;

  @Column(name = "intentos_envio")
  private int intentosEnvio;

  @Column(name = "siguiente_reintento")
  private Instant siguienteReintento;

  @Column(name = "creado_en")
  private Instant creadoEn;

  @Column(name = "actualizado_en")
  private Instant actualizadoEn;

  @Lob
  @Column(name = "payload_json")
  private String payloadJson;

  public UUID getId() {
    return id;
  }

  public void setId(UUID id) {
    this.id = id;
  }

  public String getTipo() {
    return tipo;
  }

  public void setTipo(String tipo) {
    this.tipo = tipo;
  }

  public String getEstado() {
    return estado;
  }

  public void setEstado(String estado) {
    this.estado = estado;
  }

  public String getClaveAcceso() {
    return claveAcceso;
  }

  public void setClaveAcceso(String claveAcceso) {
    this.claveAcceso = claveAcceso;
  }

  public String getXml() {
    return xml;
  }

  public void setXml(String xml) {
    this.xml = xml;
  }

  public String getXmlFirmado() {
    return xmlFirmado;
  }

  public void setXmlFirmado(String xmlFirmado) {
    this.xmlFirmado = xmlFirmado;
  }

  public String getNumeroAutorizacion() {
    return numeroAutorizacion;
  }

  public void setNumeroAutorizacion(String numeroAutorizacion) {
    this.numeroAutorizacion = numeroAutorizacion;
  }

  public String getEmisorRuc() {
    return emisorRuc;
  }

  public void setEmisorRuc(String emisorRuc) {
    this.emisorRuc = emisorRuc;
  }

  public String getReceptorIdentificacion() {
    return receptorIdentificacion;
  }

  public void setReceptorIdentificacion(String receptorIdentificacion) {
    this.receptorIdentificacion = receptorIdentificacion;
  }

  public BigDecimal getTotal() {
    return total;
  }

  public void setTotal(BigDecimal total) {
    this.total = total;
  }

  public int getIntentosEnvio() {
    return intentosEnvio;
  }

  public void setIntentosEnvio(int intentosEnvio) {
    this.intentosEnvio = intentosEnvio;
  }

  public Instant getSiguienteReintento() {
    return siguienteReintento;
  }

  public void setSiguienteReintento(Instant siguienteReintento) {
    this.siguienteReintento = siguienteReintento;
  }

  public Instant getCreadoEn() {
    return creadoEn;
  }

  public void setCreadoEn(Instant creadoEn) {
    this.creadoEn = creadoEn;
  }

  public Instant getActualizadoEn() {
    return actualizadoEn;
  }

  public void setActualizadoEn(Instant actualizadoEn) {
    this.actualizadoEn = actualizadoEn;
  }

  public String getPayloadJson() {
    return payloadJson;
  }

  public void setPayloadJson(String payloadJson) {
    this.payloadJson = payloadJson;
  }
}
