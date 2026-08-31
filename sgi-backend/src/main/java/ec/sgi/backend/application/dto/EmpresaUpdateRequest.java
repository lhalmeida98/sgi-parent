package ec.sgi.backend.application.dto;

import jakarta.validation.constraints.NotBlank;

public record EmpresaUpdateRequest(
    @NotBlank String ambiente,
    @NotBlank String tipoEmision,
    @NotBlank String razonSocial,
    @NotBlank String nombreComercial,
    @NotBlank String dirMatriz,
    @NotBlank String estab,
    @NotBlank String ptoEmi,
    @NotBlank String secuencial,
    Boolean obligadoContabilidad,
    String regimenTributario,
    Boolean regimenRimpe,
    Boolean contribuyenteEspecial,
    String numeroContribuyenteEspecial,
    Boolean agenteRetencion,
    Integer creditoDiasDefault
) {
  public EmpresaUpdateRequest(
      String ambiente,
      String tipoEmision,
      String razonSocial,
      String nombreComercial,
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
        dirMatriz,
        estab,
        ptoEmi,
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
