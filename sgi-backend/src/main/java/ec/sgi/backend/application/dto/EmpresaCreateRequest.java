package ec.sgi.backend.application.dto;

import jakarta.validation.constraints.NotBlank;

public record EmpresaCreateRequest(
    @NotBlank String ambiente,
    @NotBlank String tipoEmision,
    @NotBlank String razonSocial,
    @NotBlank String nombreComercial,
    @NotBlank String ruc,
    @NotBlank String dirMatriz,
    @NotBlank String estab,
    @NotBlank String ptoEmi,
    @NotBlank String secuencial,
    String secuencialPruebas,
    Boolean obligadoContabilidad,
    String regimenTributario,
    Boolean regimenRimpe,
    Boolean contribuyenteEspecial,
    String numeroContribuyenteEspecial,
    Boolean agenteRetencion,
    Integer creditoDiasDefault
) {
  public EmpresaCreateRequest(
      String ambiente,
      String tipoEmision,
      String razonSocial,
      String nombreComercial,
      String ruc,
      String dirMatriz,
      String estab,
      String ptoEmi,
      String secuencial,
      Boolean obligadoContabilidad,
      Boolean regimenRimpe,
      Integer creditoDiasDefault
  ) {
    this(
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
        obligadoContabilidad,
        null,
        regimenRimpe,
        false,
        null,
        false,
        creditoDiasDefault
    );
  }
}
