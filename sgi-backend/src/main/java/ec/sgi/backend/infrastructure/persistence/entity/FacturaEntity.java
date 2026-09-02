package ec.sgi.backend.infrastructure.persistence.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "facturas")
public class FacturaEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private Long empresaId;
  private Long clienteId;
  private Long preordenId;
  private LocalDate fechaEmision;
  private String dirEstablecimiento;
  private String moneda;
  private String observaciones;

  private BigDecimal totalSinImpuestos;
  private BigDecimal totalDescuento;
  private BigDecimal totalImpuestos;
  private BigDecimal importeTotal;

  private String estado;
  private String claveAcceso;
  private String coreComprobanteId;
  private String sriEstadoConsulta;
  private String sriEstadoAutorizacion;
  private String sriMensaje;
  private String numeroAutorizacion;
  private LocalDateTime fechaAutorizacion;
  @Lob
  private String xmlFirmado;
  @Lob
  private String xmlAutorizado;
  private Integer intentosConsulta;
  private LocalDateTime ultimoIntentoConsulta;

  private String infoAmbiente;
  private String infoTipoEmision;
  private String infoRazonSocial;
  private String infoNombreComercial;
  private String infoRuc;
  private String infoDirMatriz;
  private String infoEstab;
  private String infoPtoEmi;
  private String infoSecuencial;
  private Boolean infoObligadoContabilidad;
  private String infoRegimenTributario;
  private Boolean infoContribuyenteEspecial;
  private String infoNumeroContribuyenteEspecial;
  private Boolean infoAgenteRetencion;

  @OneToMany(mappedBy = "factura", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
  private List<FacturaItemEntity> items = new ArrayList<>();

  @OneToMany(mappedBy = "factura", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
  private List<FacturaPagoEntity> pagos = new ArrayList<>();

  public FacturaEntity() {
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

  public Long getPreordenId() {
    return preordenId;
  }

  public void setPreordenId(Long preordenId) {
    this.preordenId = preordenId;
  }

  public LocalDate getFechaEmision() {
    return fechaEmision;
  }

  public void setFechaEmision(LocalDate fechaEmision) {
    this.fechaEmision = fechaEmision;
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

  public String getObservaciones() {
    return observaciones;
  }

  public void setObservaciones(String observaciones) {
    this.observaciones = observaciones;
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

  public String getClaveAcceso() {
    return claveAcceso;
  }

  public void setClaveAcceso(String claveAcceso) {
    this.claveAcceso = claveAcceso;
  }

  public String getCoreComprobanteId() {
    return coreComprobanteId;
  }

  public void setCoreComprobanteId(String coreComprobanteId) {
    this.coreComprobanteId = coreComprobanteId;
  }

  public String getSriEstadoConsulta() {
    return sriEstadoConsulta;
  }

  public void setSriEstadoConsulta(String sriEstadoConsulta) {
    this.sriEstadoConsulta = sriEstadoConsulta;
  }

  public String getSriEstadoAutorizacion() {
    return sriEstadoAutorizacion;
  }

  public void setSriEstadoAutorizacion(String sriEstadoAutorizacion) {
    this.sriEstadoAutorizacion = sriEstadoAutorizacion;
  }

  public String getSriMensaje() {
    return sriMensaje;
  }

  public void setSriMensaje(String sriMensaje) {
    this.sriMensaje = sriMensaje;
  }

  public String getNumeroAutorizacion() {
    return numeroAutorizacion;
  }

  public void setNumeroAutorizacion(String numeroAutorizacion) {
    this.numeroAutorizacion = numeroAutorizacion;
  }

  public LocalDateTime getFechaAutorizacion() {
    return fechaAutorizacion;
  }

  public void setFechaAutorizacion(LocalDateTime fechaAutorizacion) {
    this.fechaAutorizacion = fechaAutorizacion;
  }

  public String getXmlFirmado() {
    return xmlFirmado;
  }

  public void setXmlFirmado(String xmlFirmado) {
    this.xmlFirmado = xmlFirmado;
  }

  public String getXmlAutorizado() {
    return xmlAutorizado;
  }

  public void setXmlAutorizado(String xmlAutorizado) {
    this.xmlAutorizado = xmlAutorizado;
  }

  public Integer getIntentosConsulta() {
    return intentosConsulta;
  }

  public void setIntentosConsulta(Integer intentosConsulta) {
    this.intentosConsulta = intentosConsulta;
  }

  public LocalDateTime getUltimoIntentoConsulta() {
    return ultimoIntentoConsulta;
  }

  public void setUltimoIntentoConsulta(LocalDateTime ultimoIntentoConsulta) {
    this.ultimoIntentoConsulta = ultimoIntentoConsulta;
  }

  public String getInfoAmbiente() {
    return infoAmbiente;
  }

  public void setInfoAmbiente(String infoAmbiente) {
    this.infoAmbiente = infoAmbiente;
  }

  public String getInfoTipoEmision() {
    return infoTipoEmision;
  }

  public void setInfoTipoEmision(String infoTipoEmision) {
    this.infoTipoEmision = infoTipoEmision;
  }

  public String getInfoRazonSocial() {
    return infoRazonSocial;
  }

  public void setInfoRazonSocial(String infoRazonSocial) {
    this.infoRazonSocial = infoRazonSocial;
  }

  public String getInfoNombreComercial() {
    return infoNombreComercial;
  }

  public void setInfoNombreComercial(String infoNombreComercial) {
    this.infoNombreComercial = infoNombreComercial;
  }

  public String getInfoRuc() {
    return infoRuc;
  }

  public void setInfoRuc(String infoRuc) {
    this.infoRuc = infoRuc;
  }

  public String getInfoDirMatriz() {
    return infoDirMatriz;
  }

  public void setInfoDirMatriz(String infoDirMatriz) {
    this.infoDirMatriz = infoDirMatriz;
  }

  public String getInfoEstab() {
    return infoEstab;
  }

  public void setInfoEstab(String infoEstab) {
    this.infoEstab = infoEstab;
  }

  public String getInfoPtoEmi() {
    return infoPtoEmi;
  }

  public void setInfoPtoEmi(String infoPtoEmi) {
    this.infoPtoEmi = infoPtoEmi;
  }

  public String getInfoSecuencial() {
    return infoSecuencial;
  }

  public void setInfoSecuencial(String infoSecuencial) {
    this.infoSecuencial = infoSecuencial;
  }

  public Boolean getInfoObligadoContabilidad() {
    return infoObligadoContabilidad;
  }

  public void setInfoObligadoContabilidad(Boolean infoObligadoContabilidad) {
    this.infoObligadoContabilidad = infoObligadoContabilidad;
  }

  public String getInfoRegimenTributario() {
    return infoRegimenTributario;
  }

  public void setInfoRegimenTributario(String infoRegimenTributario) {
    this.infoRegimenTributario = infoRegimenTributario;
  }

  public Boolean getInfoContribuyenteEspecial() {
    return infoContribuyenteEspecial;
  }

  public void setInfoContribuyenteEspecial(Boolean infoContribuyenteEspecial) {
    this.infoContribuyenteEspecial = infoContribuyenteEspecial;
  }

  public String getInfoNumeroContribuyenteEspecial() {
    return infoNumeroContribuyenteEspecial;
  }

  public void setInfoNumeroContribuyenteEspecial(String infoNumeroContribuyenteEspecial) {
    this.infoNumeroContribuyenteEspecial = infoNumeroContribuyenteEspecial;
  }

  public Boolean getInfoAgenteRetencion() {
    return infoAgenteRetencion;
  }

  public void setInfoAgenteRetencion(Boolean infoAgenteRetencion) {
    this.infoAgenteRetencion = infoAgenteRetencion;
  }

  public List<FacturaItemEntity> getItems() {
    return items;
  }

  public void setItems(List<FacturaItemEntity> items) {
    this.items = items;
  }

  public List<FacturaPagoEntity> getPagos() {
    return pagos;
  }

  public void setPagos(List<FacturaPagoEntity> pagos) {
    this.pagos = pagos;
  }
}
