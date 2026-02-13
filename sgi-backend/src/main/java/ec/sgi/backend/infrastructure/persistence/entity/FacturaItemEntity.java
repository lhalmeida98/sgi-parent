package ec.sgi.backend.infrastructure.persistence.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "factura_items")
public class FacturaItemEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "factura_id")
  private FacturaEntity factura;

  private Long bodegaId;
  private Long productoId;
  private String codigoPrincipal;
  private String descripcion;
  private BigDecimal cantidad;
  private BigDecimal precioUnitario;
  private BigDecimal descuento;
  private BigDecimal precioTotalSinImpuesto;

  @OneToMany(mappedBy = "item", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
  private List<FacturaImpuestoEntity> impuestos = new ArrayList<>();

  public FacturaItemEntity() {
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public FacturaEntity getFactura() {
    return factura;
  }

  public void setFactura(FacturaEntity factura) {
    this.factura = factura;
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

  public List<FacturaImpuestoEntity> getImpuestos() {
    return impuestos;
  }

  public void setImpuestos(List<FacturaImpuestoEntity> impuestos) {
    this.impuestos = impuestos;
  }
}
