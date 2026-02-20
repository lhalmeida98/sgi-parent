package ec.sgi.backend.infrastructure.persistence.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "cobros_cliente")
public class CobroClienteEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private Long empresaId;
  private Long clienteId;
  private LocalDate fecha;
  private String formaPago;
  private String referencia;
  private BigDecimal montoTotal;
  private String observacion;
  private LocalDateTime creadoEn;

  @OneToMany(mappedBy = "cobroCliente", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
  private List<CobroClienteDetalleEntity> detalles = new ArrayList<>();

  public CobroClienteEntity() {
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public Long getEmpresaId() {
    return empresaId;
  }

  public void setEmpresaId(Long empresaId) {
    this.empresaId = empresaId;
  }

  public Long getClienteId() {
    return clienteId;
  }

  public void setClienteId(Long clienteId) {
    this.clienteId = clienteId;
  }

  public LocalDate getFecha() {
    return fecha;
  }

  public void setFecha(LocalDate fecha) {
    this.fecha = fecha;
  }

  public String getFormaPago() {
    return formaPago;
  }

  public void setFormaPago(String formaPago) {
    this.formaPago = formaPago;
  }

  public String getReferencia() {
    return referencia;
  }

  public void setReferencia(String referencia) {
    this.referencia = referencia;
  }

  public BigDecimal getMontoTotal() {
    return montoTotal;
  }

  public void setMontoTotal(BigDecimal montoTotal) {
    this.montoTotal = montoTotal;
  }

  public String getObservacion() {
    return observacion;
  }

  public void setObservacion(String observacion) {
    this.observacion = observacion;
  }

  public LocalDateTime getCreadoEn() {
    return creadoEn;
  }

  public void setCreadoEn(LocalDateTime creadoEn) {
    this.creadoEn = creadoEn;
  }

  public List<CobroClienteDetalleEntity> getDetalles() {
    return detalles;
  }

  public void setDetalles(List<CobroClienteDetalleEntity> detalles) {
    this.detalles = detalles;
  }
}
