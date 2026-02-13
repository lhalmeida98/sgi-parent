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
@Table(name = "documentos_proveedor_items")
public class DocumentoProveedorItemEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "documento_proveedor_id")
  private DocumentoProveedorEntity documentoProveedor;

  private Long bodegaId;
  private Long productoId;
  private String codigoPrincipal;
  private String descripcion;
  private BigDecimal cantidad;
  private BigDecimal costoUnitario;
  private BigDecimal subtotal;

  public DocumentoProveedorItemEntity() {
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public DocumentoProveedorEntity getDocumentoProveedor() {
    return documentoProveedor;
  }

  public void setDocumentoProveedor(DocumentoProveedorEntity documentoProveedor) {
    this.documentoProveedor = documentoProveedor;
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

  public BigDecimal getCostoUnitario() {
    return costoUnitario;
  }

  public void setCostoUnitario(BigDecimal costoUnitario) {
    this.costoUnitario = costoUnitario;
  }

  public BigDecimal getSubtotal() {
    return subtotal;
  }

  public void setSubtotal(BigDecimal subtotal) {
    this.subtotal = subtotal;
  }
}
