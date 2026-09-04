package ec.sgi.backend.application.dto;


public record EmpresaResult(
    Long id,
    String ambiente,
    String tipoEmision,
    String razonSocial,
    String nombreComercial,
    String ruc,
    String dirMatriz,
    String estab,
    String ptoEmi,
    String secuencial,
    String secuencialPruebas,
    String logoRuta,
    boolean obligadoContabilidad,
    String regimenTributario,
    boolean regimenRimpe,
    boolean contribuyenteEspecial,
    String numeroContribuyenteEspecial,
    boolean agenteRetencion,
    Integer creditoDiasDefault
) {
  public EmpresaResult(
      Long id,
      String ambiente,
      String tipoEmision,
      String razonSocial,
      String nombreComercial,
      String ruc,
      String dirMatriz,
      String estab,
      String ptoEmi,
      String secuencial,
      String logoRuta,
      boolean obligadoContabilidad,
      boolean regimenRimpe,
      Integer creditoDiasDefault
  ) {
    this(
        id,
        ambiente,
        tipoEmision,
        razonSocial,
        nombreComercial,
        ruc,
        dirMatriz,
        estab,
        ptoEmi,
        secuencial,
        secuencial,
        logoRuta,
        obligadoContabilidad,
        regimenRimpe ? "RIMPE_EMPRENDEDOR" : "GENERAL",
        regimenRimpe,
        false,
        null,
        false,
        creditoDiasDefault
    );
  }
}
