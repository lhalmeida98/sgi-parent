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
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "preordenes")
public class PreordenEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private Long empresaId;
  private Long clienteId;
  private LocalDateTime fechaCreacion;
  private String dirEstablecimiento;
  private String moneda;
  private BigDecimal totalSinImpuestos;
  private BigDecimal totalDescuento;
  private BigDecimal totalImpuestos;
  private BigDecimal importeTotal;
  private String estado;
  private String observaciones;
  private Boolean reservaInventario;

  @OneToMany(mappedBy = "preorden", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
  private List<PreordenItemEntity> items = new ArrayList<>();

  public PreordenEntity() {
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

  public LocalDateTime getFechaCreacion() {
    return fechaCreacion;
  }

  public void setFechaCreacion(LocalDateTime fechaCreacion) {
    this.fechaCreacion = fechaCreacion;
  }

  public String getDirEstablecimiento() {
    return dirEstablecimiento;
  }

  public void setDirEstablecimiento(String dirEstablecimiento) {
    this.dirEstablecimiento = dirEstablecimiento;
  }

  public String getMoneda() {
    return moneda;
  }

  public void setMoneda(String moneda) {
    this.moneda = moneda;
  }

  public BigDecimal getTotalSinImpuestos() {
    return totalSinImpuestos;
  }

  public void setTotalSinImpuestos(BigDecimal totalSinImpuestos) {
    this.totalSinImpuestos = totalSinImpuestos;
  }

  public BigDecimal getTotalDescuento() {
    return totalDescuento;
  }

  public void setTotalDescuento(BigDecimal totalDescuento) {
    this.totalDescuento = totalDescuento;
  }

  public BigDecimal getTotalImpuestos() {
    return totalImpuestos;
  }

  public void setTotalImpuestos(BigDecimal totalImpuestos) {
    this.totalImpuestos = totalImpuestos;
  }

  public BigDecimal getImporteTotal() {
    return importeTotal;
  }

  public void setImporteTotal(BigDecimal importeTotal) {
    this.importeTotal = importeTotal;
  }

  public String getEstado() {
    return estado;
  }

  public void setEstado(String estado) {
    this.estado = estado;
  }

  public String getObservaciones() {
    return observaciones;
  }

  public void setObservaciones(String observaciones) {
    this.observaciones = observaciones;
  }

  public Boolean getReservaInventario() {
    return reservaInventario;
  }

  public void setReservaInventario(Boolean reservaInventario) {
    this.reservaInventario = reservaInventario;
  }

  public List<PreordenItemEntity> getItems() {
    return items;
  }

  public void setItems(List<PreordenItemEntity> items) {
    this.items = items;
  }
}
