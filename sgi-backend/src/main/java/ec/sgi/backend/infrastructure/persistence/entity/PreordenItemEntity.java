package ec.sgi.backend.infrastructure.persistence.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;

@Entity
@Table(name = "preorden_items")
public class PreordenItemEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "preorden_id")
  private PreordenEntity preorden;

  private Long bodegaId;
  private Long productoId;
  private String codigoPrincipal;
  private String descripcion;
  private BigDecimal cantidad;
  private BigDecimal precioUnitario;
  private BigDecimal descuento;
  private BigDecimal precioTotalSinImpuesto;

  public PreordenItemEntity() {
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public PreordenEntity getPreorden() {
    return preorden;
  }

  public void setPreorden(PreordenEntity preorden) {
    this.preorden = preorden;
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

  public String getCodigoPrincipal() {
    return codigoPrincipal;
  }

  public void setCodigoPrincipal(String codigoPrincipal) {
    this.codigoPrincipal = codigoPrincipal;
  }

  public String getDescripcion() {
    return descripcion;
  }

  public void setDescripcion(String descripcion) {
    this.descripcion = descripcion;
  }

  public BigDecimal getCantidad() {
    return cantidad;
  }

  public void setCantidad(BigDecimal cantidad) {
    this.cantidad = cantidad;
  }

  public BigDecimal getPrecioUnitario() {
    return precioUnitario;
  }

  public void setPrecioUnitario(BigDecimal precioUnitario) {
    this.precioUnitario = precioUnitario;
  }

  public BigDecimal getDescuento() {
    return descuento;
  }

  public void setDescuento(BigDecimal descuento) {
    this.descuento = descuento;
  }

  public BigDecimal getPrecioTotalSinImpuesto() {
    return precioTotalSinImpuesto;
  }

  public void setPrecioTotalSinImpuesto(BigDecimal precioTotalSinImpuesto) {
    this.precioTotalSinImpuesto = precioTotalSinImpuesto;
  }
}
