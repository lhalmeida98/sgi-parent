package ec.sgi.backend.infrastructure.persistence.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "cuentas_por_cobrar")
public class CuentaPorCobrarEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private Long empresaId;
  private Long clienteId;
  private Long documentoClienteId;
  private BigDecimal montoOriginal;
  private BigDecimal montoCobrado;
  private BigDecimal saldo;
  private String estado;
  private LocalDate fechaVencimiento;
  private Integer creditoDias;
  private LocalDateTime creadoEn;
  private LocalDateTime actualizadoEn;

  public CuentaPorCobrarEntity() {
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

  public Long getDocumentoClienteId() {
    return documentoClienteId;
  }

  public void setDocumentoClienteId(Long documentoClienteId) {
    this.documentoClienteId = documentoClienteId;
  }

  public BigDecimal getMontoOriginal() {
    return montoOriginal;
  }

  public void setMontoOriginal(BigDecimal montoOriginal) {
    this.montoOriginal = montoOriginal;
  }

  public BigDecimal getMontoCobrado() {
    return montoCobrado;
  }

  public void setMontoCobrado(BigDecimal montoCobrado) {
    this.montoCobrado = montoCobrado;
  }

  public BigDecimal getSaldo() {
    return saldo;
  }

  public void setSaldo(BigDecimal saldo) {
    this.saldo = saldo;
  }

  public String getEstado() {
    return estado;
  }

  public void setEstado(String estado) {
    this.estado = estado;
  }

  public LocalDate getFechaVencimiento() {
    return fechaVencimiento;
  }

  public void setFechaVencimiento(LocalDate fechaVencimiento) {
    this.fechaVencimiento = fechaVencimiento;
  }

  public Integer getCreditoDias() {
    return creditoDias;
  }

  public void setCreditoDias(Integer creditoDias) {
    this.creditoDias = creditoDias;
  }

  public LocalDateTime getCreadoEn() {
    return creadoEn;
  }

  public void setCreadoEn(LocalDateTime creadoEn) {
    this.creadoEn = creadoEn;
  }

  public LocalDateTime getActualizadoEn() {
    return actualizadoEn;
  }

  public void setActualizadoEn(LocalDateTime actualizadoEn) {
    this.actualizadoEn = actualizadoEn;
  }
}
