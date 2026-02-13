package ec.sgi.backend.infrastructure.persistence.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "inventarios")
public class InventarioEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private Long empresaId;
  private Long bodegaId;
  private Long productoId;
  private BigDecimal stockActual;
  private BigDecimal stockReservado;
  private BigDecimal stockMinimo;
  private BigDecimal stockMaximo;
  private String ubicacion;
  private BigDecimal costoPromedio;
  private LocalDateTime actualizadoEn;

  public InventarioEntity() {
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

  public Long getBodegaId() {
    return bodegaId;
  }

  public void setBodegaId(Long bodegaId) {
    this.bodegaId = bodegaId;
  }

  public Long getProductoId() {
    return productoId;
  }

  public void setProductoId(Long productoId) {
    this.productoId = productoId;
  }

  public BigDecimal getStockActual() {
    return stockActual;
  }

  public void setStockActual(BigDecimal stockActual) {
    this.stockActual = stockActual;
  }

  public BigDecimal getStockReservado() {
    return stockReservado;
  }

  public void setStockReservado(BigDecimal stockReservado) {
    this.stockReservado = stockReservado;
  }

  public BigDecimal getStockMinimo() {
    return stockMinimo;
  }

  public void setStockMinimo(BigDecimal stockMinimo) {
    this.stockMinimo = stockMinimo;
  }

  public BigDecimal getStockMaximo() {
    return stockMaximo;
  }

  public void setStockMaximo(BigDecimal stockMaximo) {
    this.stockMaximo = stockMaximo;
  }

  public String getUbicacion() {
    return ubicacion;
  }

  public void setUbicacion(String ubicacion) {
    this.ubicacion = ubicacion;
  }

  public BigDecimal getCostoPromedio() {
    return costoPromedio;
  }

  public void setCostoPromedio(BigDecimal costoPromedio) {
    this.costoPromedio = costoPromedio;
  }

  public LocalDateTime getActualizadoEn() {
    return actualizadoEn;
  }

  public void setActualizadoEn(LocalDateTime actualizadoEn) {
    this.actualizadoEn = actualizadoEn;
  }
}
