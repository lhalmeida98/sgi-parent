package ec.sri.einvoice.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "bitacora")
public class BitacoraEntity {
  @Id
  private UUID id;

  @Column(name = "comprobante_id")
  private UUID comprobanteId;

  private String accion;

  @Column(columnDefinition = "text")
  private String detalle;

  @Column(name = "ocurrido_en")
  private Instant ocurridoEn;

  public UUID getId() {
    return id;
  }

  public void setId(UUID id) {
    this.id = id;
  }

  public UUID getComprobanteId() {
    return comprobanteId;
  }

  public void setComprobanteId(UUID comprobanteId) {
    this.comprobanteId = comprobanteId;
  }

  public String getAccion() {
    return accion;
  }

  public void setAccion(String accion) {
    this.accion = accion;
  }

  public String getDetalle() {
    return detalle;
  }

  public void setDetalle(String detalle) {
    this.detalle = detalle;
  }

  public Instant getOcurridoEn() {
    return ocurridoEn;
  }

  public void setOcurridoEn(Instant ocurridoEn) {
    this.ocurridoEn = ocurridoEn;
  }
}
