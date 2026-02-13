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
@Table(name = "cuentas_por_pagar")
public class CuentaPorPagarEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private Long empresaId;
  private Long proveedorId;
  private Long documentoProveedorId;
  private BigDecimal montoOriginal;
  private BigDecimal montoPagado;
  private BigDecimal saldo;
  private String estado;
  private LocalDate fechaVencimiento;
  private LocalDateTime creadoEn;
  private LocalDateTime actualizadoEn;

  public CuentaPorPagarEntity() {
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

  public Long getProveedorId() {
    return proveedorId;
  }

  public void setProveedorId(Long proveedorId) {
    this.proveedorId = proveedorId;
  }

  public Long getDocumentoProveedorId() {
    return documentoProveedorId;
  }

  public void setDocumentoProveedorId(Long documentoProveedorId) {
    this.documentoProveedorId = documentoProveedorId;
  }

  public BigDecimal getMontoOriginal() {
    return montoOriginal;
  }

  public void setMontoOriginal(BigDecimal montoOriginal) {
    this.montoOriginal = montoOriginal;
  }

  public BigDecimal getMontoPagado() {
    return montoPagado;
  }

  public void setMontoPagado(BigDecimal montoPagado) {
    this.montoPagado = montoPagado;
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
