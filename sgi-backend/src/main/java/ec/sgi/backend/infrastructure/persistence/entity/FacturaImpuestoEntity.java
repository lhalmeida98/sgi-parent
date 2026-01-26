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
@Table(name = "factura_impuestos")
public class FacturaImpuestoEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "item_id")
  private FacturaItemEntity item;

  private String codigo;
  private String codigoPorcentaje;
  private BigDecimal tarifa;
  private BigDecimal baseImponible;
  private BigDecimal valor;

  public FacturaImpuestoEntity() {
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public FacturaItemEntity getItem() {
    return item;
  }

  public void setItem(FacturaItemEntity item) {
    this.item = item;
  }

  public String getCodigo() {
    return codigo;
  }

  public void setCodigo(String codigo) {
    this.codigo = codigo;
  }

  public String getCodigoPorcentaje() {
    return codigoPorcentaje;
  }

  public void setCodigoPorcentaje(String codigoPorcentaje) {
    this.codigoPorcentaje = codigoPorcentaje;
  }

  public BigDecimal getTarifa() {
    return tarifa;
  }

  public void setTarifa(BigDecimal tarifa) {
    this.tarifa = tarifa;
  }

  public BigDecimal getBaseImponible() {
    return baseImponible;
  }

  public void setBaseImponible(BigDecimal baseImponible) {
    this.baseImponible = baseImponible;
  }

  public BigDecimal getValor() {
    return valor;
  }

  public void setValor(BigDecimal valor) {
    this.valor = valor;
  }
}
