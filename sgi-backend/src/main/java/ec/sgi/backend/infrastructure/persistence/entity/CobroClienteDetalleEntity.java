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
@Table(name = "cobros_cliente_detalle")
public class CobroClienteDetalleEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "cobro_cliente_id")
  private CobroClienteEntity cobroCliente;

  private Long cuentaPorCobrarId;
  private BigDecimal montoAplicado;

  public CobroClienteDetalleEntity() {
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public CobroClienteEntity getCobroCliente() {
    return cobroCliente;
  }

  public void setCobroCliente(CobroClienteEntity cobroCliente) {
    this.cobroCliente = cobroCliente;
  }

  public Long getCuentaPorCobrarId() {
    return cuentaPorCobrarId;
  }

  public void setCuentaPorCobrarId(Long cuentaPorCobrarId) {
    this.cuentaPorCobrarId = cuentaPorCobrarId;
  }

  public BigDecimal getMontoAplicado() {
    return montoAplicado;
  }

  public void setMontoAplicado(BigDecimal montoAplicado) {
    this.montoAplicado = montoAplicado;
  }
}
