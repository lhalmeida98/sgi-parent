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
@Table(name = "pagos_proveedor_detalle")
public class PagoProveedorDetalleEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "pago_proveedor_id")
  private PagoProveedorEntity pagoProveedor;

  private Long cuentaPorPagarId;
  private BigDecimal montoAplicado;

  public PagoProveedorDetalleEntity() {
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public PagoProveedorEntity getPagoProveedor() {
    return pagoProveedor;
  }

  public void setPagoProveedor(PagoProveedorEntity pagoProveedor) {
    this.pagoProveedor = pagoProveedor;
  }

  public Long getCuentaPorPagarId() {
    return cuentaPorPagarId;
  }

  public void setCuentaPorPagarId(Long cuentaPorPagarId) {
    this.cuentaPorPagarId = cuentaPorPagarId;
  }

  public BigDecimal getMontoAplicado() {
    return montoAplicado;
  }

  public void setMontoAplicado(BigDecimal montoAplicado) {
    this.montoAplicado = montoAplicado;
  }
}
