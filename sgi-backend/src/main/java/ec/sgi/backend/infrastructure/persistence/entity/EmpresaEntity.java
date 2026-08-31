package ec.sgi.backend.infrastructure.persistence.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "empresas")
public class EmpresaEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private String ambiente;
  private String tipoEmision;
  private String razonSocial;
  private String nombreComercial;
  private String ruc;
  private String dirMatriz;
  private String estab;
  private String ptoEmi;
  private String secuencial;
  private String logoRuta;
  private boolean obligadoContabilidad;
  private boolean regimenRimpe;
  private String regimenTributario;
  private boolean contribuyenteEspecial;
  private String numeroContribuyenteEspecial;
  private boolean agenteRetencion;
  private Integer creditoDiasDefault;

  public EmpresaEntity() {
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getAmbiente() {
    return ambiente;
  }

  public void setAmbiente(String ambiente) {
    this.ambiente = ambiente;
  }

  public String getTipoEmision() {
    return tipoEmision;
  }

  public void setTipoEmision(String tipoEmision) {
    this.tipoEmision = tipoEmision;
  }

  public String getRazonSocial() {
    return razonSocial;
  }

  public void setRazonSocial(String razonSocial) {
    this.razonSocial = razonSocial;
  }

  public String getNombreComercial() {
    return nombreComercial;
  }

  public void setNombreComercial(String nombreComercial) {
    this.nombreComercial = nombreComercial;
  }

  public String getRuc() {
    return ruc;
  }

  public void setRuc(String ruc) {
    this.ruc = ruc;
  }

  public String getDirMatriz() {
    return dirMatriz;
  }

  public void setDirMatriz(String dirMatriz) {
    this.dirMatriz = dirMatriz;
  }

  public String getEstab() {
    return estab;
  }

  public void setEstab(String estab) {
    this.estab = estab;
  }

  public String getPtoEmi() {
    return ptoEmi;
  }

  public void setPtoEmi(String ptoEmi) {
    this.ptoEmi = ptoEmi;
  }

  public String getSecuencial() {
    return secuencial;
  }

  public void setSecuencial(String secuencial) {
    this.secuencial = secuencial;
  }

  public String getLogoRuta() {
    return logoRuta;
  }

  public void setLogoRuta(String logoRuta) {
    this.logoRuta = logoRuta;
  }

  public boolean isObligadoContabilidad() {
    return obligadoContabilidad;
  }

  public void setObligadoContabilidad(boolean obligadoContabilidad) {
    this.obligadoContabilidad = obligadoContabilidad;
  }

  public boolean isRegimenRimpe() {
    return regimenRimpe;
  }

  public void setRegimenRimpe(boolean regimenRimpe) {
    this.regimenRimpe = regimenRimpe;
  }

  public String getRegimenTributario() {
    return regimenTributario;
  }

  public void setRegimenTributario(String regimenTributario) {
    this.regimenTributario = regimenTributario;
  }

  public boolean isContribuyenteEspecial() {
    return contribuyenteEspecial;
  }

  public void setContribuyenteEspecial(boolean contribuyenteEspecial) {
    this.contribuyenteEspecial = contribuyenteEspecial;
  }

  public String getNumeroContribuyenteEspecial() {
    return numeroContribuyenteEspecial;
  }

  public void setNumeroContribuyenteEspecial(String numeroContribuyenteEspecial) {
    this.numeroContribuyenteEspecial = numeroContribuyenteEspecial;
  }

  public boolean isAgenteRetencion() {
    return agenteRetencion;
  }

  public void setAgenteRetencion(boolean agenteRetencion) {
    this.agenteRetencion = agenteRetencion;
  }

  public Integer getCreditoDiasDefault() {
    return creditoDiasDefault;
  }

  public void setCreditoDiasDefault(Integer creditoDiasDefault) {
    this.creditoDiasDefault = creditoDiasDefault;
  }
}
