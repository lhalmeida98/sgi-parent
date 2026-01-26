package ec.sgi.backend.infrastructure.persistence.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;

@Entity
@Table(name = "impuestos")
public class ImpuestoEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private Long empresaId;
  private String codigo;
  private String codigoPorcentaje;
  private BigDecimal tarifa;
  private String descripcion;
  private Boolean activo;

  public ImpuestoEntity() {
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

  public String getDescripcion() {
    return descripcion;
  }

  public void setDescripcion(String descripcion) {
    this.descripcion = descripcion;
  }

  public Boolean getActivo() {
    return activo;
  }

  public void setActivo(Boolean activo) {
    this.activo = activo;
  }
}
